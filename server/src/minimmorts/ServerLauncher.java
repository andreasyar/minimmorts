package minimmorts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import minimmorts.client.ClientManager;
import minimmorts.db.DBWorker;
import minimmorts.messages.MessageManager;
import minimmorts.websocketserver.WebSocketServerPipelineFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.xml.sax.SAXException;

/**
 * Game server launcher. Use it to start game server.
 * @author Sorc
 */
public class ServerLauncher {
    private static final Logger logger = Logger.getLogger(ServerLauncher.class.getName());

    public static void main(String[] args) {
        ServerConfig config;
        DBWorker dbworker;
        Server gameServer;

        // Read config.
        
        try {
            config = new ServerConfig("config-home.xml");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        logger.log(Level.INFO, "Configuration readed.");
        
        // Connect to database.

        dbworker = new DBWorker(config.getDbHost(), config.getDbPort(), config.getDatabaseName(), config.getDbUser(), config.getDbPassword());
        try {
            dbworker.connect();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        logger.log(Level.INFO, "DB connected.");
        
        // Create game server.
        
        MessageManager messageManager = new MessageManager();
        ClientManager clientManager = new ClientManager();
        gameServer = new Server(clientManager);
        messageManager.setGameServer(gameServer);
        gameServer.setMessageManager(messageManager);
        logger.log(Level.INFO, "Game server created.");
        
        // Starts web server.
        
        ServerBootstrap bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
        );
        bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(gameServer));
        bootstrap.bind(new InetSocketAddress(Integer.parseInt(config.getWebPort())));
        logger.log(Level.INFO, "Web server bind port {0}.", Integer.parseInt(config.getWebPort()));
        
        // Start server main loop.
        
        gameServer.start();
    }
}
