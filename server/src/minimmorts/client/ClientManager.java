package minimmorts.client;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.GlobalLogFile;
import minimmorts.messages.Message;
import org.jboss.netty.channel.Channel;

/**
 * Менеджер клиентов. Занимается управлением клиентами: созданием, хранением,
 * удалением, изменением статусов клиентов и т.д.
 * @author Sorc
 */
public class ClientManager {
    
    /**
     *
     */
    private static Logger logger = null;
    
    /**
     *
     */
    private static boolean loggingEnabled = false;

    /**
     * Хеш клиентов. id -> Клиент.
     */
    private final ConcurrentHashMap<Long, Client> clientsById = new ConcurrentHashMap();

    /**
     * Хеш клиентов. Канал -> Клиент.
     */
    private final ConcurrentHashMap<Channel, Client> clientsByChannel = new ConcurrentHashMap();

    public static void enableLogging() {
        ClientManager.loggingEnabled = true;
    }

    public static void disableLogging() {
        ClientManager.loggingEnabled = false;
    }

    public ClientManager() throws IOException {
        if (ClientManager.logger == null) {
            ClientManager.logger = Logger.getLogger(ClientManager.class.getName());
            FileHandler h = new FileHandler(ClientManager.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        }
    }
    
    public boolean createClient(long newClientId, Channel channel) {
        Client newClient;
        
        if (!clientsById.containsKey(newClientId) && !clientsByChannel.containsKey(channel)) {
            Random generator = new Random();
            newClient = new Client(newClientId, channel, new Color(generator.nextInt(256), generator.nextInt(256), generator.nextInt(256)));
            clientsById.put(newClientId, newClient);
            clientsByChannel.put(channel, newClient);
            if (ClientManager.loggingEnabled) {
                logger.info("Создан новый клиент id=" + newClientId);
            }
            return true;
        } else {
            if (ClientManager.loggingEnabled) {
                logger.info("Клиент id=" + newClientId + " не может быть создан, так как клиент с таким id уже существует.");
            }
            return false;
        }
    }
    
    public LinkedList<Message> takeFromOutbox(long clientId) {
        Client client;
        LinkedList<Message> clone;
        
        if ( (client = clientsById.get(clientId)) != null) {
            clone = client.takeFromOutbox();
            return clone;
        } else {
            return null;
        }
    }
    
    public boolean setLastReportedStep(long clientId, long step) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.setLastReportedStep(step);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setLastReportedStep(Channel channel, long step) {
        Client client;
        
        if ( (client = clientsByChannel.get(channel)) != null) {
            client.setLastReportedStep(step);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setLastConfirmedStep(long clientId, long step) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.setLastConfirmedStep(step);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean markDisconnected(long clientId) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.markDisconnected();
            return true;
        } else {
            return false;
        }
    }
    
    public boolean markDisconnected(Channel channel) {
        Client client;
        
        if ( (client = clientsByChannel.get(channel)) != null) {
            client.markDisconnected();
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setInitialized(long clientId) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.setInitialized();
            return true;
        } else {
            return false;
        }
    }

    public boolean setUninitialized(long clientId) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.setUninitialized();
            return true;
        } else {
            return false;
        }
    }
    
    public boolean disconnectClient(long clientId) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            clientsById.remove(clientId);
            clientsByChannel.remove(client.getChannel());
            return true;
        } else {
            return false;
        }
    }
    
    public Client getClient(long clientId) {
        return clientsById.get(clientId);
    }
    
    public Client getClient(Channel channel) {
        return clientsByChannel.get(channel);
    }
    
    public Channel getClientChannel(long clientId) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            return client.getChannel();
        } else {
            return null;
        }
    }
    
    public Channel getClientChannel(Channel channel) {
        Client client;
        
        if ( (client = clientsByChannel.get(channel)) != null) {
            return client.getChannel();
        } else {
            return null;
        }
    }
    
    public LinkedList<Client> getDisconnectedClients() {
        LinkedList<Client> disconnectedClients = new LinkedList<>();
        Entry pair;
        Client client;
        
        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (client.isDisconnected()) {
                disconnectedClients.add(client);
            }
        }
        
        return disconnectedClients;
    }
    
    public LinkedList<Client> getAwaitingInitializingClients() {
        LinkedList<Client> awaitingInitializing = new LinkedList<>();
        Entry pair;
        Client client;
        
        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (client.isAwaitingInitializing()) {
                awaitingInitializing.add(client);
            }
        }
        
        return awaitingInitializing;
    }
    
    /**
     * Этот метод не возвращает отключенных и ожидающих инициализации клиентов!
     */
    public LinkedList<Client> getClients() {
        LinkedList<Client> clients = new LinkedList<>();
        Entry pair;
        Client client;
        
        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isDisconnected() && !client.isAwaitingInitializing()) {
                clients.add(client);
            }
        }
        
        return clients;
    }
    
    public boolean isAllClientsReported(long step) {
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isDisconnected() && !client.isAwaitingInitializing() && client.getLastReportedStep() != step) {
                return false;
            }
        }

        return true;
    }
    
    public void notifyAllOtherClients(Client client, Message action) {
        Entry pair;
        Client curClient;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            curClient = (Client) pair.getValue();
            if (!curClient.equals(client)) {
                curClient.addToOubox(action);
            }
        }
    }
    
    public void notifyAllOtherClients(Client client, LinkedList<Message> actions) {
        Entry pair;
        Client curClient;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            curClient = (Client) pair.getValue();
            if (!curClient.equals(client)) {
                curClient.addAllToOubox(actions);
            }
        }
    }

    public boolean setClientPingTime(long clientId, long time) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.setClientPingTime(time);
            return true;
        } else {
            return false;
        }
    }
}
