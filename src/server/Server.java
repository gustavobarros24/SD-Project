package server;

import taggedConnection.TaggedConnection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static int activeSessions = 0; // Número de sessões ativas
    private static final ReentrantLock lock = new ReentrantLock(); // Lock para proteger a variável activeSessions
    private static final Condition condition = lock.newCondition(); // Condition para bloquear threads se o limite for atingido


    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("O programa deve ser chamado com um argumento inteiro");
            return;
        }
        String arg = args[0];
        // numero de threads/clientes que o servidor pode atender em simultâneo
        int S;
        try {
            S = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            System.out.println("O argumento deve ser um número inteiro.");
            return;
        }


        try (ServerSocket ss = new ServerSocket(12345)) {

            SharedMemory memoria = new SharedMemory();

            while (true) { // Main loop do server
                Socket socket = ss.accept();

                // Lógica para limitar o número de sessões
                lock.lock();
                try {
                    while (activeSessions >= S) {
                        condition.await(); // Espera até que uma sessão seja liberada
                    }
                    activeSessions++; // Aumenta o contador de sessões ativas
                } finally {
                    lock.unlock();
                }

                Thread t = new Thread(new ServerWorker(socket, memoria));
                t.start();

            }

        } catch (Exception e) {
            System.out.println("Debug: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void sessionEnded(TaggedConnection c) {
        lock.lock();
        try {
            activeSessions--; // Decrementa o número de sessões ativas
            condition.signal(); // Notifica uma thread em espera
        } finally {
            lock.unlock();
        }
        try {
            c.close(); // Fecha a conexão (socket)
            System.out.println("client.Client disconnected");
        } catch (IOException e) {
            System.out.println("Error closing connection");
            e.printStackTrace();
        }
    }

}

class ServerWorker implements Runnable {
    // Este worker vai ser responsável por lidar com um cliente (ou seja, tem o código que vai correr em cada thread)

    private final SharedMemory memoria;
    private TaggedConnection connection;

    public ServerWorker(Socket socket, SharedMemory memoria) {
        this.memoria = memoria;
        try {
            this.connection = new TaggedConnection(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void run() {
        System.out.println("client.Client connected");


        // Recebe o user.User do cliente
        TaggedConnection.Frame frame;
        try {
            frame = connection.receiveFromClient();
        } catch (IOException e) {
            System.out.println("Error receiving data from client in authentication");
            throw new RuntimeException(e);
        }
        boolean exit = false;
        switch (frame.tag) {
            case 0: // Exit
                exit = true;
                break;
            case 1: // Register
                User user = (User) frame.data;
                boolean registered = memoria.userAuthentication.register(user.getUsername(), user.getPassword());
                try {
                    connection.sendToClient(1, registered);
                } catch (IOException e) {
                    System.out.println("Error sending data to client in register");
                }
                if (!registered) exit = true;
                break;
            case 2: // Login
                User userLogin = (User) frame.data;
                AuthenticationMessage message = memoria.userAuthentication.authenticate(userLogin.getUsername(), userLogin.getPassword());
                try {
                    connection.sendToClient(2, message);
                } catch (IOException e) {
                    System.out.println("Error sending data to client in login");
                    e.printStackTrace();
                }
                if (!message.isSuccess()) exit = true;
                break;
            default:
                System.out.println("Invalid tag in authentication");
                exit = true;
        }

        if (exit) {
            Server.sessionEnded(connection);
            return;
        }

        System.out.println("user.User authenticated");


        TaggedConnection.Frame frame2;
        boolean exitTag = false;
        do {// recebe o pedido do cliente
            try {
                frame2 = connection.receiveFromClient();
            } catch (IOException e) {
                System.out.println("Error receiving data from client");
                throw new RuntimeException(e);
            }

            switch (frame2.tag) {
                case 0: // Exit
                    exitTag = true;
                    break;
                case 3: // put
                    String key = (String) frame2.data;
                    byte[] data = (byte[]) frame2.optionalData;
                    System.out.println("put " + key + "-" + new String(data));
                    memoria.put(key, data);
                    try {
                        connection.sendToClient(3);
                    } catch (IOException e) {
                        System.out.println("Error sending acknowledgement to client in put");
                        e.printStackTrace();
                    }
                    break;
                case 4: // get
                    String getKey = (String) frame2.data;
                    System.out.println("get " + getKey);
                    byte[] value = memoria.get(getKey);
                    try {
                        connection.sendToClient(4, value);
                    } catch (IOException e) {
                        System.out.println("Error sending data to client in get");
                        e.printStackTrace();
                    }
                    break;
                case 5: // multiPut
                    Map<String, byte[]> pairs = (Map<String, byte[]>) frame2.data;
                    System.out.println("multiPut");
                    for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                        System.out.println(entry.getKey() + " - " + new String(entry.getValue()));
                    }
                    memoria.multiPut(pairs);
                    try {
                        connection.sendToClient(5);
                    } catch (IOException e) {
                        System.out.println("Error sending acknowledgement to client in multiPut");
                        e.printStackTrace();
                    }
                    break;
                case 6: // multiGet
                    Set<String> keys = (Set<String>) frame2.data;
                    System.out.println("multiGet");
                    for (String key2 : keys) {
                        System.out.println(key2);
                    }
                    Map<String, byte[]> result = memoria.multiGet(keys);
                    try {
                        connection.sendToClient(6, result);
                    } catch (IOException e) {
                        System.out.println("Error sending data to client in multiGet");
                        e.printStackTrace();
                    }
                    break;
            }
        } while (!exitTag); //tag 0 é exit

        Server.sessionEnded(connection);

    }
}


