package pdc;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class ReferenceWorker {
    private String workerId;
    private String masterHost;
    private int masterPort;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String runtimeToken;
    private ExecutorService taskPool = Executors.newFixedThreadPool(3);
    
    public ReferenceWorker(String workerId, String masterHost, int masterPort) {
        this.workerId = workerId;
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }
    
    public void connect() throws Exception {
        socket = new Socket(masterHost, masterPort);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        
        Message register = new Message();
        register.messageType = "REGISTER_WORKER";
        register.payload = workerId;
        writer.println(register.toJson());
        
        String ackLine = reader.readLine();
        Message ack = Message.parse(ackLine);
        ack.validate();
        
        if ("WORKER_ACK".equals(ack.messageType)) {
            this.runtimeToken = ack.payload;
            System.out.println("[CSM218-WORKER] " + workerId + " Connected. Token received.");
        }
        
        startMessageListener();
        startHeartbeatSender();
    }
    
    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Message msg = Message.parse(line);
                    msg.validate();
                    
                    if ("HEARTBEAT".equals(msg.messageType)) {
                        Message hbAck = new Message();
                        hbAck.messageType = "HEARTBEAT";
                        hbAck.payload = "ACK";
                        writer.println(hbAck.toJson());
                    } else if ("RPC_REQUEST".equals(msg.messageType)) {
                        taskPool.submit(() -> handleRpcRequest(msg.payload));
                    }
                }
            } catch (SocketException e) {
                // Connection closed
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }
    
    private void handleRpcRequest(String payload) {
        try {
            String[] parts = payload.split(";");
            String taskId = parts[0];
            String taskType = parts[1];
            String data = parts[2];
            String token = parts[3];
            
            if (!runtimeToken.equals(token)) {
                sendError(taskId, "Invalid token");
                return;
            }
            
            System.out.println("[CSM218-WORKER] " + workerId + " START " + taskId);
            
            String result = executeTask(taskType, data);
            
            Message response = new Message();
            response.messageType = "TASK_COMPLETE";
            response.payload = taskId + ";" + result;
            writer.println(response.toJson());
            
            System.out.println("[CSM218-WORKER] " + workerId + " END " + taskId);
            
        } catch (Exception e) {
            try {
                String taskId = payload.split(";")[0];
                sendError(taskId, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    private String executeTask(String taskType, String data) throws Exception {
        if ("MATRIX_MULTIPLY".equals(taskType)) {
            return handleMatrixMultiply(data);
        } else if ("BLOCK_TRANSPOSE".equals(taskType)) {
            return handleBlockTranspose(data);
        } else {
            throw new Exception("Unknown task type: " + taskType);
        }
    }
    
    private String handleMatrixMultiply(String data) throws Exception {
        // Simulate heavy computation
        Thread.sleep(100 + (int)(Math.random() * 200));
        
        String[] parts = data.split("\\|");
        if (parts.length < 2) {
            throw new Exception("Invalid matrix data");
        }
        
        int[][] a = parseMatrix(parts[0]);
        int[][] b = parseMatrix(parts[1]);
        
        int[][] result = multiplyMatrices(a, b);
        return matrixToString(result);
    }
    
    private String handleBlockTranspose(String data) throws Exception {
        Thread.sleep(100 + (int)(Math.random() * 200));
        
        int[][] matrix = parseMatrix(data);
        int[][] result = transposeMatrix(matrix);
        return matrixToString(result);
    }
    
    private int[][] multiplyMatrices(int[][] a, int[][] b) {
        int[][] c = new int[a.length][b[0].length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b[0].length; j++) {
                for (int k = 0; k < a[0].length; k++) {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return c;
    }
    
    private int[][] transposeMatrix(int[][] matrix) {
        int[][] result = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }
    
    private int[][] parseMatrix(String str) throws Exception {
        String[] rows = str.split("\\\\");
        int[][] matrix = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            String[] cols = rows[i].split(",");
            matrix[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                matrix[i][j] = Integer.parseInt(cols[j].trim());
            }
        }
        return matrix;
    }
    
    private String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matrix.length; i++) {
            if (i > 0) sb.append("\\");
            for (int j = 0; j < matrix[i].length; j++) {
                if (j > 0) sb.append(",");
                sb.append(matrix[i][j]);
            }
        }
        return sb.toString();
    }
    
    private void sendError(String taskId, String error) throws Exception {
        Message errMsg = new Message();
        errMsg.messageType = "TASK_ERROR";
        errMsg.payload = taskId + ";" + error;
        writer.println(errMsg.toJson());
    }
    
    private void startHeartbeatSender() {
        Thread sender = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    Message hb = new Message();
                    hb.messageType = "HEARTBEAT";
                    hb.payload = "WORKER_ALIVE";
                    writer.println(hb.toJson());
                } catch (Exception e) {
                    break;
                }
            }
        });
        sender.setDaemon(true);
        sender.start();
    }
    
    public static void main(String[] args) throws Exception {
        String workerId = System.getenv().getOrDefault("WORKER_ID", "worker-1");
        String masterHost = System.getenv().getOrDefault("MASTER_HOST", "localhost");
        int masterPort = Integer.parseInt(System.getenv().getOrDefault("MASTER_PORT", "9999"));
        
        ReferenceWorker worker = new ReferenceWorker(workerId, masterHost, masterPort);
        worker.connect();
        
        Thread.sleep(Long.MAX_VALUE);
    }
}
