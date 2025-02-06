
package operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import taggedConnection.TaggedConnection;

import java.io.IOException;

public class PutOperation extends Operation {
    private final String key;
    private final byte[] value;

    public PutOperation(String key, String value) {
        super(Command.PUT);
        this.key = key;
        this.value = value.getBytes();
    }

    public String getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("PutOperation [%s->%s]", key, bytesToHex(value));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return "0x" + hexString.toString();
    }

    @Override
    public void sendToServer(TaggedConnection tc) {
        try { tc.sendToServer(3, getKey(), getValue()); }
        catch (IOException e) { System.out.println("Error sending PUT data: " + e.getMessage()); }
    }
}