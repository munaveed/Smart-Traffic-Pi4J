import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.*;
import java.util.concurrent.Semaphore;

public class Main {

    // shared Variable
    private static volatile boolean emergencyActive = false;
    private static volatile boolean pedestrianWaiting = false;

    // Semaphore for ther Critical Section
    // Mutex to ensures only one thread changes light at a given moment
    private static final Semaphore lightMutex = new Semaphore(1);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting Smart Traffic Light OS Project...");
        Context pi4j = Pi4J.newAutoContext();

        // PIN Setup 
        DigitalOutput redLight = createOutput(pi4j, 17, "RED");
        DigitalOutput yellowLight = createOutput(pi4j, 27, "YELLOW");
        DigitalOutput greenLight = createOutput(pi4j, 22, "GREEN");
        DigitalOutput pedLight = createOutput(pi4j, 6, "PED_LIGHT");
        DigitalOutput emgLight = createOutput(pi4j, 26, "EMG_LIGHT");

        DigitalInput pedButton = createInput(pi4j, 5, "PED_BTN");
        DigitalInput emgButton = createInput(pi4j, 21, "EMG_BTN");

    
        
        // Normal Traffic Cycle
        Thread trafficThread = new Thread(() -> {
            try {
                while (true) {
                    if (!emergencyActive) {
                        lightMutex.acquire(); // Enter Critical Section
                        
                        // Green Light Cycle
                        redLight.low();
                        yellowLight.low();
                        greenLight.high();
                        pedLight.low();
                        lightMutex.release(); // Leave Critical Section
                        
                        // Check for waiting predestrian
                        for(int i = 0; i < 50; i++) { 
                            if(emergencyActive) break;
                            Thread.sleep(100);
                        }

                        if (emergencyActive) continue; // Skip to emergency

                        lightMutex.acquire(); // Enter Critical Section
                        greenLight.low();
                        yellowLight.high();
                        lightMutex.release(); // Leave Critical Section
                        Thread.sleep(2000); 

                        lightMutex.acquire(); // Enter Critical Section
                        yellowLight.low();
                        redLight.high();
                        
                        // If pedestrian pushed button
                        if (pedestrianWaiting) {
                            System.out.println("Pedestrian is walking...");
                            pedLight.high();
                            Thread.sleep(4000); 
                            pedLight.low();
                            pedestrianWaiting = false;
                        } else {
                            Thread.sleep(3000); 
                        }
                        lightMutex.release(); // Leave Critical Section
                    }
                    Thread.sleep(100); //  buffer
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // Pedestrian Button Monitor
        Thread pedestrianThread = new Thread(() -> {
            while (true) {
                if (pedButton.isLow() && !emergencyActive) {
                    System.out.println("Pedestrian button pressed!");
                    pedestrianWaiting = true;
                    try { Thread.sleep(1000); } catch (InterruptedException e) {}
                }
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        });

        //Emergency Vehicle Monitor
        Thread emergencyThread = new Thread(() -> {
            while (true) {
                if (emgButton.isLow()) {
                    System.out.println("!!! EMERGENCY VEHICLE DETECTED !!!");
                    emergencyActive = true;
                    
                    try {
                        lightMutex.acquire(); // FORCE TAKE CONTROL OF LIGHTS

                        greenLight.low();
                        yellowLight.low();
                        pedLight.low();
                        redLight.high();
                        
                        // Flash emergency light 
                        for (int i = 0; i < 10; i++) {
                            emgLight.high();
                            Thread.sleep(250);
                            emgLight.low();
                            Thread.sleep(250);
                        }
                        
                        System.out.println("Emergency cleared. Resuming normal traffic.");
                        emergencyActive = false; // Reset
                        lightMutex.release(); // GIVE BACK CONTROL
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try { Thread.sleep(50); } catch (InterruptedException e) {}
            }
        });

        //  SET PRIORITIES 
        trafficThread.setPriority(Thread.NORM_PRIORITY);      // Priority 5
        pedestrianThread.setPriority(Thread.NORM_PRIORITY + 1); // Priority 6
        emergencyThread.setPriority(Thread.MAX_PRIORITY);     // Priority 10 (Highest)

        //  START THE THREADS 
        trafficThread.start();
        pedestrianThread.start();
        emergencyThread.start();

        // Keep main thread alive
        trafficThread.join();
    }

    
    private static DigitalOutput createOutput(Context pi4j, int address, String id) {
        var config = DigitalOutput.newConfigBuilder(pi4j)
                .id(id)
                .name(id)
                .address(address)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .provider("gpiod-digital-output")
                .build();
        return pi4j.create(config);
    }

    private static DigitalInput createInput(Context pi4j, int address, String id) {
        var config = DigitalInput.newConfigBuilder(pi4j)
                .id(id)
                .name(id)
                .address(address)
                .pull(PullResistance.PULL_UP) // Important for the ground  buttons
                .provider("gpiod-digital-input")
                .build();
        return pi4j.create(config);
    }
}