package minimmorts.db;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import minimmorts.config.XMLConfig;

public class DBWorker {
    private Connection connection;

    private String host;
    private String port;
    private String database;
    private String user;
    private String password;

    public DBWorker(String host, String port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    /**
     * Этот конструктор только MMORPG сервера.
     * @deprecated Этот конструктор только для обратной совместимости, чтобы
     * работал старый MMORTS сервер.
     */
    public DBWorker(XMLConfig xmlconfig) {
        String DEFAULT_SERVER = "localhost";
        String DEFAULT_PORT = "8015";
        String DEFAULT_DATABASE = "iivgrpg";
        String DEFAULT_USER = "root";
        String DEFAULT_PASSWORD = "";

        if (xmlconfig != null) {
            host = xmlconfig.getString("database/server", DEFAULT_SERVER);
            port = xmlconfig.getString("database/port", DEFAULT_PORT);
            database = xmlconfig.getString("database/database", DEFAULT_DATABASE);
            user = xmlconfig.getString("database/user", DEFAULT_USER);
            password = xmlconfig.getString("database/password", DEFAULT_PASSWORD);
        } else {
            host = DEFAULT_SERVER;
            port = DEFAULT_PORT;
            database = DEFAULT_DATABASE;
            user = DEFAULT_USER;
            password = DEFAULT_PASSWORD;
        }
    }

    public void connect() throws SQLException {
        Properties p = new Properties();
        String s = "jdbc:mysql://" + host + ":" + port + "/" + database + "?user=" + user + "&password=" + password;
        p.setProperty("autoReconnect", "true");
        p.setProperty("validationQuery", "Select 1");
        connection = (Connection) DriverManager.getConnection(s, p);
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    public Statement newStatement() throws SQLException {
        return (Statement) connection.createStatement();
    }
}
