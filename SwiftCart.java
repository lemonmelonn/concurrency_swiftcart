package SwiftCart;

// DEVAN ASOKAN - TP070977 - CCP JAVA CODE

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SwiftCart {

    public static void main(String[] args) {

        System.out.println("=============== SWIFTCART E-COMMERCE SIMULATION ================\n");
        
        // --- Shared Queues ---
        BlockingQueue<Order> pickingQueue = new LinkedBlockingQueue<>(4);
        BlockingQueue<Order> packingQueue = new LinkedBlockingQueue<>(1);
        BlockingQueue<Order> labellingQueue = new LinkedBlockingQueue<>(1);
        BlockingQueue<Order> sortingQueue = new LinkedBlockingQueue<>();
        BlockingQueue<List<Order>> containerQueue = new LinkedBlockingQueue<>(20);

        BlockingQueue<List<Order>> loadingBay1 = new LinkedBlockingQueue<>(5);
        BlockingQueue<List<Order>> loadingBay2 = new LinkedBlockingQueue<>(5);

        Semaphore loadersSemaphore = new Semaphore(3);
        AtomicInteger baySelector = new AtomicInteger(0);
        AtomicInteger truckTurn = new AtomicInteger(0);

        // --- Thread Creation ---

        // Order Intake
        Thread intakeThread = new Thread(new OrderIntake(pickingQueue), "OrderIntake-Thread");

        // Picking
        Thread[] pickerThreads = new Thread[4];
        for (int i = 0; i < pickerThreads.length; i++) {
            pickerThreads[i] = new Thread(new Picker(pickingQueue, packingQueue), "Picker-" + (i + 1));
        }

        // Packing
        Thread[] packerThreads = new Thread[2];
        for (int i = 0; i < packerThreads.length; i++) {
            packerThreads[i] = new Thread(new Packer(packingQueue, labellingQueue), "Packer-" + (i + 1));
        }

        // Labelling
        Thread[] labellerThreads = new Thread[2];
        for (int i = 0; i < labellerThreads.length; i++) {
            labellerThreads[i] = new Thread(new Labeller(labellingQueue, sortingQueue), "Labeller-" + (i + 1));
        }

        // Sorting
        Thread sorterThread = new Thread(new Sorter(sortingQueue, containerQueue), "Sorter-Thread");

        // Loading (3 AGV Loaders)
        Thread[] loaderThreads = new Thread[3];
        for (int i = 0; i < loaderThreads.length; i++) {
            loaderThreads[i] = new Thread(new Loader(
                    containerQueue,
                    loadingBay1,
                    loadingBay2,
                    loadersSemaphore,
                    baySelector,
                    "Loader-" + (i + 1)
            ), "Loader-" + (i + 1));
        }

        // Trucks (3 trucks, rotating turns)
        Thread truck1 = new Thread(new Truck(loadingBay1, loadingBay2, truckTurn, 0, "Truck-1"), "Truck-1");
        Thread truck2 = new Thread(new Truck(loadingBay1, loadingBay2, truckTurn, 1, "Truck-2"), "Truck-2");
        Thread truck3 = new Thread(new Truck(loadingBay1, loadingBay2, truckTurn, 2, "Truck-3"), "Truck-3");

        // --- Starting All Threads ---
        intakeThread.start();
        for (Thread t : pickerThreads) t.start();
        for (Thread t : packerThreads) t.start();
        for (Thread t : labellerThreads) t.start();
        sorterThread.start();
        for (Thread t : loaderThreads) t.start();
        truck1.start();
        truck2.start();
        truck3.start();

        // --- Graceful Shutdown ---
        try {
            intakeThread.join();
            Thread.sleep(30000); // Allow time for system to process remaining orders
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Interrupt threads
        for (Thread t : pickerThreads) t.interrupt();
        for (Thread t : packerThreads) t.interrupt();
        for (Thread t : labellerThreads) t.interrupt();
        sorterThread.interrupt();
        for (Thread t : loaderThreads) t.interrupt();
        truck1.interrupt();
        truck2.interrupt();
        truck3.interrupt();

        // Print final statistics
        System.out.println("\n🚩 SwiftCart Simulation Complete 🚩");
        Statistics.printFinalStatistics();
        
        System.exit(0);
    }
}
