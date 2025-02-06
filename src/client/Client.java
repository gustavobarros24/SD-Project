package client;

import server.AuthenticationMessage;
import taggedConnection.TaggedConnection;
import server.User;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class Client {
    static ClientView view = new ClientView();


    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 12345);
            TaggedConnection c = new TaggedConnection(socket);

            boolean logged = login_or_register(c);
            if (!logged) {
                view.printMessage("Not logged in. Exiting...");
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                return;
            }

            view.printMessage("Logged in!");

            int option;
            do {
                option = view.printMainMenu();
                switch (option) {
                    case 1: // put
                        String key = view.getKey();
                        byte[] value = view.getValue();
                        try {
                            c.sendToServer(3, key, value);
                        } catch (IOException e) {
                            view.printError(e, "Error sending put data");
                        }
                        // no put o servidor não envia resposta
                        break;
                    case 2: // get
                        String getKey = view.getKey();
                        try {
                            c.sendToServer(4, getKey);
                        } catch (IOException e) {
                            view.printError(e, "Error sending get data");
                        }
                        TaggedConnection.Frame frame = c.receiveFromServer();
                        if (frame.data == null) {
                            view.printMessage("Value received: null");
                            break;
                        }
                        byte[] response = (byte[]) frame.data;
                        view.printMessage("Value received: " + new String(response));
                        break;
                    case 3: // multiPut
                        try {
                            Map<String, byte[]> pairs = view.getKeyValuePairs();
                            c.sendToServer(5, pairs);
                        } catch (IOException e) {
                            view.printError(e, "Error sending multiPut data");
                        }
                        break;
                    case 4: // multiGet
                        try {
                            Set<String> keys = view.getKeys();
                            c.sendToServer(6, keys);
                        } catch (IOException e) {
                            view.printError(e, "Error sending multiGet data");
                        }
                        TaggedConnection.Frame frame2 = c.receiveFromServer();
                        @SuppressWarnings("unchecked") Map<String, byte[]> result = (Map<String, byte[]>) frame2.data;
                        view.printMessage("Values received: ");
                        for (Map.Entry<String, byte[]> entry : result.entrySet()) {
                            if (entry.getValue() != null) {
                                view.printMessage(entry.getKey() + " - " + new String(entry.getValue()));
                            } else {
                                view.printMessage(entry.getKey() + " - null");
                            }
                        }
                        break;
                    case 5: // Exit
                        try {
                            c.sendToServer(0);
                        } catch (IOException e) {
                            view.printError(e, "Error sending exit data");
                        }
                        break;
                }
            } while (option != 5);


            //Nota: ao sair do cliente, o servidor deve ser informado para que o mesmo possa fechar a conexão (para matar a thread)

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();

        } catch (Exception e) {
            view.printError(e, "Error in client.Client");
        }
    }

    private static boolean login_or_register(TaggedConnection c) {
        int option = view.printAuthenticationMenu();
        boolean logged = false;
        switch (option) {
            case 1: // Register
                logged = handleUserAuthentication(c, 1);
                break;
            case 2: // Login
                logged = handleUserAuthentication(c, 2);
                break;
            case 3:
                logged = handleUserAuthentication(c, 3);
                break;
        }
        return logged;
    }

    private static boolean handleUserAuthentication(TaggedConnection c, int option) {
        if (option == 3) { // Exit
            try {
                c.sendToServer(0);
            } catch (IOException e) {
                view.printError(e, "Error sending exit data in handleUserAuthentication");
            }
            return false;
        }

        String username = view.getUsername();
        String password = view.getPassword();

        try {
            c.sendToServer(option, new User(username, password));
        } catch (IOException e) {
            if (option == 1) {
                view.printError(e, "Error sending register data");
                return false;
            } else if (option == 2) {
                view.printError(e, "Error sending login data");
                return false;
            } else {
                view.printError(e, "Invalid option when sending in client.Client");
                return false;
            }
        }


        int responseTag = 0;
        TaggedConnection.Frame frame;
        String message;
        view.printMessage("Nota: A conexão pode atrasar se o servidor estiver cheio.");
        try {
            frame = c.receiveFromServer();
            responseTag = frame.tag;

        } catch (IOException e) {
            view.printError(e, "Error receiving data from server\nTag: " + responseTag);
            return false;

        }


        boolean logged = false;
        switch (responseTag) {
            case 1: // Register
                boolean registered = (boolean) frame.data;
                if (registered) {
                    message = "user.User registered!";
                } else {
                    message = "user.User already exists!";
                }
                logged = registered;
                break;
            case 2: // Login
                AuthenticationMessage authMessage = (AuthenticationMessage) frame.data;
                message = authMessage.toString();
                logged = authMessage.isSuccess();
                break;
            default:
                message = "Invalid responseTag in client.Client";
                break;
        }
        view.printMessage(message);
        return logged;
    }

}
