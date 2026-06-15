import java.util.*;


class FileNode {
    int fileId;
    String fileName;
    long fileSizeInMb;
    FileNode left, right;

    public FileNode(int id, String name, long size) {
        this.fileId = id;
        this.fileName = name;
        this.fileSizeInMb = size;
        this.left = this.right = null;
    }
}

class FileCatalogTree {
    private FileNode root;

    public void insert(int id, String name, long size) {
        root = insertRec(root, id, name, size);
    }


    private FileNode insertRec(FileNode root, int id, String name, long size) {
        if (root == null) {
            return new FileNode(id, name, size);
        }
        if (id < root.fileId) {
            root.left = insertRec(root.left, id, name, size);
        } else if (id > root.fileId) {
            root.right = insertRec(root.right, id, name, size);
        }
        return root;
    }

    public void displayCatalog() {
        System.out.println("\n--- Global Torrent File Catalog (BST In-Order) ---");
        displayRec(root);
    }

    private void displayRec(FileNode root) {
        if (root != null) {
            displayRec(root.left);
            System.out.println("[File ID: " + root.fileId + "] " + root.fileName + " (" + root.fileSizeInMb + " MB)");
            displayRec(root.right);
        }
    }
}


class PeerNode {
    int peerId;
    String ipAddress;
    int bandwidthMbps; // Performance Metric
    PeerNode prev, next;

    public PeerNode(int id, String ip, int speed) {
        this.peerId = id;
        this.ipAddress = ip;
        this.bandwidthMbps = speed;
    }
}

class ConnectedPeerList {
    private PeerNode head, tail;

    public void addPeer(int id, String ip, int speed) {
        PeerNode newNode = new PeerNode(id, ip, speed);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    public void displayPeers() {
        System.out.println("\n--- Connected Network Peers (Doubly Linked List) ---");
        PeerNode current = head;
        while (current != null) {
            System.out.println("Peer #" + current.peerId + " | IP: " + current.ipAddress + " | Speed: " + current.bandwidthMbps + " Mbps");
            current = current.next;
        }
    }

    public PeerNode getHead() { return head; }
}

class ActiveDownloader implements Comparable<ActiveDownloader> {
    int peerId;
    int currentSpeed;

    public ActiveDownloader(int id, int speed) {
        this.peerId = id;
        this.currentSpeed = speed;
    }

    @Override
    public int compareTo(ActiveDownloader other) {
        // Max-Heap behavior: Highest speed download prioritized (Greedy)
        return Integer.compare(other.currentSpeed, this.currentSpeed);
    }
}

public class DistributedDownloadManager {

    private String[] systemLogs;
    private int logCount;
    private ArrayList<Integer> missingChunksList;

    private Queue<Integer> chunkDownloadQueue;
    private Stack<Integer> completedDownloadHistory;

    private HashMap<Integer, String> chunkRegistryMap;

    private PriorityQueue<ActiveDownloader> clusterPriorityQueue;

    private ConnectedPeerList peerNetwork;
    private FileCatalogTree networkCatalog;

    public DistributedDownloadManager() {
        systemLogs = new String[5]; // Fixed-size Array
        logCount = 0;
        missingChunksList = new ArrayList<>(); // Dynamic Array
        chunkDownloadQueue = new LinkedList<>(); // Standard Queue
        completedDownloadHistory = new Stack<>(); // Stack
        chunkRegistryMap = new HashMap<>(); // HashMap
        clusterPriorityQueue = new PriorityQueue<>(); // Min/Max Heap
        peerNetwork = new ConnectedPeerList();
        networkCatalog = new FileCatalogTree();
    }

    private void addLog(String message) {
        if (logCount < systemLogs.length) {
            systemLogs[logCount++] = message;
        }
    }

    public void bootstrapNetwork() {
        addLog("Network Bootstrapped");

        networkCatalog.insert(500, "Ubuntu_26.04_LTS.iso", 4600);
        networkCatalog.insert(250, "OpenSource_Database_Backup.sql", 1200);
        networkCatalog.insert(750, "BigData_Dataset.tar.gz", 15400);

        peerNetwork.addPeer(1, "192.168.1.50", 15);
        peerNetwork.addPeer(2, "10.0.0.12", 95);  // Fast node
        peerNetwork.addPeer(3, "172.16.5.88", 45); // Mid node

        for (int chunkId = 1001; chunkId <= 1006; chunkId++) {
            missingChunksList.add(chunkId);
            chunkDownloadQueue.add(chunkId); // Queueing chunks for processing
            chunkRegistryMap.put(chunkId, "PENDING"); // Mapping state in O(1)
        }

        PeerNode currentPeer = peerNetwork.getHead();
        while (currentPeer != null) {
            clusterPriorityQueue.add(new ActiveDownloader(currentPeer.peerId, currentPeer.bandwidthMbps));
            currentPeer = currentPeer.next;
        }
    }


    public void executeDistributedDownload() {
        System.out.println("\n--- Initiating Multi-Peer Parallel Swarm Download ---");
        addLog("Download Executed");

        if (clusterPriorityQueue.isEmpty() || chunkDownloadQueue.isEmpty()) return;

        while (!chunkDownloadQueue.isEmpty()) {

            int currentChunk = chunkDownloadQueue.poll();

            ActiveDownloader elitePeer = clusterPriorityQueue.peek();

            System.out.println("[Swarm Control] Extracting Chunk ID " + currentChunk +
                    " -> Routed via Cluster Peer Node #" + elitePeer.peerId +
                    " at " + elitePeer.currentSpeed + " Mbps");


            chunkRegistryMap.put(currentChunk, "COMPLETED");

            completedDownloadHistory.push(currentChunk);
        }
        System.out.println(">> File Swarm Assembly Status: Complete.");
    }

    public void rollbackLastDownloadedChunk() {

        System.out.println("\n--- Simulating User Intent: Rollback Last Action ---");
        if (!completedDownloadHistory.isEmpty()) {
            int revertedChunk = completedDownloadHistory.pop(); // Stack Pop
            chunkRegistryMap.put(revertedChunk, "ROLLBACK_PENDING"); // Map update

            chunkDownloadQueue.add(revertedChunk); // Queue Re-insertion
            System.out.println("Reverted Chunk ID: " + revertedChunk + " is flagged back to download pipeline.");
        } else {

            System.out.println("History Stack is currently clear.");
        }
    }

    public void displaySystemStates() {

        networkCatalog.displayCatalog();
        peerNetwork.displayPeers();


        System.out.println("\n--- Cluster Tracker State Map (HashMap Verification) ---");
        for (Map.Entry<Integer, String> entry : chunkRegistryMap.entrySet()) {
            System.out.println("Chunk #" + entry.getKey() + " Status: [ " + entry.getValue() + " ]");

        }

        System.out.println("\n--- Static System Event Logs (Array Data) ---");
        for (int i = 0; i < logCount; i++) {
            System.out.println("Log Event [" + i + "]: " + systemLogs[i]);

        }
    }


    public static void main(String[] args) {
        DistributedDownloadManager torrentEngine = new DistributedDownloadManager();

        torrentEngine.bootstrapNetwork();

        torrentEngine.displaySystemStates();

        torrentEngine.executeDistributedDownload();

        torrentEngine.rollbackLastDownloadedChunk();

        torrentEngine.displaySystemStates();
    }
}