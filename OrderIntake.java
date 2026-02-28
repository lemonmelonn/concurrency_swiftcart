package SwiftCart;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderIntake implements Runnable {
    private static final AtomicInteger orderIdGenerator = new AtomicInteger(1);
    private final BlockingQueue<Order> pickingQueue;

    public OrderIntake(BlockingQueue<Order> pickingQueue) {
        this.pickingQueue = pickingQueue;
    }

    @Override
    public void run() {
        while (orderIdGenerator.get() <= 600) {
            try {
                Thread.sleep(500); // 1 order is generated every 500ms
                int orderId = orderIdGenerator.getAndIncrement();
                Order order = new Order(orderId);
                
                // Passing order to picking queue
                synchronized (pickingQueue) {
                    pickingQueue.put(order);
                    pickingQueue.notifyAll();
                }
                
                // Update terminal and statistics
                System.out.println("OrderIntake: Order #" + orderId + " received (" + Thread.currentThread().getName() + ")");
                Statistics.incrementOrdersReceived();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
