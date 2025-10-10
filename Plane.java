package Airport_Simulation;
import java.util.Random;

public class Plane implements Runnable {
    private final String planeName;
    private final ATC atc;
    
    private final double fuelLevel;
    private final boolean isEmergency;
    
    private final Random rand = new Random();
    private final int numPassengerDisembark;
    private final int numPassengerEmbark;
    
    // Landing approval status
    private boolean waitingForLanding = false;
    private boolean canLand = false;
    private boolean waitingForTakeOff = false;
    private boolean canTakeOff = false;
    
    public Plane(int id, ATC atc) {
        this.planeName = "Plane-" + id;
        this.atc = atc;
        
        if (id == 5) {
            this.fuelLevel = 5.0 + rand.nextDouble() * 5.0;
            this.isEmergency = true;

            System.out.println(planeName + " created with fuel level: " + String.format("%.1f", fuelLevel) + "% \u001B[31m(Emergency)\u001B[0m");
        } 
        else {
            this.fuelLevel = 20.0 + rand.nextDouble() * 40.0;
            this.isEmergency = false;

            System.out.println(planeName + " created with fuel level: " + String.format("%.1f", fuelLevel) + "%");
        }
        
        this.numPassengerDisembark = 30 + rand.nextInt(21);
        this.numPassengerEmbark = 30 + rand.nextInt(21);
    }
    
    public String getName() {
        return planeName;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public double getFuelLevel() {
        return fuelLevel;
    }

    public int getnumPassengerDisembark() { 
        return numPassengerDisembark;
    }

    public int getnumPassengerEmbark() { 
        return numPassengerEmbark; 
    }

    // Just waiting, not landing yet
    public synchronized boolean waitingLandApproval() {
        return waitingForLanding && !canLand;
    }
    
    // Approved from atc
    public synchronized void availableToLand() {
        canLand = true;
    }

    // Just waiting, not takeoff yet
    public synchronized boolean waitingTakeOffApproval() {
        return waitingForTakeOff && !canTakeOff;
    }
    
    // Approved from atc
    public synchronized void approveAndStartTakeOff() {
        canTakeOff = true;
    }
    
    @Override
    public void run() {
        try {
            // First step
            requestLanding();

            // Second step
            land();

            // Find and assign gate
            Gate assignedGate = getAssignGateNumber();
            if (assignedGate == null) {
                System.out.println(planeName + ": ERROR - No gate available!");
                return;
            }

            // third step
            coastAndDock(assignedGate);

            // fourth step (3 steps concurrent + 1 step)
            performGroundOperations();

            // fifth step
            undockAndCoast(assignedGate);

            // sixth step
            requestTakeOff();

            // seventh step
            takeoff();

            // For statistical
            atc.getAirport().incrementPlanesServed();
            atc.getAirport().countPassengerServed(numPassengerDisembark);
            atc.getAirport().countPassengerBoarded(numPassengerEmbark);
            
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    private void requestLanding() throws InterruptedException {
        long waitStart = System.currentTimeMillis();
        
        // Add to queue
        atc.addLandingQueue(this);
        
        // Wait atc approve
        synchronized (this) {
            waitingForLanding = true;

            while (!canLand) {
                wait();
            }

            waitingForLanding = false;
        }
        
        long waitTime = System.currentTimeMillis() - waitStart;
        atc.getAirport().recordWaitingTime(waitTime);
    }
    
    private void land() throws InterruptedException {
        // lock runway 
        synchronized (atc.getAirport().getRunwayLock()) {
            // For statistical
            atc.getAirport().enterAirport();
            
            System.out.println(planeName + ": Landing.");
            Thread.sleep(2000);
            
            System.out.println(planeName + ": Landed.");
            Thread.sleep(1000);
            
            // Notify atc
            atc.planeLanded(this);
        }
    }
    
    private Gate getAssignGateNumber() {
        return atc.assignGateNumber(this);
    }
    
    private void coastAndDock(Gate gate) throws InterruptedException {
        System.out.println(planeName + ": Coasting to Gate-" + gate.getGateNumber() + ".");
        Thread.sleep(1000);

        System.out.println(planeName + ": Docked at Gate-" + gate.getGateNumber() + ".");
        Thread.sleep(500);
    }
    
    private void performGroundOperations() throws InterruptedException {
        Thread disembarkThread = new Thread(new DisembarkPassengers(planeName, numPassengerDisembark));
        Thread refuelThread = new Thread(new RefuelOperation(planeName, fuelLevel, atc.getAirport().getRefuelingTruck()));
        Thread cleanThread = new Thread(new CleaningOperation(planeName));
        
        disembarkThread.start();
        refuelThread.start();
        cleanThread.start();
        
        disembarkThread.join();
        cleanThread.join();
        refuelThread.join();
        
        // Wait disembark then embark
        Thread embarkThread = new Thread(new EmbarkPassengers(planeName, numPassengerEmbark));
        embarkThread.start();
        embarkThread.join();
    }

    private void undockAndCoast(Gate gate) throws InterruptedException {
        // Set gate available
        gate.release();
        System.out.println(planeName + ": Undocking from Gate-" + gate.getGateNumber() + ".");
        Thread.sleep(500);
        
        System.out.println(planeName + ": Coasting to runway.");
        Thread.sleep(1000);
    }

    private void requestTakeOff() throws InterruptedException {
        // Add to queue
        atc.addTakeOffQueue(this);
        
        // Wait atc approve
        synchronized (this) {
            waitingForTakeOff = true;

            while (!canTakeOff) {
                wait();
            }

            waitingForTakeOff = false;
        }
    }

    private void takeoff() throws InterruptedException {
        // Lock runway
        synchronized (atc.getAirport().getRunwayLock()) {

            System.out.println(planeName + ": Taking-off.");
            Thread.sleep(2000);

            System.out.println(planeName + ": Departed.");
            
            // For statistical
            atc.getAirport().exitAirport();
            atc.planeDeparted(this);
        }
    }
}