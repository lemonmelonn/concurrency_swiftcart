# SwiftCart Concurrency Simulation

Individual assignment for "Concurrent Programming" module at APU. (Degree) <br>
A multithreaded Java simulation of **SwiftCart**, an e-commerce fulfilment centre. Orders flow through a pipeline of stations (intake, picking, packing, labelling, sorting, loading and transport), with each stage running in its own threads. The project demonstrates core concurrency concepts: producer–consumer queues, locks, semaphores, wait/notify coordination, and thread-safe statistics.

## System Specification

1. Order Intake System
   1. Orders arrive from the online platform at a rate of 1 order every 500ms.
   2. Each order is verified for payment, inventory availability, and shipping address.

2. Picking Station
   1. Robotic arms pick items from shelves and place them into order bins.
   2. Up to 4 orders can be picked at a time.
   3. Orders are verified for missing items.

3. Packing Station
   1. Completed bins are packed into shipping boxes (1 order at a time).
   2. A scanner checks each box to ensure contents match the order.

4. Labelling Station
   1. Each box is assigned a shipping label with destination and tracking.
   2. Boxes pass through a quality scanner (1 at a time).

5. Sorting Area
   1. Boxes are sorted into batches of 6 boxes based on regional zones.
   2. Batches are loaded into transport containers (30 boxes per container).

6. Loading Bay & Transport
   1. 3 autonomous loaders (AGVs) transfer containers to 2 outbound loading bays.
   2. Trucks take up to 18 containers and leave for delivery hubs.
   3. If both bays are occupied, incoming trucks must wait.

## Pipeline Overview

```
OrderIntake → Picker (x4) → Packer (x2) → Labeller (x2) → Sorter → Loader (x3) → Loading Bay 1/2 → Truck (x3)
```

Each arrow is a shared `BlockingQueue`, so a stage blocks when the next stage is full. This provides natural backpressure.

## Project Structure

| File | Role |
|---|---|
| `SwiftCart.java` | Entry point. Creates the queues and threads, starts the simulation, handles shutdown and prints final statistics. |
| `Order.java` | Simple order object (ID and rejected flag). |
| `OrderIntake.java` | Generates 600 orders, one every 500 ms, and pushes them into the picking queue. |
| `Picker.java` | Takes up to 4 orders at a time from the picking queue. Rejects ~3% for missing items. |
| `Packer.java` | Packs one order at a time. Rejects ~3% for packing errors. Pauses while a loading bay is full. |
| `Labeller.java` | Assigns a random tracking ID. Rejects ~3% as mislabelled. |
| `Sorter.java` | Groups orders into batches of 6, then into containers of 30. |
| `Loader.java` | AGV loader. Moves containers into loading bays, alternating between bays. Simulates random breakdowns. |
| `LoadingBayMonitor.java` | Shared state (lock and counters) that lets packers know when a bay is full. |
| `Truck.java` | Waits its turn, waits for a full bay, departs, then returns. |
| `Statistics.java` | Thread-safe counters and truck timing metrics. |

## Concurrency Techniques Used

- **Bounded `LinkedBlockingQueue`s** for producer–consumer hand-offs between stations. Capacities are 4 (picking), 1 (packing), 1 (labelling), 20 (containers) and 5 per loading bay.
- **`ReentrantLock`** per station to guard the critical section where a worker takes from its input queue.
- **`Semaphore(3)`** to limit the number of loaders working at once.
- **`synchronized`, `wait()` and `notifyAll()`** for coordination between packers, loaders and trucks (for example, packers pause when a bay is full).
- **Round-robin truck turns** using a shared `AtomicInteger`, so trucks depart in a fixed order.
- **Atomic counters** (`AtomicInteger`, `AtomicLong`) for lock-free statistics.
- **Thread interruption** for graceful shutdown.

## Simulation Parameters

| Parameter | Value |
|---|---|
| Total orders | 600 |
| Order arrival interval | 500 ms |
| Rejection rate per stage (picking, packing, labelling) | 3% |
| Loader breakdown chance | 5% (2 s repair) |
| Loading time per container | 500 ms |
| Truck delivery time | 2 s (plus 1 s before it is ready again) |
| Batch size / container size | 6 orders / 30 orders |
| Loading bay capacity | 5 containers each |
| Drain time after the last order | 30 s |

**Note:** In the code, trucks depart once a bay holds 5 containers, and each bay has a capacity of 5. This differs from the 18-container figure in the original specification.

## Output Statistics

The final report includes:

- Orders received, picked, packed and labelled
- Orders rejected at picking, packing and labelling
- Batches and containers created
- Trucks departed
- Minimum, maximum and average truck wait and loading times (ms)
