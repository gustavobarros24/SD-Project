

import benchmarking.AutomatedClient;
import benchmarking.ClientConfiguration;
import benchmarking.Workload;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import operations.*;

import java.util.*;
import java.io.File;


public class TestPlatform {

    public static Map<String, ClientConfiguration> clientConfigs = new HashMap<>();
    public static Map<String, Workload> workloads = new HashMap<>();

    @Override
    public String toString() {
        if (clientConfigs.isEmpty())
            return "No client configurations present.";

        StringBuilder sb = new StringBuilder();
        clientConfigs.forEach((name, client) -> sb.append("Client Name: ").append(name).append("\n")
                .append(client).append("\n\n"));
        return sb.toString();
    }

    private static void fetchClientConfigurations(String filename) throws Exception {
        // Load JSON file
        File jsonFile = new File(Objects.requireNonNull(TestPlatform.class.getClassLoader()
                        .getResource(filename))
                .getFile());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile);

        // Iterate over each client configuration in the JSON file
        root.fields().forEachRemaining(entry -> {
            String clientName = entry.getKey();
            JsonNode clientData = entry.getValue();

            String password = clientData.get("password").asText();
            int repeat = clientData.has("repeat") ? clientData.get("repeat").asInt() : 1;

            List<Operation> operations = new ArrayList<>();
            clientData.get("operations").forEach(opNode -> {
                String command = opNode.get("command").asText();
                JsonNode args = opNode.get("args");

                switch (command.toLowerCase()) {
                    case "put":
                        args.fields().forEachRemaining(arg -> {
                            String key = arg.getKey();
                            String value = arg.getValue().asText();
                            operations.add(new PutOperation(key, value));
                        });
                        break;

                    case "multiput":
                        Map<String, byte[]> pairs = new HashMap<>();
                        args.fields().forEachRemaining(arg -> {
                            String key = arg.getKey();
                            String value = arg.getValue().asText();
                            pairs.put(key, value.getBytes());
                        });
                        operations.add(new MultiPutOperation(pairs));
                        break;

                    case "get":
                        if (args.isTextual()) {
                            operations.add(new GetOperation(args.asText()));
                        } else if (args.isObject()) {
                            args.fields().forEachRemaining(arg -> {
                                String key = arg.getKey();
                                operations.add(new GetOperation(key));
                            });
                        }
                        break;

                    case "multiget":
                        Set<String> keys = new HashSet<>();
                        args.forEach(keyNode -> keys.add(keyNode.asText()));
                        operations.add(new MultiGetOperation(keys));
                        break;

                    default:
                        throw new IllegalArgumentException("Unknown command: " + command);
                }
            });

            ClientConfiguration client = new ClientConfiguration(clientName, password, repeat, operations);
            clientConfigs.put(clientName, client); // Store client in HashMap
        });
        //System.out.println(clientConfigs);
    }

    private static boolean registerAllConfigurations() throws InterruptedException {
        // The number of configurations
        int configsSize = clientConfigs.size();

        // Boolean array to track registration results
        boolean[] results = new boolean[configsSize];
        List<Thread> threads = new ArrayList<>();

        // Create an iterator over the client configurations
        Iterator<Map.Entry<String, ClientConfiguration>> iterator = clientConfigs.entrySet().iterator();

        for (int i = 0; i < configsSize; i++) {
            // Fetch the next client configuration
            Map.Entry<String, ClientConfiguration> entry = iterator.next();
            ClientConfiguration client = entry.getValue();
            final int threadIndex = i;

            // Create and start a thread for each client
            Thread thread = new Thread(() -> {
                // Each thread writes to its own index
                results[threadIndex] = client.register();
            });

            threads.add(thread);
            thread.start();
        }

        // Wait for all threads to finish
        for (Thread thread : threads) {
            thread.join();
        }

        // Check if all clients were successfully registered
        for (boolean result : results) {
            if (!result) {
                return false;
            }
        }

        return true;
    }

    private static void fetchWorkloads(String filename) throws Exception {
        // Load JSON file
        File jsonFile = new File(Objects.requireNonNull(TestPlatform.class.getClassLoader()
                        .getResource(filename))
                .getFile());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonFile);

        // Iterate over each workload in the JSON file
        root.fields().forEachRemaining(entry -> {
            String workloadName = entry.getKey();
            JsonNode clientData = entry.getValue();

            HashMap<ClientConfiguration, Integer> clientsPerConfiguration = new HashMap<>();

            clientData.fields().forEachRemaining(clientEntry -> {
                String clientName = clientEntry.getKey();
                int clientCount = clientEntry.getValue().asInt();

                ClientConfiguration clientConfig = clientConfigs.get(clientName);

                if (clientConfig == null)
                    throw new IllegalArgumentException("Client configuration '" + clientName + "' not found.");

                clientsPerConfiguration.put(clientConfig, clientCount);
            });

            // Create and store the benchmarking.Workload
            Workload workload = new Workload(workloadName, clientsPerConfiguration);
            workloads.put(workloadName, workload);
        });

        //System.out.println(workloads);
    }

    private static void startup() throws Exception {
        fetchClientConfigurations("clientconfigs.json");
        registerAllConfigurations();
        fetchWorkloads("workloads.json");
        workloads.get("smallput").execute(true); // This one is just to guarantee the test keys exist
    }

    public static void main(String[] args) throws Exception {

        startup();

        workloads.get("smallput").execute(false);
        workloads.get("smallget").execute(false);

        Thread.sleep(1000);

        workloads.get("mediumput").execute(false);
        workloads.get("mediumget").execute(false);

        Thread.sleep(1000);

        workloads.get("bigput").execute(false);
        workloads.get("bigget").execute(false);

        workloads.get("smallhalves").execute(false);
        workloads.get("mediumhalves").execute(false);
        workloads.get("bighalves").execute(false);

    }
}
