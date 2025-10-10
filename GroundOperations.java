package Airport_Simulation;

class CleaningOperation implements Runnable {
    private final String planeName;
    
    public CleaningOperation(String planeName) {
        this.planeName = planeName;
    }

    @Override
    public void run() {
        try {
            System.out.println(planeName + ": Cleaning and resupply started.");
            Thread.sleep(3000);

            System.out.println(planeName + ": Cleaning and resupply completed.");
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



class DisembarkPassengers implements Runnable { 
    private final String planeName;
    private final int numberOfPassengers;
    
    public DisembarkPassengers(String planeName, int numberOfPassengers) {
        this.planeName = planeName;
        this.numberOfPassengers = numberOfPassengers;
    }
    
    @Override
    public void run() {
        try {
            // wait passengers to disembark
            Thread.sleep(1000);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(planeName + ": All " + numberOfPassengers + " passengers disembarked.");
    }
}



class RefuelOperation implements Runnable {
    private final String planeName;
    private final double currentFuelLevel;
    private final RefuelingTruck truck;
    
    public RefuelOperation(String planeName, double fuelLevel, RefuelingTruck truck) {
        this.planeName = planeName;
        this.currentFuelLevel = fuelLevel;
        this.truck = truck;
    }
    
    @Override
    public void run() {
        try {
            truck.startRefuel(planeName, currentFuelLevel);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}



class EmbarkPassengers implements Runnable {
    private final String planeName;
    private final int numberOfPassengers;
    
    public EmbarkPassengers(String planeName, int numberOfPassengers) {
        this.planeName = planeName;
        this.numberOfPassengers = numberOfPassengers;
    }
    
    @Override
    public void run() {
       try {
            // wait passengers to embark
            Thread.sleep(1000);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println(planeName + ": All " + numberOfPassengers + " passengers boarded.");
    }
}