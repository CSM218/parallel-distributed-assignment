package pdc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Simple Message schema for CSM218 protocol.
 * Fields: magic, version, messageType, studentId, timestamp, payload
 */
public class Message {
    public String magic = "CSM218";
    public int version = 1;
    public String messageType;
    public String studentId;
    public long timestamp;
    public String payload;

    public Message() {
    }

    public Message(String messageType, String studentId, String payload) {
        this.messageType = messageType;
        this.studentId = studentId;
        this.timestamp = System.currentTimeMillis();
        this.payload = payload;
    }

    public String toJson() {
        // Simple JSON builder to avoid external deps
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        sb.append("\"magic\":\"").append(escape(magic)).append("\",");
        sb.append("\"version\":").append(version).append(',');
        sb.append("\"messageType\":\"").append(escape(messageType)).append("\",");
        sb.append("\"studentId\":\"").append(escape(studentId)).append("\",");
        sb.append("\"timestamp\":").append(timestamp).append(',');
        sb.append("\"payload\":\"").append(escape(payload)).append('\"');
        sb.append('}');
        return sb.toString();
    }

    public static Message fromJson(String s) {
        if (s == null)
            return null;
        Message m = new Message();
        Map<String, String> map = parseFlatJson(s);
        m.magic = map.getOrDefault("magic", "CSM218");
        try {
            m.version = Integer.parseInt(map.getOrDefault("version", "1"));
        } catch (Exception e) {
        }
        m.messageType = map.get("messageType");
        m.studentId = map.get("studentId");
        try {
            m.timestamp = Long.parseLong(map.getOrDefault("timestamp", "0"));
        } catch (Exception e) {
        }
        m.payload = map.get("payload");
        return m;
    }

    /**
     * Alias for fromJson to support standard autograder integration.
     */
    public static Message parse(String s) {
        return fromJson(s);
    }

    public boolean validate() {
        if (!Objects.equals(magic, "CSM218"))
            return false;
        if (version != 1)
            return false;
        if (messageType == null || messageType.isEmpty())
            return false;
        if (studentId == null || studentId.isEmpty())
            return false;
        return true;
    }

    private static String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static Map<String, String> parseFlatJson(String s) {
        Map<String, String> map = new HashMap<>();
        String t = s.trim();
        if (t.startsWith("{"))
            t = t.substring(1);
        if (t.endsWith("}"))
            t = t.substring(0, t.length() - 1);
        String[] parts = t.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length < 2)
                continue;
            String k = kv[0].trim();
            String v = kv[1].trim();
            if (k.startsWith("\""))
                k = k.substring(1);
            if (k.endsWith("\""))
                k = k.substring(0, k.length() - 1);
            if (v.startsWith("\""))
                v = v.substring(1);
            if (v.endsWith("\""))
                v = v.substring(0, v.length() - 1);
            v = v.replaceAll("\\\\n", "\n").replaceAll("\\\\\"", "\"").replaceAll("\\\\\\\\", "\\\\");
            map.put(k, v);
        }
        return map;
    }
}
