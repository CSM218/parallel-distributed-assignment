package pdc;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class ReferenceMaster {
    private int masterPort;
    private ServerSocket serverSocket;
    private List<WorkerConnection> workers = new CopyOnWriteArrayList<>();
    private String runtimeToken;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Map<String, TaskResult> results = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_TIMEOUT = 5000;
    private static final long TASK_TIMEOUT = 30000;
    
    public ReferenceMaster(int port) throws IOException {
        this.masterPort = port;
        this.serverSocket = new ServerSocket(port);
        this.runtimeToken = generateToken();
        System.out.println("[CSM218-MASTER] Starting on port " + port);
    }
    
    private String generateToken() {
        return "CSM218_TOKEN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    public void start() throws Exception {
        Thread acceptThread = new Thread(() -> {
            try {
                while (true) {
                    Socket socket = serverSocket.accept();
                    executorService.submit(() -> handleClientConnection(socket));
                }
            } catch (SocketException e) {
                // Server shutdown
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        acceptThread.setDaemon(true);
        acceptThread.start();
        
        startHeartbeatMonitor();
    }
    
    private void handleClientConnection(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            
            String greeting = reader.readLine();
            Message greetMsg = Message.parse(greeting);
            greetMsg.validate();
            
            if ("REGISTER_WORKER".equals(greetMsg.messageType)) {
                String workerId = greetMsg.payload;
                WorkerConnection wc = new WorkerConnection(workerId, socket, reader, writer);
                workers.add(wc);
                
                Message ack = new Message();
                ack.messageType = "WORKER_ACK";
                ack.payload = runtimeToken;
                writer.println(ack.toJson());
                
                startWorkerHeartbeat(wc);
                
                System.out.println("[CSM218-MASTER] Worker registered: " + workerId);
                
                String line;
                while ((line = reader.readLine()) != null) {
                    handleWorkerMessage(wc, Message.parse(line));
                }
            }
        } catch (Exception e) {
            System.err.println("[CSM218-MASTER] Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    private void handleWorkerMessage(WorkerConnection wc, Message msg) throws Exception {
        msg.validate();
        
        if ("HEARTBEAT".equals(msg.messageType)) {
            wc.lastHeartbeat = System.currentTimeMillis();
        } else if ("TASK_COMPLETE".equals(msg.messageType)) {
            String taskId = msg.payload.split(";")[0];
            String result = msg.payload.split(";")[1];
            results.put(taskId, new TaskResult(taskId, result, wc.workerId));
            System.out.println("[CSM218-MASTER] Task complete: " + taskId + " from " + wc.workerId);
        } else if ("TASK_ERROR".equals(msg.messageType)) {
            String taskId = msg.payload.split(";")[0];
            String error = msg.payload.substring(taskId.length() + 1);
            results.put(taskId, new TaskResult(taskId, null, wc.workerId, error));
            System.out.println("[CSM218-MASTER] Task error: " + taskId + " - " + error);
        }
    }
    
    private void startWorkerHeartbeat(WorkerConnection wc) {
        wc.lastHeartbeat = System.currentTimeMillis();
        
        Thread hbThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    
                    Message hb = new Message();
                    hb.messageType = "HEARTBEAT";
                    hb.payload = "PING";
                    wc.writer.println(hb.toJson());
                } catch (Exception e) {
                    break;
                }
            }
        });
        hbThread.setDaemon(true);
        hbThread.start();
    }
    
    private void startHeartbeatMonitor() {
        Thread monitor = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    long now = System.currentTimeMillis();
                    
                    for (WorkerConnection wc : workers) {
                        if (now - wc.lastHeartbeat > HEARTBEAT_TIMEOUT) {
                            System.out.println("[CSM218-MASTER] Worker failed: " + wc.workerId);
                            workers.remove(wc);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        monitor.setDaemon(true);
        monitor.start();
    }
    
    public String executeTask(String taskId, String taskType, String payload) throws Exception {
        if (workers.isEmpty()) {
            throw new Exception("No workers available");
        }
        
        WorkerConnection worker = workers.get(taskId.hashCode() % workers.size());
        
        Message task = new Message();
        task.messageType = "RPC_REQUEST";
        task.payload = taskId + ";" + taskType + ";" + payload + ";" + runtimeToken;
        
        System.out.println("[CSM218-MASTER] Sending task " + taskId + " to " + worker.workerId);
        worker.writer.println(task.toJson());
        
        long startTime = System.currentTimeMillis();
        while (!results.containsKey(taskId)) {
            if (System.currentTimeMillis() - startTime > TASK_TIMEOUT) {
                throw new Exception("Task timeout: " + taskId);
            }
            Thread.sleep(100);
        }
        
        TaskResult tr = results.get(taskId);
        results.remove(taskId);
        
        if (tr.error != null) {
            throw new Exception(tr.error);
        }
        
        return tr.result;
    }
    
    public int getWorkerCount() {
        return workers.size();
    }
    
    public String getRuntimeToken() {
        return runtimeToken;
    }
    
    public void shutdown() throws IOException {
        serverSocket.close();
        executorService.shutdownNow();
    }
    
    private static class WorkerConnection {
        String workerId;
        Socket socket;
        BufferedReader reader;
        PrintWriter writer;
        long lastHeartbeat;
        
        WorkerConnection(String workerId, Socket socket, BufferedReader reader, PrintWriter writer) {
            this.workerId = workerId;
            this.socket = socket;
            this.reader = reader;
            this.writer = writer;
            this.lastHeartbeat = System.currentTimeMillis();
        }
    }
    
    private static class TaskResult {
        String taskId;
        String result;
        String workerId;
        String error;
        
        TaskResult(String taskId, String result, String workerId) {
            this.taskId = taskId;
            this.result = result;
            this.workerId = workerId;
        }
        
        TaskResult(String taskId, String result, String workerId, String error) {
            this.taskId = taskId;
            this.result = result;
            this.workerId = workerId;
            this.error = error;
        }
    }
    
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("MASTER_PORT", "9999"));
        ReferenceMaster master = new ReferenceMaster(port);
        master.start();
        
        Thread.sleep(Long.MAX_VALUE);
    }
}
