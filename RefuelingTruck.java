package Airport_Simulation;

public class RefuelingTruck {
    private boolean available = true;
    
    public boolean isAvailable() {
        return available;
    }
    
    // Refuel plane to 100%
    public void startRefuel(Plane plane, double currentFuelLevel) throws InterruptedException {
        available = false;
        
        double fuelNeeded = 100.0 - currentFuelLevel;
        // simulate refueling time (0.05s per 1%)
        double refuelTime = fuelNeeded * 0.05;
        
        // Truck running to plane
        Thread.sleep(1000);
        
        System.out.println(plane.getName() + ": Refuelling started. \u001B[35m(" + String.format("%.2f", refuelTime) + " seconds needed)\u001B[0m");
        
        Thread.sleep((long)(refuelTime * 1000));
        
        System.out.println(plane.getName() + ": Refuelling completed.");
        
        available = true;
    }
}

class RefuelOperation implements Runnable {
    private final RefuelingTruck truck;
    private final Plane plane;
    
    public RefuelOperation(Plane plane, RefuelingTruck truck) {
        this.plane = plane;
        this.truck = truck;
    }

    @Override
    public void run() {
        try {
            truck.startRefuel(plane, plane.getFuelLevel());
            
            // Notify plane
            plane.refuelingCompleted();
            
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}