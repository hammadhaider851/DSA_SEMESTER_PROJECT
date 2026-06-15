import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        if (root == null) return new FileNode(id, name, size);
        if (id < root.fileId) root.left = insertRec(root.left, id, name, size);
        else if (id > root.fileId) root.right = insertRec(root.right, id, name, size);
        return root;
    }

    public String getCatalogString() {
        StringBuilder sb = new StringBuilder("--- Global Torrent File Catalog (BST In-Order) ---\n");
        buildCatalogString(root, sb);
        return sb.toString();
    }

    private void buildCatalogString(FileNode root, StringBuilder sb) {
        if (root != null) {
            buildCatalogString(root.left, sb);
            sb.append("[File ID: ").append(root.fileId).append("] ")
                    .append(root.fileName).append(" (").append(root.fileSizeInMb).append(" MB)\n");
            buildCatalogString(root.right, sb);
        }
    }
}

class PeerNode {
    int peerId;
    String ipAddress;
    int bandwidthMbps;
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

    public String getPeersString() {
        StringBuilder sb = new StringBuilder("--- Connected Network Peers (Doubly Linked List) ---\n");
        PeerNode current = head;
        while (current != null) {
            sb.append("Peer #").append(current.peerId).append(" | IP: ").append(current.ipAddress)
                    .append(" | Speed: ").append(current.bandwidthMbps).append(" Mbps\n");
            current = current.next;
        }
        return sb.toString();
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
        return Integer.compare(other.currentSpeed, this.currentSpeed);
    }
}

public class DistributedDownloadManager extends JFrame {

    private String[] systemLogs;
    private int logCount;
    private ArrayList<Integer> missingChunksList;
    private Queue<Integer> chunkDownloadQueue;
    private Stack<Integer> completedDownloadHistory;
    private HashMap<Integer, String> chunkRegistryMap;
    private PriorityQueue<ActiveDownloader> clusterPriorityQueue;
    private ConnectedPeerList peerNetwork;
    private FileCatalogTree networkCatalog;

    private JTextArea displayArea;
    private JTextArea logArea;
    private JButton btnBootstrap, btnDownload, btnRollback, btnState;

    public DistributedDownloadManager() {
        systemLogs = new String[10];
        logCount = 0;
        missingChunksList = new ArrayList<>();
        chunkDownloadQueue = new LinkedList<>();
        completedDownloadHistory = new Stack<>();
        chunkRegistryMap = new HashMap<>();
        clusterPriorityQueue = new PriorityQueue<>();
        peerNetwork = new ConnectedPeerList();
        networkCatalog = new FileCatalogTree();

        setTitle("Distributed Download Manager (P2P Swarm Simulator)");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollDisplay = new JScrollPane(displayArea);

        logArea = new JTextArea(8, 20);
        logArea.setEditable(false);
        logArea.setBackground(new Color(240, 240, 240));
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollLogs = new JScrollPane(logArea);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        btnBootstrap = new JButton("1. Bootstrap Network");
        btnState = new JButton("2. View System States");
        btnDownload = new JButton("3. Execute Download");
        btnRollback = new JButton("4. Rollback Last Chunk");

        btnDownload.setEnabled(false);
        btnRollback.setEnabled(false);
        btnState.setEnabled(false);

        buttonPanel.add(btnBootstrap);
        buttonPanel.add(btnState);
        buttonPanel.add(btnDownload);
        buttonPanel.add(btnRollback);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollDisplay, BorderLayout.CENTER);
        add(scrollLogs, BorderLayout.SOUTH);

        btnBootstrap.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bootstrapNetwork();
                btnBootstrap.setEnabled(false);
                btnDownload.setEnabled(true);
                btnState.setEnabled(true);
                updateLogDisplay();
                displayArea.setText("Network Successfully Bootstrapped!\nClick 'View System States' or 'Execute Download'.");
            }
        });

        btnState.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshSystemStateDisplay();
            }
        });

        btnDownload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeDistributedDownload();
                btnRollback.setEnabled(true);
                updateLogDisplay();
            }
        });

        btnRollback.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rollbackLastDownloadedChunk();
                updateLogDisplay();
            }
        });
    }

    private void addLog(String message) {
        if (logCount < systemLogs.length) {
            systemLogs[logCount++] = message;
        }
    }

    private void updateLogDisplay() {
        StringBuilder sb = new StringBuilder("--- Static System Event Logs ---\n");
        for (int i = 0; i < logCount; i++) {
            sb.append("Log Event [").append(i).append("]: ").append(systemLogs[i]).append("\n");
        }
        logArea.setText(sb.toString());
    }

    public void bootstrapNetwork() {
        addLog("Network Bootstrapped");

        networkCatalog.insert(500, "Ubuntu_26.04_LTS.iso", 4600);
        networkCatalog.insert(250, "OpenSource_Database_Backup.sql", 1200);
        networkCatalog.insert(750, "BigData_Dataset.tar.gz", 15400);

        peerNetwork.addPeer(1, "192.168.1.50", 15);
        peerNetwork.addPeer(2, "10.0.0.12", 95);
        peerNetwork.addPeer(3, "172.16.5.88", 45);

        missingChunksList.clear();
        chunkDownloadQueue.clear();
        chunkRegistryMap.clear();
        clusterPriorityQueue.clear();

        for (int chunkId = 1001; chunkId <= 1006; chunkId++) {
            missingChunksList.add(chunkId);
            chunkDownloadQueue.add(chunkId);
            chunkRegistryMap.put(chunkId, "PENDING");
        }

        PeerNode currentPeer = peerNetwork.getHead();
        while (currentPeer != null) {
            clusterPriorityQueue.add(new ActiveDownloader(currentPeer.peerId, currentPeer.bandwidthMbps));
            currentPeer = currentPeer.next;
        }
    }

    public void executeDistributedDownload() {
        StringBuilder sb = new StringBuilder("--- Initiating Multi-Peer Parallel Swarm Download ---\n");
        addLog("Download Executed");

        if (clusterPriorityQueue.isEmpty() || chunkDownloadQueue.isEmpty()) {
            displayArea.setText("No chunks available to download or no peers found.");
            return;
        }

        while (!chunkDownloadQueue.isEmpty()) {
            int currentChunk = chunkDownloadQueue.poll();
            ActiveDownloader elitePeer = clusterPriorityQueue.peek();

            sb.append("[Swarm Control] Extracting Chunk ID ").append(currentChunk)
                    .append(" -> Routed via Cluster Peer Node #").append(elitePeer.peerId)
                    .append(" at ").append(elitePeer.currentSpeed).append(" Mbps\n");

            chunkRegistryMap.put(currentChunk, "COMPLETED");
            completedDownloadHistory.push(currentChunk);
        }
        sb.append(">> File Swarm Assembly Status: Complete.\n\n");
        sb.append("Click 'View System States' to see updated chunk statuses.");
        displayArea.setText(sb.toString());
    }

    public void rollbackLastDownloadedChunk() {
        StringBuilder sb = new StringBuilder("--- Simulating User Intent: Rollback Last Action ---\n");
        if (!completedDownloadHistory.isEmpty()) {
            int revertedChunk = completedDownloadHistory.pop();
            chunkRegistryMap.put(revertedChunk, "ROLLBACK_PENDING");
            chunkDownloadQueue.add(revertedChunk);

            sb.append("Reverted Chunk ID: ").append(revertedChunk).append(" is flagged back to download pipeline.\n\n");
            sb.append("Click 'Execute Download' to re-download or 'View System States' to check status.");
            addLog("Rollback Chunk #" + revertedChunk);
        } else {
            sb.append("History Stack is currently clear. No chunks to rollback.");
        }
        displayArea.setText(sb.toString());
    }

    public void refreshSystemStateDisplay() {
        StringBuilder sb = new StringBuilder();

        sb.append(networkCatalog.getCatalogString()).append("\n");

        sb.append(peerNetwork.getPeersString()).append("\n");

        sb.append("--- Cluster Tracker State Map (HashMap Verification) ---\n");
        ArrayList<Integer> sortedKeys = new ArrayList<>(chunkRegistryMap.keySet());
        Collections.sort(sortedKeys);
        for (int key : sortedKeys) {
            sb.append("Chunk #").append(key).append(" Status: [ ").append(chunkRegistryMap.get(key)).append(" ]\n");
        }

        displayArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DistributedDownloadManager().setVisible(true);
            }
        });
    }
}
