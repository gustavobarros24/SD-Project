package server;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class UserAuthentication {
    // Mapa de utilizadores registados: username -> password (possivelmente encriptada)
    HashMap<String, User> users = new HashMap<>();
    ReentrantLock lock = new ReentrantLock();

    public void putUser(String username, User user) {
        lock.lock();
        try {
            this.users.putIfAbsent(username, user); // Para não sobrescrever um utilizador já existente (isto mudaria a palavra-passe de um user existente)
        } finally {
            lock.unlock();
        }
    }

    public User getUser(String username) {
        lock.lock();
        try {
            return this.users.get(username);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(users); // Generate hash code based on the users field
    }


    public boolean register(String username, String password) {
        if (users.containsKey(username)) {
            System.out.println("user.User already exists.");
            return false;
        }
        this.putUser(username, new User(username, password));
        System.out.println("user.User registered successfully!");
        return true;
    }

    public AuthenticationMessage authenticate(String username, String password) {
        User mapUser = this.getUser(username);
        if (mapUser == null) {
            System.out.println("Username doesn't exist");
            return AuthenticationMessage.WRONG_USERNAME;
        }
        if (!mapUser.verifyPassword(password)) {
            System.out.println("Wrong password");
            return AuthenticationMessage.WRONG_PASSWORD;
        }
        System.out.println("user.User authenticated successfully!");
        return AuthenticationMessage.SUCCESS;
    }
}
