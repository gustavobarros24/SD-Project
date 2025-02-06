package benchmarking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Workload {
    final String name;
    HashMap<ClientConfiguration, Integer> clientsCount = new HashMap<>();

    // clientsCount dictates how many clients will be started for each specified ClientConfiguration.
    // The results of each client will be stored as lists mapped by the ClientConfiguration name as key.
    // example: "put_tester" -> [AutomatedClient("put_tester")[0], AutomatedClient("put_tester")[1], AutomatedClient("put_tester")[2]...]
    // We can then order these results by various criteria to obtain, for example, the min/max/median ops per second achieved by clients running "put_tester".
    HashMap<ClientConfiguration, AutomatedClient[]> clientsByConfig = new HashMap<>();
    Double latestMedianOperationRate = 0.0;
    Double latestAvgOperationRate = 0.0;

    public Workload (String name, HashMap<ClientConfiguration, Integer> clientsPerConfiguration) {
        this.name = name;
        this.clientsCount = clientsPerConfiguration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Workload ").append(name).append(":").append("\n");

        if (clientsCount.isEmpty()) {
            sb.append("No client configurations assigned.\n");
        } else {
            clientsCount.forEach((config, count) ->
                    sb.append("| ").append(count).append(" ").append(config.name()).append(" clients").append("\n")
            );
        }

        if (clientsByConfig.isEmpty() == false) {
            sb.append("| Latest results:\n");
            sb.append("| | half the clients had avg speed below ").append(String.format("%.2f", latestMedianOperationRate)).append(" operations/s.\n");
            sb.append("| | the avg speed throughout the clients was ").append(String.format("%.2f", latestAvgOperationRate)).append(" operations/s.\n");
        }

        return sb.toString();
    }

    public void execute(boolean silent) {
        createClients();

        ArrayList<AutomatedClient> clients = new ArrayList<>();
        clientsByConfig.forEach(
                (k, v) -> { clients.addAll(Arrays.stream(v).toList()); }
        );

        for (AutomatedClient client : clients) client.start();
        for (AutomatedClient client : clients) {
            try {
                client.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        latestMedianOperationRate = calcMedianOperationRate();
        latestAvgOperationRate = calcAvgOperationRate();
        if (!silent) System.out.println(this);

        /*
        clientsByConfig.forEach( (k, v) -> {
                    for (AutomatedClient client : v) client.start();
                }
        );

        clientsByConfig.forEach( (k, v) -> {
                    for (AutomatedClient client : v)
                        try { client.join(); }
                        catch (InterruptedException e) { e.printStackTrace();}
                }
        );

        */
    }

    private void createClients() {
        // First, clear clientsByConfig to make the Workload hold a fresh bundle of runnable clients.
        // This way, a future execution will wipe the previous AutomatedClient instances.
        clientsByConfig.clear();

        // Then, instantiate in clientsByConfig as many AutomatedClients as specified in clientsCount.
        clientsCount.forEach(
                (k,v) -> {
                    // Initialize v-sized array of clients for the ClientConfiguration k.
                    // Then, make k map to it in the clientsByConfig hashmap.
                    AutomatedClient[] clientsForThisConfig = new AutomatedClient[v];
                    Arrays.setAll(clientsForThisConfig, i -> new AutomatedClient(k));
                    clientsByConfig.put(k, clientsForThisConfig);
                    //System.out.println(Arrays.stream(clientsByConfig.get(k)).toList());
                }
        );
    }

    private Double calcAvgOperationRate() {
        // Flattened list of all clients
        ArrayList<AutomatedClient> clients = new ArrayList<>();
        clientsByConfig.forEach(
                (k, v) -> { clients.addAll(Arrays.stream(v).toList()); }
        );

        Double sumAvgOpsPerSecond = 0.0;
        for (AutomatedClient client : clients) sumAvgOpsPerSecond += client.avgOpsPerSecond;

        return sumAvgOpsPerSecond/clients.size();
    }

    private Double calcMedianOperationRate() {
        // Flattened list of all clients
        ArrayList<AutomatedClient> clients = new ArrayList<>();
        clientsByConfig.forEach(
                (k, v) -> { clients.addAll(Arrays.stream(v).toList()); }
        );

        StringBuilder sb = new StringBuilder();
        /*
        clients.sort(Comparator.comparingLong(AutomatedClient::medianOperationTime));
        sb.append(" | Median operation times among clients: ");
            sb.append(clients.getFirst().medianOperationTime()).append(" min, ");
            sb.append(clients.get(clients.size()/2).medianOperationTime()).append(" median, ");
            sb.append(clients.getLast().medianOperationTime()).append(" max nanoseconds");
            sb.append("\n");
        */

        clients.sort(Comparator.comparingDouble(c -> c.avgOpsPerSecond));

        /*
        sb.append(" | Average operation speed among clients: ");
            sb.append(clients.getFirst().medianOperationTime()).append(" min, ");
            sb.append(clients.get(clients.size()/2).medianOperationTime()).append(" median, ");
            sb.append(clients.getLast().medianOperationTime()).append(" max operations/s");
        */

        return clients.get((clients.size()/2)).avgOpsPerSecond;
    }
}
