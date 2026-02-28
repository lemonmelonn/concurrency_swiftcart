package SwiftCart;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Packer implements Runnable {
    private final BlockingQueue<Order> packingQueue;
    private final BlockingQueue<Order> labellingQueue;
    private final Lock packLock = new ReentrantLock();

    public Packer(BlockingQueue<Order> packingQueue, BlockingQueue<Order> labellingQueue) {
        this.packingQueue = packingQueue;
        this.labellingQueue = labellingQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Pause if at least one loading bay is full
                synchronized (LoadingBayMonitor.lock) {
                    while (LoadingBayMonitor.isAnyBayFull()) {
                        System.out.println("\nPackingStation Supervisor: Waiting, at least one loading bay is full (" + Thread.currentThread().getName() + ")\n");
                        LoadingBayMonitor.lock.wait();
                    }
                }

                Order order;
                packLock.lock();
                try {
                    order = packingQueue.take();
                } finally {
                    packLock.unlock();
                }

                // Simulate packing defect
                if (Math.random() < 0.03) {
                    order.isRejected = true;
                    System.out.println("PackingStation: Order #" + order.id + " rejected (packing error) (" + Thread.currentThread().getName() + ")");
                    Statistics.incrementOrdersRejectedAtPacking();
                    continue;
                }

                // Update terminal and statistics
                System.out.println("PackingStation: Packed Order #" + order.id + " (" + Thread.currentThread().getName() + ")");
                Statistics.incrementOrdersPacked();

                // Passing order to labelling queue
                synchronized (labellingQueue) {
                    labellingQueue.put(order);
                    labellingQueue.notifyAll();
                }

            } catch (InterruptedException e) {
                break; // Thread interrupted — graceful shutdown
            }
        }
    }
}
