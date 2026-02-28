package SwiftCart;

import java.util.concurrent.atomic.AtomicInteger;

public class LoadingBayMonitor {
    public static final Object lock = new Object();  // Used for packer and loader synchronization

    public static final AtomicInteger bay1Count = new AtomicInteger(0);
    public static final AtomicInteger bay2Count = new AtomicInteger(0);

    public static boolean isAnyBayFull() {
        return bay1Count.get() >= 5 || bay2Count.get() >= 5;
    }

    public static void reset() {
        bay1Count.set(0);
        bay2Count.set(0);
    }
}

