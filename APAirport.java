package Airport_Simulation;
import java.util.concurrent.atomic.AtomicInteger;

public class APAirport {
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