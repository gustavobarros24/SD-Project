
package operations;

import taggedConnection.TaggedConnection;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MultiPutOperation extends Operation {
    private final Map<String, byte[]> pairs;


    public MultiPutOperation(Map<String, byte[]> pairs) {
        super(Command.MULTIPUT);
        this.pairs = pairs;
    }

    public Map<String, byte[]> getPairs() {
        return pairs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MultiPutOperation [");
        pairs.forEach((key, value) -> sb.append(key).append("->").append(bytesToHex(value)).append(", "));
        if (sb.length() > 19) {
            sb.setLength(sb.length() - 2); // Remove the trailing ", "
        }
        sb.append("]");
        return sb.toString();
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
        try { tc.sendToServer(5, getPairs()); }
        catch (IOException e) { System.out.println("Error sending MULTIPUT data: " + e.getMessage()); }
    }
}