
package operations;
import taggedConnection.TaggedConnection;

public abstract class Operation {
    public enum Command {
        PUT, MULTIPUT, GET, MULTIGET
    }

    final Command command;

    protected Operation(Command command) {
        this.command = command;
    }

    public Command getCommand() {
        return command;
    }

    @Override
    public abstract String toString();

    public abstract void sendToServer(TaggedConnection tc);
}