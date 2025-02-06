package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum AuthenticationMessage {
    SUCCESS,
    WRONG_PASSWORD,
    WRONG_USERNAME;

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(this.ordinal());
    }

    public static AuthenticationMessage deserialize(DataInputStream in) throws IOException {
        return AuthenticationMessage.values()[in.readInt()];
    }

    public boolean isSuccess() {
        return this.equals(SUCCESS);
    }

    public String toString() {
        switch (this) {
            case SUCCESS:
                return "user.User Authenticated!";
            case WRONG_PASSWORD:
                return "Wrong password";
            case WRONG_USERNAME:
                return "Username doesn't exist";
            default:
                return "Invalid message";
        }
    }
}
