package Airport_Simulation;

class Gate {
    private final int gateNumber;
    private boolean isAvailable = true;
    
    public Gate(int number) {
        this.gateNumber = number;
    }
    
    public synchronized void occupy() {
        isAvailable = false;
    }
    
    public synchronized void release() {
        isAvailable = true;
    }
    
    public synchronized boolean isAvailable() {
        return isAvailable;
    }
    
    public int getGateNumber() {
        return gateNumber;
    }
}

class RefuelingTruck {
    // Ensure only one truck available
    private final Object truckLock = new Object();
    
    // Refuel plane to 100%
    public void startRefuel(String planeName, double currentFuelLevel) throws InterruptedException {
        // lock truck
        synchronized (truckLock) {
            double fuelNeeded = 100.0 - currentFuelLevel;

            // simulate refueling time (0.05s per 1%)
            double refuelTime = fuelNeeded * 0.05; 
            
            System.out.println("Fuel-Truck: Assigned to " + planeName + ".");

            try {
                Thread.sleep(1000);
            } 
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println(planeName + ": Refuelling started. \u001B[35m(" + String.format("%.2f",refuelTime) + " seconds needed)\u001B[0m");
            
            // Refueling time based on fuel needed
            Thread.sleep((long)(refuelTime * 1000));
            
            System.out.println(planeName + ": Refuelling completed.");
        }
    }
}