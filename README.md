# concurrency_swiftcart

1. Order Intake System
a. Orders arrive from the online platform at a rate of 1 order every 500ms.
b. Each order is verified for payment, inventory availability, and shipping address.


2. Picking Station
a. Robotic arms pick items from shelves and place them into order bins.
b. Up to 4 orders can be picked at a time.
c. Orders are verified for missing items.


3. Packing Station
a. Completed bins are packed into shipping boxes (1 order at a time).
b. A scanner checks each box to ensure contents match the order.


4. Labelling Station
a. Each box is assigned a shipping label with destination and tracking.
b. Boxes pass through a quality scanner (1 at a time).


5. Sorting Area
a. Boxes are sorted into batches of 6 boxes based on regional zones.
b. Batches are loaded into transport containers (30 boxes per container).


6. Loading Bay & Transport
a. 3 autonomous loaders (AGVs) transfer containers to 2 outbound loading bays.
b. Trucks take up to 18 containers and leave for delivery hubs.
c. If both bays are occupied, incoming trucks must wait.
