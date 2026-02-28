package SwiftCart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Sorter implements Runnable {
    private final BlockingQueue<Order> sortingQueue;
    private final BlockingQueue<List<Order>> containerQueue;
    private final Lock sortLock = new ReentrantLock();

    public Sorter(BlockingQueue<Order> sortingQueue, BlockingQueue<List<Order>> containerQueue) {
        this.sortingQueue = sortingQueue;
        this.containerQueue = containerQueue;
    }


    @Override
    public void run() {
        List<Order> currentBatch = new ArrayList<>();
        List<Order> currentContainer = new ArrayList<>();
        int batchId = 1;
        int containerId = 1;

        while (true) {
            try {
                Order order;
                sortLock.lock();
                try {
                    order = sortingQueue.take();
                } finally {
                    sortLock.unlock();
                }

                currentBatch.add(order);
                
                // Update terminal and statistics
                System.out.println("Sorter: Added Order #" + order.id + " to Batch #" + batchId + " (" + Thread.currentThread().getName() + ")");

                if (currentBatch.size() == 6) {
                    currentContainer.addAll(currentBatch);
                    currentBatch.clear();
                    
                    // Update terminal
                    System.out.println("Sorter: Batch #" + batchId + " completed (" + Thread.currentThread().getName() + ")");
                    Statistics.incrementBatchesCreated();
                    batchId++;
                }

                if (currentContainer.size() >= 30) {
                    
                    // Passing order to container queue
                    synchronized (containerQueue) {
                        containerQueue.put(new ArrayList<>(currentContainer));
                        containerQueue.notifyAll();
                    }
                    
                    // Update terminal
                    System.out.println("\nSorter: Container #" + containerId + " (30 orders) ready for loading (" + Thread.currentThread().getName() + ")\n");
                    currentContainer.clear();
                    containerId++;
                }

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
