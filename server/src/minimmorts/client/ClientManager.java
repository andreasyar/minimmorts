package minimmorts.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import minimmorts.messages.Message;
import org.jboss.netty.channel.Channel;

/**
 * Менеджер клиентов. Занимается управлением клиентами: созданием, хранением,
 * удалением, изменением статусов клиентов и т.д.
 * @author Sorc
 */
public class ClientManager {

    /**
     * Хеш клиентов. id -> Клиент.
     */
    private final ConcurrentHashMap<Long, Client> clientsById = new ConcurrentHashMap();

    /**
     * Хеш клиентов. Канал -> Клиент.
     */
    private final ConcurrentHashMap<Channel, Client> clientsByChannel = new ConcurrentHashMap();
    
    public boolean createClient(long newClientId, Channel channel) {
        Client newClient;
        
        if (!clientsById.containsKey(newClientId) && !clientsByChannel.containsKey(channel)) {
            newClient = new Client(newClientId, channel);
            clientsById.put(newClientId, newClient);
            clientsByChannel.put(channel, newClient);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean addToOubox(long clientId, Message message) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.addToOubox(message);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean addAllToOubox(long clientId, Collection<Message> messages) {
        Client client;
        
        if ( (client = clientsById.get(clientId)) != null) {
            client.addAllToOubox(messages);
            return true;
        } else {
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
    
    public boolean markDisconnected(long id) {
        Client client;
        
        if ( (client = clientsById.get(id)) != null) {
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
    
    public boolean setInitialized(long id) {
        Client client;
        
        if ( (client = clientsById.get(id)) != null) {
            client.setInitialized();
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
    
    public Client getClient(long id) {
        return clientsById.get(id);
    }
    
    public Client getClient(Channel channel) {
        return clientsByChannel.get(channel);
    }
    
    public Channel getClientChannel(long id) {
        Client client;
        
        if ( (client = clientsById.get(id)) != null) {
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
}
