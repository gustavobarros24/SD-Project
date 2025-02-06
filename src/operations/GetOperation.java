
package operations;

import taggedConnection.TaggedConnection;

import java.io.IOException;

public class GetOperation extends Operation {
    private final String key;

    public GetOperation(String key) {
        super(Command.GET);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format("GetOperation [key=%s]", key);
    }

    @Override
    public void sendToServer(TaggedConnection tc) {
        try { tc.sendToServer(4, key); }
        catch (IOException e) { System.out.println("Error sending GET data: " + e.getMessage()); }
    }
}