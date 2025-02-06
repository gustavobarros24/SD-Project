
package operations;

import taggedConnection.TaggedConnection;

import java.io.IOException;
import java.util.Set;

public class MultiGetOperation extends Operation {
    private final Set<String> keys;

    public MultiGetOperation(Set<String> keys) {
        super(Command.MULTIGET);
        this.keys = keys;
    }

    public Set<String> getKeys() {
        return keys;
    }

    @Override
    public String toString() {
        return String.format("MultiGetOperation [keys=%s]", String.join(", ", keys));
    }

    public void sendToServer(TaggedConnection tc) {
        try { tc.sendToServer(6, getKeys()); }
        catch (IOException e) { System.out.println("Error sending MULTIGET data: " + e.getMessage()); }
    }
}