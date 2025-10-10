package Airport_Simulation;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TP080522 {
    public static void main(String[] args) {

        // Create a unique atc for all planes
        ATC atc = new ATC("ATC");
        Thread atcThread = new Thread(atc, "Thread-ATC");
        atcThread.start();
        
        // Easier using for loop to start & join threads
        List<Thread> planeThreads = new ArrayList<>();
        
        for (int id = 1; id <= 6; id++) {
            Plane plane = new Plane(id, atc);
            Thread planeThread = new Thread(plane, "Thread-Plane-" + id);
            planeThreads.add(planeThread);
        }
        
        // Wait for user to start
        Scanner scanner = new Scanner(System.in);
        String input;
        System.out.println();        
        do {
            System.out.println("\u001B[30mPlease press '\u001B[35mq\u001B[30m' to continue...\u001B[0m");
            input = scanner.nextLine();
        } while (!input.equalsIgnoreCase("q"));
        scanner.close();
        
        System.out.println("\n = = = = = Asia Pacific Airport Simulation = = = = = = = \n");
        
        long startTime = System.currentTimeMillis();
        
        // Start all planes
        for (Thread t : planeThreads) {
            try {
                Random rand = new Random();
                long random = 800 + rand.nextInt(3) * 350;
                t.start();
                Thread.sleep(random);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Wait for all planes to complete
        for (Thread t : planeThreads) {
            try {
                t.join();
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Shutdown ATC
        atc.shutdown();

        long endTime = System.currentTimeMillis();
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        
        System.out.println("\n = = = = = = = Simulation Completed = = = = = = = = = =  \n");
        System.out.println("Total simulation time: " + String.format("%.2f", totalTimeSeconds) + " seconds");
        
        atc.getAirport().checkList();
        atc.getAirport().printStatistics();
    }
}



class APAirport {
    private final AtomicInteger planesInAirport = new AtomicInteger(0);
    private final Gate[] gates = {new Gate(1), new Gate(2), new Gate(3)};
    private final RefuelingTruck refuelingTruck = new RefuelingTruck();

    // Statistics
    private long totalWaitingTime = 0;
    private long maximumWaitingTime = 0;
    private long minimumWaitingTime = 0;

    private final AtomicInteger totalPlanesServed = new AtomicInteger(0);
    private final AtomicInteger totalPassengersServed = new AtomicInteger(0);
    private final AtomicInteger totalPassengersBoarded = new AtomicInteger(0);
    
    // Runway lock - ensures only one plane uses runway at a time
    private final Object runwayLock = new Object();

    public Object getRunwayLock() {
        return runwayLock;
    }
    
    public void enterAirport() {
        planesInAirport.incrementAndGet();
    }
    
    public void exitAirport() {
        planesInAirport.decrementAndGet();
    }
    
    public int getPlanesInAirport() {
        return planesInAirport.get();
    }
    
    // Assigned to plane
    public Gate findAvailableGate() {
        for (Gate gate : gates) {
            if (gate.isAvailable()) {
                return gate;
            }
        }
        return null;
    }
    
    public RefuelingTruck getRefuelingTruck() {
        return refuelingTruck;
    }
    
    public void recordWaitingTime(long time) {
        if (time > maximumWaitingTime) {
            maximumWaitingTime = time;
        }
        else if (time < minimumWaitingTime || minimumWaitingTime == 0) {
            minimumWaitingTime = time;
        }

        totalWaitingTime += time;
    }

    public long getTotalWaitingTime() {
        return totalWaitingTime;
    }

    public long countAverageWaitingTime(long totalTime, int planeServeds) {
        return (totalTime / planeServeds);
    }
    
    public void incrementPlanesServed() {
        totalPlanesServed.incrementAndGet();
    }
    
    public void countPassengerServed(int count) {
        totalPassengersServed.addAndGet(count);
    }

    public void countPassengerBoarded(int count) {
        totalPassengersBoarded.addAndGet(count);
    }

    public void checkList() {
        System.out.println("\n - - - - - - Checklist - - - - - - - -\n");
        for (Gate gate : gates) {
            System.out.println("Gate-" + gate.getGateNumber() + " is " + (gate.isAvailable() ? "Empty" : "Occupied"));
        }
        
        System.out.println("\nPlanes served: " + totalPlanesServed.get());
        System.out.println("Planes remaining in airport: " + planesInAirport.get());
    }
    
    public void printStatistics() {
    // Waiting time statistics
        System.out.println("\n - - Planes Circle in the Sky (Time) - - -\n");
        System.out.println("Maximum waiting time: " + maximumWaitingTime + " ms");
        System.out.println("Minimum waiting time: " + minimumWaitingTime + " ms");
        System.out.println("Average waiting time: " + countAverageWaitingTime(getTotalWaitingTime(),totalPlanesServed.get()) + " ms");

        System.out.println("\n - - - Total Passenger Statistics - - - -\n");
        System.out.println("Total passengers served: " + totalPassengersServed.get());
        System.out.println("Total passengers boarded: " + totalPassengersBoarded.get());
        System.out.println();
    }
}



class ATC implements Runnable {
    private final String atcName;
    private final APAirport airport;
    private final int MAX_CAPACITY = 3;
    
    // Queue for planes waiting to land & takeoff
    private final LinkedList<Plane> landingQueue = new LinkedList<>();
    private final LinkedList<Plane> takeoffQueue = new LinkedList<>();
    
    // Runway control
    private volatile boolean runwayAvailable = true;

    // Flag to stop ATC thread
    private boolean running = true;
    
    public ATC(String name) {
        this.atcName = name;
        this.airport = new APAirport();
    }
    
    public APAirport getAirport() {
        return airport;
    }
    
    @Override
    public void run() {
        System.out.println(atcName + " created");

        while (running) {
            try {
                // Clear the airport first
                processTakeoffRequests();

                processLandingRequests();
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Approve takeoff requests
    private synchronized void processTakeoffRequests() {

        // Runway clear && plane waiting for takeoff
        if (runwayAvailable && !takeoffQueue.isEmpty()) {
            System.out.println(atcName +": Runway is now free.");

            // first in first out (peek read first elements)
            Plane nextPlane = takeoffQueue.peek();
            
            // If plane is waiting only
            if (nextPlane.waitingTakeOffApproval()) {
                
                // Mark runway occupied
                runwayAvailable = false;
                
                System.out.println(atcName +": Takeoff granted for " + nextPlane.getName() + ".");
                
                // Approve plane to takeoff
                nextPlane.approveAndStartTakeOff();
                
                // Wake up the plane
                synchronized (nextPlane) {
                    nextPlane.notify();
                }
            }
        }
    }

    // Approve landing requests
    private synchronized void processLandingRequests() {

        // Runway clear && airport not full && plane waiting for landing
        if (runwayAvailable && airport.getPlanesInAirport() < MAX_CAPACITY && !landingQueue.isEmpty()) {
            System.out.println(atcName +": Runway is now free.");

            Plane nextPlane = landingQueue.peek(); 

            if (nextPlane.waitingLandApproval()) {
                runwayAvailable = false;
                
                if (nextPlane.isEmergency()) {
                    System.out.println(atcName + ": Emergency landing authorized for " + nextPlane.getName() + ".");
                } 
                else {
                    System.out.println(atcName + ": Landing permission granted for " + nextPlane.getName() + ".");
                }

                nextPlane.availableToLand();
                
                synchronized (nextPlane) {
                    nextPlane.notify();
                }
            }
        }
    }
    
    // Plane added into queue
    public synchronized void addLandingQueue(Plane plane) {
        String planeName = plane.getName();
        boolean isEmergency = plane.isEmergency();
        
        if (isEmergency) {
            System.out.println(planeName + ": Requesting emergency landing!!!");
        } 
        else {
            System.out.println(planeName + ": Requesting landing.");
        }
        
        // Prioritize emergency plane(s)
        if (isEmergency) {
            int insertPos = 0;

            for (int i = 0; i < landingQueue.size(); i++) {

                // if current plane is not emergency, insert before it
                if (!landingQueue.get(i).isEmergency()) {
                    insertPos = i;
                    break;
                }
            }

            landingQueue.add(insertPos, plane);
        }
        else { // Adding plane in queue
            landingQueue.add(plane);
        }
    }

    // Remove plane from queue
    public synchronized void planeLanded(Plane plane) {
        landingQueue.remove(plane);
        runwayAvailable = true;     
    }

    // Assign gate to plane
    public synchronized Gate assignGateNumber(Plane plane) {
        Gate gate = airport.findAvailableGate();
        if (gate != null) {

            // Set gate unavailable
            gate.occupy();

            System.out.println(atcName + ": Gate-" + gate.getGateNumber() + " assigned for " + plane.getName() + ".");
        }
        return gate;
    }

    // Request takeoff permission
    public synchronized void addTakeOffQueue(Plane plane) {
        takeoffQueue.add(plane);
        
        System.out.println(plane.getName() + ": Requesting takeoff.");
    }

    // Called when plane departs (frees up a slot)
    public synchronized void planeDeparted(Plane plane) {
        takeoffQueue.remove(plane);
        runwayAvailable = true;
    }
    
    // Stop ATC thread
    public void shutdown() {
        System.out.println("\n" + atcName + ": Shutting down.");
        running = false;
    }
}