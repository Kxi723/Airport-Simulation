package Airport_Simulation;
import java.util.*;

public class ATC implements Runnable {
    private final String atcName;
    private final APAirport airport;
    private final int MAX_CAPACITY = 3;
    
    // Queue for planes waiting to land & takeoff & refuel
    private final LinkedList<Plane> landingQueue = new LinkedList<>();
    private final LinkedList<Plane> takeoffQueue = new LinkedList<>();
    private final LinkedList<Plane> refuelingQueue = new LinkedList<>();
    
    // Runway control
    private boolean runwayAvailable = true;

    // Atc thread running status
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

                processRefuelingRequests();
                Thread.sleep(10); // Loop every 0.01s
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Assign truck
    private synchronized void processRefuelingRequests() {

        // Plane waiting for refuel
        if (!refuelingQueue.isEmpty()) {
            
            RefuelingTruck truck = airport.getRefuelingTruck();

            // If truck is available
            if (truck.isAvailable()) {
                Plane nextPlane = refuelingQueue.peek(); // Get first plane
                
                // If plane havent refuel yet
                if (nextPlane.waitingForRefueling()) {
                    // Dispatch truck
                    refuelingQueue.removeFirst();
                    
                    System.out.println("ATC: Refueling truck assigned to " + nextPlane.getName() + ".");
                    
                    RefuelOperation refuelTask = new RefuelOperation(nextPlane, truck);
                    Thread refuelThread = new Thread(refuelTask, "Thread-Refuel");
                    
                    refuelThread.start();
                }
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
                
                System.out.println(atcName + ": Takeoff granted for " + nextPlane.getName() + ".");
                
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
            System.out.println(atcName + ": " + planeName + " has been allowed to cut the queue.");
        }
        else { // Adding plane in queue
            landingQueue.add(plane);
            System.out.println(atcName + ": " + planeName + " has been added in the queue.");
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

    // Plane requests refueling
    public synchronized void requestRefueling(Plane plane) {
        System.out.println(plane.getName() + ": Requesting refueling.");

        refuelingQueue.add(plane);
    }

    // Request takeoff permission
    public synchronized void addTakeOffQueue(Plane plane) {
        takeoffQueue.add(plane);
        
        System.out.println(plane.getName() + ": Requesting take-off.");
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