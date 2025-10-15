package Airport_Simulation;

public class Gate {
    private final int gateNumber;
    private boolean isAvailable = true;

    private boolean cleaningCompleted = false;
    private boolean embarkCompleted = false;
    
    public Gate(int number) {
        this.gateNumber = number;
    }
    
    public int getGateNumber() {
        return gateNumber;
    }
    
    // Lock gate
    public synchronized void occupy() {
        isAvailable = false;
    }
    
    // Release gate & reset to default settings
    public synchronized void release() {
        isAvailable = true;
        cleaningCompleted = false;
        embarkCompleted = false;
    }
    
    // Check can assign not
    public synchronized boolean isAvailable() {
        return isAvailable;
    }  

    // Check ready for take off not
    public synchronized boolean operationCompleted() {
        return cleaningCompleted && embarkCompleted;
    }
    
    // Start operation
    public void openGate(Plane plane) {
        DisembarkOperation disembarkTask = new DisembarkOperation(this, plane);
        Thread disembarkThread = new Thread(disembarkTask, "Thread-Disembark");
        
        CleaningOperation cleaningTask = new CleaningOperation(this, plane);
        Thread cleaningThread = new Thread(cleaningTask, "Thread-Cleaning");
        
        // Start concurrently, refuel start at another side
        disembarkThread.start();
        cleaningThread.start();

        System.out.println(plane.getName() + ": Start cleaning and resupply.");
    }
    
    public void disembarkPassengers(Plane plane) throws InterruptedException {
        // Disembark take 1 second 
        Thread.sleep(1000);
        
        synchronized (this) {
            System.out.println(plane.getName() + ": All " + plane.getnumPassengerDisembark() + " passengers disembarked.");

            // Finish disembark, start embark passengers
            EmbarkOperation embarkTask = new EmbarkOperation(this, plane);
            Thread embarkThread = new Thread(embarkTask, "Thread-Embark");

            embarkThread.start();
        }
    }
    
    public void cleanPlane(Plane plane) throws InterruptedException {
        // Cleaning takes 2 seconds
        Thread.sleep(2000);
        
        synchronized (this) {
            cleaningCompleted = true;
            System.out.println(plane.getName() + ": Cleaning and resupply completed.");
        }
    }
    
    public void embarkPassengers(Plane plane) throws InterruptedException {
         // Embark takes 1 second
        Thread.sleep(1000);
        
        synchronized (this) {
            embarkCompleted = true;
            System.out.println(plane.getName() + ": All " + plane.getnumPassengerEmbark() + " passengers embarked.");
        }
    }
}

class DisembarkOperation implements Runnable {
    private final Gate gate;
    private final Plane plane;
    
    public DisembarkOperation(Gate gate, Plane plane) {
        this.gate = gate;
        this.plane = plane;
    }
    
    @Override
    public void run() {
        try {
            gate.disembarkPassengers(plane);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class CleaningOperation implements Runnable {
    private final Gate gate;
    private final Plane plane;
    
    public CleaningOperation(Gate gate, Plane plane) {
        this.gate = gate;
        this.plane = plane;
    }

    @Override
    public void run() {
        try {
            gate.cleanPlane(plane);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class EmbarkOperation implements Runnable {
    private final Gate gate;
    private final Plane plane;
    
    public EmbarkOperation(Gate gate, Plane plane) {
        this.gate = gate;
        this.plane = plane;
    }
    
    @Override
    public void run() {
        try {
            gate.embarkPassengers(plane);
        } 
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}