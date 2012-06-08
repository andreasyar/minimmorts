package minimmorts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import minimmorts.client.ClientManager;
import minimmorts.db.DBWorker;
import minimmorts.messages.MessageManager;
import minimmorts.movement.Movement;
import minimmorts.movement.MovementManager;
import minimmorts.server.ServerMainLoop;
import minimmorts.units.UnitManager;
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
        ServerConfig config = null;
        DBWorker dbworker = null;
        ClientManager clientManager = null;
        MessageManager messageManager = null;
        MovementManager movementManager = null;
        UnitManager unitManager = null;
        ServerMainLoop serverMainLoop = null;

        // Read config.
        
        try {
            config = new ServerConfig("config-home.xml");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        logger.log(Level.INFO, "Configuration readed.");
        
        // Connect to database.

        //dbworker = new DBWorker(config.getDbHost(), config.getDbPort(), config.getDatabaseName(), config.getDbUser(), config.getDbPassword());
        //try {
        //    dbworker.connect();
        //} catch (SQLException ex) {
        //    logger.log(Level.SEVERE, null, ex);
        //    return;
        //}
        //logger.log(Level.INFO, "DB connected.");
        
        // Create game server.
        
        try {
            clientManager = new ClientManager();
            ClientManager.enableLogging();
            
            messageManager = new MessageManager(clientManager);
            movementManager = new MovementManager();
            
            unitManager = new UnitManager();
            UnitManager.enableLogging();
            
            serverMainLoop = new ServerMainLoop(unitManager, movementManager, messageManager, clientManager);
            ServerMainLoop.enableLogging();
            
            logger.log(Level.INFO, "Game server created.");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        
        // Starts web server.
        
        ServerBootstrap bootstrap = new ServerBootstrap(
            new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())
        );
        bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory(messageManager));
        bootstrap.bind(new InetSocketAddress(Integer.parseInt(config.getWebPort())));
        logger.log(Level.INFO, "Web server bind port {0}.", Integer.parseInt(config.getWebPort()));
        
        // Start game server main loop.
        
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(serverMainLoop, 0, 100, TimeUnit.MILLISECONDS);
        logger.log(Level.INFO, "Game server main loop scheduled.");
    }
}
