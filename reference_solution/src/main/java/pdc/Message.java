package pdc;

import java.io.*;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final String MAGIC = "CSM218";
    public static final int VERSION = 1;
    
    public String magic;
    public int version;
    public String messageType;
    public String studentId;
    public long timestamp;
    public String payload;
    
    public Message() {
        this.magic = MAGIC;
        this.version = VERSION;
        this.timestamp = System.currentTimeMillis();
        this.studentId = System.getenv("STUDENT_ID");
        if (this.studentId == null) {
            this.studentId = "unknown";
        }
    }
    
    public static Message parse(String json) throws Exception {
        Message msg = new Message();
        json = json.trim();
        
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON format");
        }
        
        String[] pairs = json.substring(1, json.length() - 1).split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            
            String key = kv[0].trim().replaceAll("\"", "");
            String value = kv[1].trim().replaceAll("\"", "");
            
            if ("magic".equals(key)) msg.magic = value;
            else if ("version".equals(key)) msg.version = Integer.parseInt(value);
            else if ("messageType".equals(key)) msg.messageType = value;
            else if ("studentId".equals(key)) msg.studentId = value;
            else if ("timestamp".equals(key)) msg.timestamp = Long.parseLong(value);
            else if ("payload".equals(key)) msg.payload = value;
        }
        
        return msg;
    }
    
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"magic\":\"").append(magic).append("\",");
        sb.append("\"version\":").append(version).append(",");
        sb.append("\"messageType\":\"").append(messageType).append("\",");
        sb.append("\"studentId\":\"").append(studentId).append("\",");
        sb.append("\"timestamp\":").append(timestamp).append(",");
        sb.append("\"payload\":\"").append(payload).append("\"");
        sb.append("}");
        return sb.toString();
    }
    
    public void validate() throws Exception {
        if (!MAGIC.equals(magic)) {
            throw new Exception("Invalid magic: " + magic);
        }
        if (version != VERSION) {
            throw new Exception("Invalid version: " + version);
        }
        if (messageType == null || messageType.isEmpty()) {
            throw new Exception("Missing messageType");
        }
        if (studentId == null || studentId.isEmpty()) {
            throw new Exception("Missing studentId");
        }
    }
}
