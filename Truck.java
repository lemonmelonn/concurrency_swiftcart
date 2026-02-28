package SwiftCart;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Truck implements Runnable {
    private final BlockingQueue<List<Order>> bay1;
    private final BlockingQueue<List<Order>> bay2;
    private final AtomicInteger truckTurn;
    private final int truckId;
    private final String truckName;

    public Truck(BlockingQueue<List<Order>> bay1, BlockingQueue<List<Order>> bay2,
                 AtomicInteger truckTurn, int truckId, String truckName) {
        this.bay1 = bay1;
        this.bay2 = bay2;
        this.truckTurn = truckTurn;
        this.truckId = truckId;
        this.truckName = truckName;

        // Notify on the terminal once a truck has been created
        System.out.println(truckName + " initialized and ready for loading.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                long waitStart = System.currentTimeMillis();
                BlockingQueue<List<Order>> currentBay;

                // Wait for turn
                synchronized (truckTurn) {
                    while (truckTurn.get() != truckId) {
                        truckTurn.wait();
                    }
                    currentBay = (truckId % 2 == 0) ? bay1 : bay2;
                }

                // Wait for 5 containers
                synchronized (currentBay) {
                    while (currentBay.size() < 5) {
                        System.out.println("\n" + truckName + ": Waiting for 5 containers at Bay " +
                                (currentBay == bay1 ? "1" : "2") + ". Current: " + currentBay.size());
                        currentBay.wait();
                    }
                }

                long waitEnd = System.currentTimeMillis();
                long waitDuration = waitEnd - waitStart;

                System.out.println("\n" + truckName + ": Fully loaded with 5 containers from Bay " +
                        (currentBay == bay1 ? "1" : "2") + ". Departing to Distribution Centre...\n");

                long loadStart = System.currentTimeMillis();
                Thread.sleep(2000); // Simulate delivery time

                // Remove 5 containers from bay
                for (int i = 0; i < 5; i++) {
                    currentBay.take();
                }

                long loadEnd = System.currentTimeMillis();
                long loadingDuration = loadEnd - loadStart;

                // Update statistics
                Statistics.incrementTrucksDeparted();
                Statistics.recordTruckTimings(waitDuration, loadingDuration);

                // Update bay count in LoadingBayMonitor
                synchronized (LoadingBayMonitor.lock) {
                    if (currentBay == bay1) {
                        LoadingBayMonitor.bay1Count.addAndGet(-5);
                    } else {
                        LoadingBayMonitor.bay2Count.addAndGet(-5);
                    }
                    LoadingBayMonitor.lock.notifyAll(); // Wake up any waiting packers
                }

                // Wake up any waiting loaders
                synchronized (currentBay) {
                    currentBay.notifyAll();
                }

                // Move to next truck's turn
                synchronized (truckTurn) {
                    truckTurn.set((truckTurn.get() + 1) % 3);
                    truckTurn.notifyAll();
                }

                // Update terminal that truck has returned from Distribution Centre
                System.out.println("\n" + truckName + ": Returned empty. Ready again.\n");

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
