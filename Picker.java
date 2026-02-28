package SwiftCart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Picker implements Runnable {
    private final BlockingQueue<Order> pickingQueue;
    private final BlockingQueue<Order> packingQueue;
    private final Lock pickLock = new ReentrantLock();

    public Picker(BlockingQueue<Order> pickingQueue, BlockingQueue<Order> packingQueue) {
        this.pickingQueue = pickingQueue;
        this.packingQueue = packingQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                List<Order> batch = new ArrayList<>();

                pickLock.lock();
                try {
                    while (batch.size() < 4 && !pickingQueue.isEmpty()) {
                        Order order = pickingQueue.take();
                        batch.add(order);
                    }
                } finally {
                    pickLock.unlock();
                }

                for (Order order : batch) {
                    // Simulate item picking and defect rejection
                    if (Math.random() < 0.03) {
                        order.isRejected = true;
                        System.out.println("PickingStation: Order #" + order.id + " rejected (missing items) (" + Thread.currentThread().getName() + ")");
                        Statistics.incrementOrdersRejectedPicker();
                        continue;
                    }

                    // Update terminal and statistics
                    System.out.println("PickingStation: Picking Order #" + order.id + " (" + Thread.currentThread().getName() + ")");
                    Statistics.incrementOrdersPicked();

                    // Passing order to packing queue
                    synchronized (packingQueue) {
                        packingQueue.put(order);
                        packingQueue.notifyAll();
                    }
                }

            } catch (InterruptedException e) {
                break; // Graceful shutdown
            }
        }
    }
}
