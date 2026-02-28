package SwiftCart;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Loader implements Runnable {
    private final BlockingQueue<List<Order>> containerQueue;
    private final BlockingQueue<List<Order>> loadingBay1;
    private final BlockingQueue<List<Order>> loadingBay2;
    private final Semaphore loaderSemaphore;
    private final AtomicInteger baySelector;
    private final String loaderName;

    public Loader(BlockingQueue<List<Order>> containerQueue,
                  BlockingQueue<List<Order>> loadingBay1,
                  BlockingQueue<List<Order>> loadingBay2,
                  Semaphore loaderSemaphore,
                  AtomicInteger baySelector,
                  String loaderName) {
        this.containerQueue = containerQueue;
        this.loadingBay1 = loadingBay1;
        this.loadingBay2 = loadingBay2;
        this.loaderSemaphore = loaderSemaphore;
        this.baySelector = baySelector;
        this.loaderName = loaderName;

        // Notify on the terminal once a loader has been created
        System.out.println(loaderName + " initialized and waiting for containers.");
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Simulate loader breakdowns
                if (Math.random() < 0.05) {
                    System.out.println("\n" + loaderName + ": Breakdown occurred. Repairing...\n");
                    Thread.sleep(2000);
                    System.out.println("\n" + loaderName + ": Repair completed. Resuming.\n");
                }

                loaderSemaphore.acquire();

                List<Order> container = containerQueue.take();

                BlockingQueue<List<Order>> currentBay;
                boolean isBay1;

                // Choose loading bay
                synchronized (baySelector) {
                    isBay1 = baySelector.get() % 2 == 0;
                    currentBay = isBay1 ? loadingBay1 : loadingBay2;
                    baySelector.set((baySelector.get() + 1) % 2);
                }

                synchronized (currentBay) {
                    while (currentBay.remainingCapacity() == 0) {
                        System.out.println(loaderName + ": Loading Bay " + (isBay1 ? "1" : "2") + " is full. Waiting...");
                        currentBay.wait();
                    }

                    currentBay.put(container);

                    // Update bay count using LoadingBayMonitor
                    synchronized (LoadingBayMonitor.lock) {
                        if (isBay1) {
                            LoadingBayMonitor.bay1Count.incrementAndGet();
                        } else {
                            LoadingBayMonitor.bay2Count.incrementAndGet();
                        }
                        // Notify packers in case they are waiting
                        LoadingBayMonitor.lock.notifyAll();
                    }

                    // Update terminal and statistics
                    Statistics.incrementContainersCreated();
                    System.out.println(loaderName + ": Loaded container into Loading Bay " +
                            (isBay1 ? "1" : "2") + " (" + currentBay.size() + "/5)");

                    currentBay.notifyAll(); // Wake trucks
                }

                loaderSemaphore.release();
                Thread.sleep(500); // Simulate loading time

            } catch (InterruptedException e) {
                break; // Graceful exit
            }
        }
    }
}
