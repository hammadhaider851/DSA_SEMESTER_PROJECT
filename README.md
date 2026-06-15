# DSA_SEMESTER_PROJECT

# Torrent-Like Distributed Download Manager (DSA_SEMESTER_PROJECT)

A Java-based simulation of a decentralized, peer-to-peer (P2P) file distribution engine. This project models how Torrent applications divide files into chunks, track peer health, and optimize swarm downloads using fundamental Data Structures and Algorithms (DSA) without relying on traditional Graph structures.

##  Key Features & Architecture
- **File Chunking (Divide & Conquer):** Large files are logically fragmented into discrete chunks for simultaneous downloading.
- **Greedy Peer Selection:** The system evaluates peer bandwidth dynamically and channels downloads through the fastest available cluster node.
- **State-Tracking Registry:** Instant $O(1)$ verification of download states for every chunk.
- **Transaction Rollback:** A robust tracking pipeline that allows users to undo/revert the last processed chunk back into the queue.

---

##  DSA Concepts Implemented

The architecture is meticulously engineered around specific data structures to fulfill specialized roles within a distributed ecosystem:

### 1. Data Structures
* **Binary Search Tree (BST):** Used in `FileCatalogTree`. It maintains the global indexing catalog of all shared torrent files, allowing efficient sorted insertion and catalog retrieval.
* **Custom Doubly Linked List (DLL):** Used in `ConnectedPeerList`. It manages the real-time active cluster of network peers (`PeerNode`), allowing dynamic node manipulation with pointer adjustments (`prev` and `next`).
* **Priority Queue (Max-Heap):** Used in `clusterPriorityQueue`. It organizes peers according to their bandwidth performance metrics, enforcing a **Greedy Strategy** to select optimal uploaders.
* **HashMap:** Used in `chunkRegistryMap`. It tracks and modifies the structural states (`PENDING`, `COMPLETED`, `ROLLBACK_PENDING`) of individual chunks using immediate $O(1)$ hashing.
* **Queue (FIFO):** Used in `chunkDownloadQueue`. Implements a traditional download pipeline to ensure chunks are systematically scheduled in their arrival order.
* **Stack (LIFO):** Used in `completedDownloadHistory`. Serves as a historical ledger of completed chunk downloads, facilitating flawless execution of the "Undo" (Rollback) feature.
* **Arrays & ArrayLists:** Dynamic arrays store individual missing chunk components, while standard arrays capture lower-level sequential system runtime logs.

### 2. Algorithms & Paradigms
* **Recursion:** Utilized for structural BST logic—both during binary key insertions (`insertRec`) and sorted In-Order data traversals (`displayRec`).
* **Greedy Scheduling Approach:** The engine continuously pulls metadata from the peak of the Priority Queue to dynamically allocate heavy workloads to elite network nodes first.
* **Divide and Conquer:** Simulates high-performance P2P protocols by slicing files into independent sub-problems (Chunks) which are solved (Downloaded) individually and recombined.

---

##  Complexity Analysis Matrix

| Operation / Component | Data Structure | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- |
| File Catalog Insertion | Binary Search Tree | $O(\log n)$ *(Average)* | $O(n)$ |
| Peer Swarm Traversal | Doubly Linked List | $O(n)$ | $O(1)$ |
| Chunk State Verification | HashMap | $O(1)$ | $O(n)$ |
| Best Peer Selection | Priority Queue (Heap) | $O(1)$ *(Peek)* | $O(n)$ |
| History Tracking / Pop | Stack | $O(1)$ | $O(n)$ |
| Request Scheduling | Queue | $O(1)$ | $O(n)$ |

---

##  Project Structure

```microservices
DSA_SEMESTER_PROJECT/
│
├── FileNode.java                  # Node structure for the BST Catalog
├── FileCatalogTree.java           # BST Implementation with recursive traversals
├── PeerNode.java                  # Node structure for Doubly Linked List
├── ConnectedPeerList.java         # Custom Doubly Linked List for peer pool
├── ActiveDownloader.java          # Heap element comparator logic for speed mapping
└── DistributedDownloadManager.java# Core execution driver containing Main engine
