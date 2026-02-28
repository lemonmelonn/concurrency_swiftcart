package SwiftCart;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
    private static final AtomicInteger ordersReceived = new AtomicInteger(0);
    private static final AtomicInteger ordersRejectedPicker = new AtomicInteger(0);
    private static final AtomicInteger ordersPicked = new AtomicInteger(0);
    private static final AtomicInteger ordersPacked = new AtomicInteger(0);
    private static final AtomicInteger ordersRejectedAtPacking = new AtomicInteger(0);
    private static final AtomicInteger ordersRejectedAtLabelling = new AtomicInteger(0);
    private static final AtomicInteger ordersLabelled = new AtomicInteger(0);
    private static final AtomicInteger batchesCreated = new AtomicInteger(0);
    private static final AtomicInteger containersCreated = new AtomicInteger(0);
    private static final AtomicInteger trucksDeparted = new AtomicInteger(0);

    private static final AtomicLong totalWaitTime = new AtomicLong(0);
    private static final AtomicLong totalLoadingTime = new AtomicLong(0);
    private static final AtomicLong maxWaitTime = new AtomicLong(0);
    private static final AtomicLong minWaitTime = new AtomicLong(Long.MAX_VALUE);
    private static final AtomicLong maxLoadingTime = new AtomicLong(0);
    private static final AtomicLong minLoadingTime = new AtomicLong(Long.MAX_VALUE);
    private static final AtomicInteger trucksMeasured = new AtomicInteger(0);

    public static volatile boolean processingComplete = false;

    // Increment methods
    public static void incrementOrdersReceived() {
        ordersReceived.incrementAndGet();
    }
    
    public static void incrementOrdersPicked() { 
        ordersPicked.incrementAndGet(); 
    }
    
    public static void incrementOrdersRejectedPicker() { 
        ordersRejectedPicker.incrementAndGet(); 
    }
    
    public static void incrementOrdersPacked() { 
        ordersPacked.incrementAndGet(); 
    }
    
    public static void incrementOrdersRejectedAtPacking() {
        ordersRejectedAtPacking.incrementAndGet();
    }
    
    public static void incrementOrdersLabelled() { 
        ordersLabelled.incrementAndGet(); 
    }
    
    public static void incrementOrdersRejectedAtLabelling() {
        ordersRejectedAtLabelling.incrementAndGet();
    }
    
    public static void incrementBatchesCreated() { 
        batchesCreated.incrementAndGet(); 
    }
    
    public static void incrementContainersCreated() { 
        containersCreated.incrementAndGet(); 
    }
    
    public static void incrementTrucksDeparted() { 
        trucksDeparted.incrementAndGet(); 
    }

    // Record truck timing statistics
    public static synchronized void recordTruckTimings(long waitTimeMillis, long loadingTimeMillis) {
        totalWaitTime.addAndGet(waitTimeMillis);
        totalLoadingTime.addAndGet(loadingTimeMillis);

        maxWaitTime.updateAndGet(prev -> Math.max(prev, waitTimeMillis));
        minWaitTime.updateAndGet(prev -> Math.min(prev, waitTimeMillis));

        maxLoadingTime.updateAndGet(prev -> Math.max(prev, loadingTimeMillis));
        minLoadingTime.updateAndGet(prev -> Math.min(prev, loadingTimeMillis));

        trucksMeasured.incrementAndGet();
    }

    // Final reporting
    public static void printFinalStatistics() {
        System.out.println("\n================= SWIFTCART STATISTICS ==================");
        System.out.println("Total Orders Received:               " + ordersReceived.get());
        System.out.println("Total Orders Picked:                 " + ordersPicked.get());
        System.out.println("Total Orders Rejected at Picking:    " + ordersRejectedPicker.get());
        System.out.println("Total Orders Packed:                 " + ordersPacked.get());
        System.out.println("Total Orders Rejected at Packing:    " + ordersRejectedAtPacking.get());
        System.out.println("Total Orders Labelled:               " + ordersLabelled.get());
        System.out.println("Total Orders Rejected at Labelling:  " + ordersRejectedAtLabelling.get());
        System.out.println("Total Batches Created:               " + batchesCreated.get());
        System.out.println("Total Containers Created:            " + containersCreated.get());
        System.out.println("Total Trucks Departed:               " + trucksDeparted.get());

        int trucks = trucksMeasured.get();
        if (trucks > 0) {
            long avgWait = totalWaitTime.get() / trucks;
            long avgLoading = totalLoadingTime.get() / trucks;

            long minWait = minWaitTime.get() == Long.MAX_VALUE ? 0 : minWaitTime.get();
            long minLoading = minLoadingTime.get() == Long.MAX_VALUE ? 0 : minLoadingTime.get();

            System.out.println("\nTruck Wait/Loading Times (ms):");
            System.out.println("Minimum Wait Time:    " + minWait);
            System.out.println("Maximum Wait Time:    " + maxWaitTime.get());
            System.out.println("Average Wait Time:    " + avgWait);
            System.out.println("Minimum Loading Time: " + minLoading);
            System.out.println("Maximum Loading Time: " + maxLoadingTime.get());
            System.out.println("Average Loading Time: " + avgLoading);
        } else {
            System.out.println("\nTruck Wait/Loading Times (ms):");
            System.out.println("No trucks departed. No timing data recorded.");
        }
        System.out.println("=========================================================\n");
    }
}
