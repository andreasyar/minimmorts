package minimmorts;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import minimmorts.client.ClientManager;
import minimmorts.messages.MessageManager;
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
    
    private static ServerMainLoop serverMainLoop = null;
    
    private static ScheduledExecutorService serverMainLoopScheduler = null;
    
    private static ScheduledFuture<?> serverMainLoopHandler = null;

    public static void main(String[] args) {
        ServerConfig config = null;
        ClientManager clientManager = null;
        MessageManager messageManager = null;
        MovementManager movementManager = null;
        UnitManager unitManager = null;

        // Read config.
        
        try {
            config = new ServerConfig("config-home.xml");
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        logger.log(Level.INFO, "Configuration readed.");
        
        // Create game server.
        
        try {
            clientManager = new ClientManager();
            //ClientManager.enableLogging();
            
            messageManager = new MessageManager(clientManager);
            //MessageManager.enableLogging();
            
            movementManager = new MovementManager();
            //MovementManager.enableLogging();
            
            unitManager = new UnitManager();
            //UnitManager.enableLogging();
            
            serverMainLoop = new ServerMainLoop(unitManager, movementManager, messageManager, clientManager, Integer.parseInt(config.getStepTime()));
            //ServerMainLoop.enableLogging();
            
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
        
        serverMainLoopScheduler = Executors.newScheduledThreadPool(1);
        serverMainLoopHandler = serverMainLoopScheduler.scheduleAtFixedRate(
            serverMainLoop,
            Integer.parseInt(config.getStepTime()),
            Integer.parseInt(config.getStepTime()),
            TimeUnit.MILLISECONDS
        );
        logger.log(Level.INFO, "Главный цикл сервера запущен.");
        
        /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        serverMainLoopHandler.get();
                    } catch (ExecutionException | InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            },
            0,
            TimeUnit.MILLISECONDS
        );*/
    }
    
    public static void restartServerMainLoop(long initialDelay, long period, TimeUnit timeUnit) {
        serverMainLoopHandler.cancel(false);
        serverMainLoopScheduler = Executors.newScheduledThreadPool(1);
        serverMainLoopHandler = serverMainLoopScheduler.scheduleAtFixedRate(serverMainLoop, initialDelay, period, timeUnit);
        logger.log(Level.INFO, "Главный цикл сервера перезапущен.");
        
        /*ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        serverMainLoopHandler.get();
                    } catch (ExecutionException | InterruptedException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            },
            0,
            TimeUnit.MILLISECONDS
        );*/
    }
}
