package Airport_Simulation;
import java.util.*;

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
        
        // Start timer
        long startTime = System.currentTimeMillis();
        
        // Start all planes
        for (Thread t : planeThreads) {
            try {
                Random rand = new Random();
                long random = 800 + rand.nextInt(3) * 350;
                t.start();
                Thread.sleep(random);
            } 
            catch (InterruptedException e) {
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