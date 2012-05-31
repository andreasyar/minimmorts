package minimmorts.messages;

import com.google.gson.Gson;

/**
 *
 * @author Sorc
 */
public class LoginMessage implements Message {
    private String login;
    private String password;

    public LoginMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toJSON() {
        Gson gson = new Gson();
        return "[\"LOGIN\", " + gson.toJson(this) + "]";
    }
}
