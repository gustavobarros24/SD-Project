package taggedConnection;

import server.AuthenticationMessage;
import server.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    public static class Frame {
        public final int tag;
        public final Object data;
        public final Object optionalData;

        public Frame(int tag, Object data) {
            this.tag = tag;
            this.data = data;
            this.optionalData = null;
        }

        public Frame(int tag, Object data, Object optionalData) {
            this.tag = tag;
            this.data = data;
            this.optionalData = optionalData;
        }
    }

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ReentrantLock sendLock = new ReentrantLock();
    private final ReentrantLock receiveLock = new ReentrantLock();


    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    // envia a tag a 0 (exit) para o servidor
    public void sendToServer(int tag) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // o username e passe para o servidor
    public void sendToServer(int tag, User user) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            user.serialize(out);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // usado para enviar o valor de um par chave-valor (put)
    public void sendToServer(int tag, String key, byte[] data) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeUTF(key);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // Enviar uma string (usado no get)
    public void sendToServer(int tag, String key) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeUTF(key);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // Enviar um map de pares chave-valor (multiPut)
    public void sendToServer(int tag, Map<String, byte[]> pairs) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeInt(pairs.size());
            for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeInt(entry.getValue().length);
                out.write(entry.getValue());
            }
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // Enviar um set de chaves (multiGet)
    public void sendToServer(int tag, Set<String> keys) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeInt(keys.size());
            for (String key : keys) {
                out.writeUTF(key);
            }
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // envia a resposta do servidor num login
    public void sendToClient(int tag, AuthenticationMessage message) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            message.serialize(out);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    //envia a resposta do servidor num register
    public void sendToClient(int tag, boolean message) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeBoolean(message);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // envia a resposta do servidor num get. Pode ser null
    public void sendToClient(int tag, byte[] data) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            if (data == null) {
                out.writeInt(0);
            } else {
                out.writeInt(data.length);
                out.write(data);
            }
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // envia a resposta do servidor num put/multiPut. Basta a tag
    public void sendToClient(int tag) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.flush();
        } finally {
            sendLock.unlock();
        }
    }

    // envia a resposta do servidor num multiGet. Dentro do map pode haver valores null
    public void sendToClient(int tag, Map<String, byte[]> pairs) throws IOException {
        sendLock.lock();
        try {
            out.writeInt(tag);
            out.writeInt(pairs.size());
            for (Map.Entry<String, byte[]> entry : pairs.entrySet()) {
                out.writeUTF(entry.getKey());
                if (entry.getValue() == null) {
                    out.writeInt(0);
                } else {
                    out.writeInt(entry.getValue().length);
                    out.write(entry.getValue());
                }
            }
        } finally {
            sendLock.unlock();
        }
    }

    public Frame receiveFromServer() throws IOException {
        receiveLock.lock();
        try {
            int tag = in.readInt();
            switch (tag) {
                case 1: // Register
                    return new Frame(tag, in.readBoolean());
                case 2: // Login
                    return new Frame(tag, AuthenticationMessage.deserialize(in));
                case 3: //put
                    return new Frame(tag, null);
                    //throw new RuntimeException("Invalid tag, there should be no response from server for put");
                case 4: //get
                    int length = in.readInt();
                    if (length == 0) {
                        return new Frame(tag, null);
                    }
                    byte[] data = new byte[length];
                    in.readFully(data);
                    return new Frame(tag, data);
                case 5: //multiPut
                    return new Frame(tag, null);
                    //throw new RuntimeException("Invalid tag, there should be no response from server for multiPut");
                case 6: //multiGet
                    Map<String, byte[]> pairs = new java.util.HashMap<>();
                    int numPairs = in.readInt();
                    for (int i = 0; i < numPairs; i++) {
                        String key = in.readUTF();
                        int length2 = in.readInt();
                        if (length2 == 0) {
                            pairs.put(key, null);
                        } else {
                            byte[] value = new byte[length2];
                            in.readFully(value);
                            pairs.put(key, value);
                        }
                    }
                    return new Frame(tag, pairs);
                default:
                    System.out.println("Invalid tag");
                    return null;
            }
        } finally {
            receiveLock.unlock();
        }
    }

    public Frame receiveFromClient() throws IOException {
        receiveLock.lock();
        try {
            int tag = in.readInt();
            switch (tag) {
                case 0: // Exit
                    return new Frame(tag, null);
                case 1: // Register
                    User user = User.deserialize(in);
                    return new Frame(tag, user);
                case 2: // Login
                    User user2 = User.deserialize(in);
                    return new Frame(tag, user2);
                case 3: //put
                    String key = in.readUTF();
                    byte[] data = new byte[in.readInt()];
                    in.readFully(data);
                    return new Frame(tag, key, data);
                case 4: //get
                    String key2 = in.readUTF();
                    return new Frame(tag, key2);
                case 5: //multiPut
                    int numPairs = in.readInt();
                    Map<String, byte[]> pairs = new java.util.HashMap<>();
                    for (int i = 0; i < numPairs; i++) {
                        String key3 = in.readUTF();
                        byte[] value = new byte[in.readInt()];
                        in.readFully(value);
                        pairs.put(key3, value);
                    }
                    return new Frame(tag, pairs);
                case 6: //multiGet
                    int numKeys = in.readInt();
                    Set<String> keys = new java.util.HashSet<>();
                    for (int i = 0; i < numKeys; i++) {
                        keys.add(in.readUTF());
                    }
                    return new Frame(tag, keys);
                default:
                    System.out.println("Invalid tag");
                    return null;
            }
        } finally {
            receiveLock.unlock();
        }
    }


    public void close() throws IOException {
        socket.close();
    }
}