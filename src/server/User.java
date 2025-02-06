package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean verifyPassword(String password) {
        return this.password.equals(password);
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(this.username);
        out.writeUTF(this.password);
    }

    public static User deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new User(username, password);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this == null || (this.getClass() != obj.getClass())) {
            return false;
        }
        User other = (User) obj;
        return this.username.equals(other.username) && this.password.equals(other.password);
    }
}

