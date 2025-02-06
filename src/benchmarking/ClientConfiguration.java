package benchmarking;

import operations.Operation;
import server.AuthenticationMessage;
import server.User;
import taggedConnection.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public record ClientConfiguration(
        String name,
        String password,
        int repeat,
        List<Operation> operations
) {

    // Authentication Methods //////////////////////////////////////////////////////////////////

    public boolean register() {

        // Registers name+password in the server.Server as a new user.User via a throwaway connection

        try (Socket socket = new Socket("localhost", 12345)) {
            TaggedConnection tc = new TaggedConnection(socket);

            // Register name & password in server
            tc.sendToServer(1, new User(name, password));

            // Receive response from server as a frame
            TaggedConnection.Frame frame;
            boolean registered = false;
            try {
                frame = tc.receiveFromServer();
                registered = (boolean) frame.data;
            } catch (IOException e) {
                System.out.println("Error sending register data: " + e.getMessage()); }

            // Send server an exit message so the worker can be killed off
            tc.sendToServer(0);
            //System.out.println("Registration complete for " + name);
            return registered;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean login(TaggedConnection tc) {

        boolean success = false;

        try {
            tc.sendToServer(2, new User(name, password));
        } catch (IOException e) {
            System.out.println("benchmarking.ClientConfiguration: Error sending register data: " + e.getMessage());
            return false;
        }

        TaggedConnection.Frame frame;
        try {
            frame = tc.receiveFromServer();
            if (frame != null)
                success = ((AuthenticationMessage) frame.data).isSuccess();
        } catch (IOException e) {
            System.out.println("benchmarking.ClientConfiguration: Error receiving data from server: " + e.getMessage());
            return false;
        }

        return success;
    }

    public void exit(TaggedConnection tc) {
        try { tc.sendToServer(0); }
        catch (IOException e) { System.out.println("benchmarking.ClientConfiguration: Error sending exit data: " + e.getMessage()); }
    }

    // Test Execution Methods //////////////////////////////////////////////////////////////////

    public ArrayList<Long> executeAllOperations(TaggedConnection tc) {

        //System.out.println("benchmarking.ClientConfiguration: Executing operations " + repeat() + " times...");

        ArrayList<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < repeat; i++) {
            for (Operation o : operations) {
                executionTimes.add(executeOperation(o, tc));
            }
        }

        return executionTimes;
    }

    public long executeOperation(Operation o, TaggedConnection tc) {

        // Send operation to server
        long startTime = System.nanoTime();
        o.sendToServer(tc);

        // Receive server response
        TaggedConnection.Frame frame = null;
        try { frame = tc.receiveFromServer(); }
        catch (IOException e) { System.out.println("Error receiving data from server: " + e.getMessage()); }

        return System.nanoTime() - startTime;
    }

    @Override
    public String toString() {
        StringBuilder operationsString = new StringBuilder();
        for (Operation operation : operations) {
            operationsString.append("\n    ").append(operation);
        }

        return "PreconfiguredClient{" +
                "\n  name='" + name + '\'' +
                "\n  password='" + password + '\'' +
                "\n  repeat=" + repeat +
                "\n  operations=[" + operationsString + "\n  ]" +
                "\n}";
    }

}
