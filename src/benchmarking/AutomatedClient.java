package benchmarking;

import taggedConnection.TaggedConnection;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

// An benchmarking.AutomatedClient is a client that connects to the server and executes a benchmarking.ClientConfiguration.
public class AutomatedClient extends Thread {
    TaggedConnection tc;
    ClientConfiguration config;

    // always assume nanoseconds unless stated otherwise
    ArrayList<Long> executionTimes = null;

    // stats
    Long sumOperationTime = null;
    Double avgOperationTime = null;
    Double avgOpsPerSecond = null;
    Long[] minMedianMax = null;

    public AutomatedClient(ClientConfiguration config) {
        this.config = config;
    }

    @Override
    public String toString() {
        if (executionTimes == null) {
            return "This benchmarking.AutomatedClient hasn't been run yet.";
        } else {
            StringBuilder builder = new StringBuilder();
            return builder
                    .append("AutomatedClient:\n")
                    .append("| sumOperationTime = ").append(sumOperationTime).append("ns\n")
                    .append("| avgOperationTime = ").append(String.format("%.2fns\n", avgOperationTime))
                    .append("| avgOpsPerSecond = ").append(String.format("%.2f operations/s\n", avgOpsPerSecond))
                    .append("| operationTimes: ")
                        .append(String.format("%dns min, ", minOperationTime()))
                        .append(String.format("%dns median, ", medianOperationTime()))
                        .append(String.format("%dns max\n", maxOperationTime()))
                    .toString();
        }
    }

    @Override
    public void run() {
        try { tc = new TaggedConnection(new Socket("localhost", 12345)); }
        catch (IOException e) { e.printStackTrace(); return; }

        Boolean loginSuccess = config.login(tc);
        //System.out.println("Login went well? " + loginSuccess.toString());
        executionTimes = config.executeAllOperations(tc);
        executionTimes.sort(null);
        config.exit(tc);

        calculateMetrics();
        //System.out.println(this);
    }

    private void calculateMetrics() {
        sumOperationTime = executionTimes.stream().mapToLong(Long::longValue).sum();
        int totalOperations = config.operations().size() * config.repeat();
        avgOperationTime = (double) sumOperationTime / totalOperations;
        avgOpsPerSecond = (double) 1_000_000_000 * totalOperations / sumOperationTime;
        minMedianMax = new Long[] {
                executionTimes.getFirst(),
                executionTimes.get(executionTimes.size() / 2),
                executionTimes.getLast()
        };
    }

    public Long minOperationTime() {
        return minMedianMax[0];
    }
    public Long maxOperationTime() {
        return minMedianMax[2];
    }
    public Long medianOperationTime() {
        return minMedianMax[1];
    }
}
