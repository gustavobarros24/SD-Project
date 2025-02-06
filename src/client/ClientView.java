package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ClientView {
    BufferedReader in;
    Logger logger = Logger.getLogger(ClientView.class.getName());

    ClientView() {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    public void printMessage(String message) {
        System.out.println(message);
    }

    public void printError(Exception e, String message) {
        logger.severe(e.getMessage());
        System.out.println(message);
    }

    public String getUsername() {
        System.out.println("Username: ");
        String username = null;
        try {
            username = in.readLine();
        } catch (Exception e) {
            this.printError(e, "Error reading username");
        }
        return username;
    }

    public String getPassword() {
        System.out.println("Password: ");
        String password = null;
        try {
            password = in.readLine();
        } catch (Exception e) {
            this.printError(e, "Error reading password");
        }
        return password;
    }

    public String getKey() {
        System.out.println("Enter key: ");
        String key = null;
        while (key == null || key.isEmpty()) {
            try {
                key = in.readLine();
            } catch (Exception e) {
                this.printMessage("Error reading key, try again");
            }
        }
        return key;
    }

    public byte[] getValue() {
        System.out.println("Enter value: ");
        byte[] value = null;
        while (value == null) {
            try {
                value = in.readLine().getBytes();
            } catch (Exception e) {
                this.printMessage("Error reading value, try again");
            }
        }
        return value;
    }

    // usado para multiPut
    public Map<String, byte[]> getKeyValuePairs() {
        System.out.println("Enter number of key-value pairs: ");
        int n = 0;
        while (n <= 0) {
            try {
                n = Integer.parseInt(in.readLine());
            } catch (Exception e) {
                this.printMessage("Invalid number of key-value pairs, try again");
            }
        }
        Map<String, byte[]> pairs = new java.util.HashMap<>();
        for (int i = 0; i < n; i++) {
            System.out.println("Key-value pair nº" + (i + 1) + ":");
            String key = this.getKey();
            byte[] value = this.getValue();
            pairs.put(key, value);
        }
        return pairs;
    }

    // usado para multiGet
    public Set<String> getKeys() {
        System.out.println("Enter number of keys: ");
        int n = 0;
        while (n <= 0) {
            try {
                n = Integer.parseInt(in.readLine());
            } catch (Exception e) {
                this.printMessage("Invalid number of keys, try again");
            }
        }
        Set<String> keys = new java.util.HashSet<>();
        for (int i = 0; i < n; i++) {
            System.out.println("Key nº" + (i + 1) + ":");
            String key = this.getKey();
            keys.add(key);
        }
        return keys;
    }

    public int printAuthenticationMenu() {
        System.out.println("Authentication Menu");
        System.out.println("1 - Register");
        System.out.println("2 - Login");
        System.out.println("3 - Exit");
        System.out.println("Choose an option: ");
        int option = 0;
        while (option < 1 || option > 3) {
            try {
                option = Integer.parseInt(in.readLine());
            } catch (Exception e) {
                this.printError(e, "Invalid option, try again");
            }
        }
        return option;
    }

    public int printMainMenu() {
        System.out.println("Main Menu");
        System.out.println("1 - put");
        System.out.println("2 - get");
        System.out.println("3 - multiPut");
        System.out.println("4 - multiGet");
        System.out.println("5 - Exit");
        System.out.println("Choose an option: ");
        int option = 0;
        while (option < 1 || option > 5) {
            try {
                option = Integer.parseInt(in.readLine());
            } catch (Exception e) {
                this.printError(e, "Invalid option, try again");
            }
        }
        return option;
    }
}
