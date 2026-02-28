package SwiftCart;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Labeller implements Runnable {
    private final BlockingQueue<Order> labellingQueue;
    private final BlockingQueue<Order> sortingQueue;
    private final Lock labelLock = new ReentrantLock();

    public Labeller(BlockingQueue<Order> labellingQueue, BlockingQueue<Order> sortingQueue) {
        this.labellingQueue = labellingQueue;
        this.sortingQueue = sortingQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Order order;
                labelLock.lock();
                try {
                    order = labellingQueue.take();
                } finally {
                    labelLock.unlock();
                }

                // Simulate labelling defect
                if (Math.random() < 0.03) {
                    order.isRejected = true;
                    System.out.println("LabellingStation: Order #" + order.id + " rejected (mislabelled) (" + Thread.currentThread().getName() + ")");
                    Statistics.incrementOrdersRejectedAtLabelling();
                    continue;
                }

                String trackingId = UUID.randomUUID().toString().substring(0, 5);
                
                // Update terminal
                System.out.println("LabellingStation: Labelled Order #" + order.id + " with Tracking ID #" + trackingId + " (" + Thread.currentThread().getName() + ")");
                Statistics.incrementOrdersLabelled();
                
                // Passing order to sorting queue
                synchronized (sortingQueue) {
                    sortingQueue.put(order);
                    sortingQueue.notifyAll();
                }

            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
