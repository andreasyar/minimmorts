package minimmorts.client;

import java.awt.Color;
import java.io.IOException;
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
     * Флаг включения логирования.
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

    /**
     *
     */
    private final LinkedList<Channel> disconnectedClients = new LinkedList<>();
    
    private static final int MAX_REINITIALIZATION_ATTEMPTS = 5;

    public static void enableLogging() {
        ClientManager.loggingEnabled = true;
    }

    public static void disableLogging() {
        ClientManager.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return loggingEnabled;
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

    /**
     * Создаёт нового клиента и помещает его в хеши клиентов и каналов.
     * @param newClientId идентификатор нового клиента
     * @param channel канал нового клиента
     * @return возвращает <b>true</b>, если клиент был успешно создан или
     * <b>false</b>, если клиент с таким идентификатором или с таким каналом
     * уже существует
     */
    public boolean createClient(long newClientId, Channel channel) {
        Client newClient;

        if (!clientsById.containsKey(newClientId) && !clientsByChannel.containsKey(channel)) {
            Random generator = new Random();
            newClient = new Client(newClientId, channel, new Color(generator.nextInt(256), generator.nextInt(256), generator.nextInt(256)));
            clientsById.put(newClientId, newClient);
            clientsByChannel.put(channel, newClient);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Возвращает исходящие сообщения клиента, при этом очищая его "ящик"
     * исходящих сообщений.
     * @param clientId идентификатор клиента
     * @return возвращает список исходящих сообщений клиента или <b>null</b>,
     * если клиент не был найден по его идентификатору с хеше клиентов
     */
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

    /**
     * Устанавливает последний шаг клиента, за который он отчитался.
     * @param channel канал клиента
     * @param step шаг
     * @return возвращает <b>true</b>, если последний шаг, за которой клиент
     * отчитался был успешно установлен или <b>false</b>, если клиент не был
     * найден по своему каналу в хеше каналов
     */
    public boolean setLastReportedStep(Channel channel, long step) {
        Client client;

        if ( (client = clientsByChannel.get(channel)) != null) {
            client.setLastReportedStep(step);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Устанавливает последний подтверждённый шаг клиента.
     * @param clientId идентификатор клиента
     * @param step шаг
     * @return возвращает <b>true</b>, если последний подтверждённый шаг клиента
     * был успешно установлен или <b>false</b>, если клиент не был найден по
     * своему идентификатору в хеше клиентов
     */
    public boolean setLastConfirmedStep(long clientId, long step) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setLastConfirmedStep(step);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Помечает клиент как отключенного.
     * @param clientId идентификатор клиента
     * @return возвращает <b>true</b>, если клиент был успешно помечен как отключенный
     * или <b>false</b>, если клиент не был найден по своему идентификатору в хеше клиентов
     */
    public boolean setDisconnected(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setDisconnected();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Помечает клиент как отключенного.
     * @param channel канал клиента
     * @return возвращает <b>true</b>, если клиент был успешно помечен как отключенный
     * или <b>false</b>, если клиент не был найден по своему каналу в хеше каналов
     */
    public boolean setDisconnected(Channel channel) {
        Client client;

        if ( (client = clientsByChannel.get(channel)) != null) {
            client.setDisconnected();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Помечает клиента как инициализированного.
     * @param clientId идентификатор клиента
     * @return возвращает <b>true</b>, если клиент был успешно помечен как инициализированный
     * или <b>false</b>, если клиент не был найден по своему идентификатору с хеше клиентов
     */
    public boolean setInitialized(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setInitialized();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Помечает клиента как неинициализированного.
     * @param clientId идентификатор клиента
     * @return возвращает <b>true</b>, если клиент был успешно помечен как неинициализированный
     * или <b>false</b>, если клиент не был найден по своему идентификатору с хеше клиентов
     */
    public boolean setUninitialized(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setUninitialized();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Удаляет клиента из хеша клиентов и каналов, тем самым останавливая
     * обслуживание клиента.
     * @param clientId идентификатор удаляемого клиента
     * @return возвращает <b>true</b> если клиент был успешно удалён и <b>false</b>
     * если клиент не был найден по своему идентификатору с хеше клиентов
     */
    public boolean removeClient(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            clientsById.remove(clientId);
            clientsByChannel.remove(client.getChannel());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Возвращает клиента по каналу.
     * @param channel канал клиента
     * @return возвращает клиента или <b>null</b>, если клиент не найден по
     * своему каналу в хеше каналов
     */
    public Client getClient(Channel channel) {
        return clientsByChannel.get(channel);
    }

    /**
     * Возвращает канал клиента по идентификатору клиента.
     * @param clientId идентификатор клиента
     * @return возвращает канал клиента или <b>null</b>, если клиент не найден по
     * своему идентификатору в хеше клиентов
     */
    public Channel getClientChannel(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            return client.getChannel();
        } else {
            return null;
        }
    }

    /**
     * Возвращает клиентов, которые помеченны как отключенные.
     * @return список отключенных клиентов
     */
    public LinkedList<Client> getDisconnectedClients() {
        LinkedList<Client> disconnectedClients = new LinkedList<>();
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isConnected()) {
                disconnectedClients.add(client);
            }
        }

        return disconnectedClients;
    }

    /**
     * Возвращает клиентов, которые помеченны как неинициализированные.
     * @return список неинициализированных клиентов
     */
    public LinkedList<Client> getUninitializedClients() {
        LinkedList<Client> uninitializedClients = new LinkedList<>();
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isInitialized()) {
                uninitializedClients.add(client);
            }
        }

        return uninitializedClients;
    }
    
    public LinkedList<Client> getUnreportedClients() {
        LinkedList<Client> unreportedClients = new LinkedList<>();
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (!client.isReported()) {
                unreportedClients.add(client);
            }
        }

        return unreportedClients;
    }

    /**
     * Возвращает активных клиентов, которые не помеченны как отключенные и
     * как неинициализированные.
     * @return список активных клиентов
     */
    public LinkedList<Client> getClients() {
        LinkedList<Client> clients = new LinkedList<>();
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (client.isConnected() && client.isInitialized() && client.isReported()) {
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
            if (client.getLastReportedStep() != step && client.isConnected()) {
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
    
    public void addDisctonnectedClient(Channel c) {
        synchronized (disconnectedClients) {
            disconnectedClients.add(c);
        }
    }
    
    public void markDisconnectedClients() {
        synchronized (disconnectedClients) {
            for (Channel c : disconnectedClients) {
                setDisconnected(c);
            }
            disconnectedClients.clear();
        }
    }
    
    public void markUnreportedClients(long step) {
        Entry pair;
        Client client;

        Iterator i = clientsById.entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            client = (Client) pair.getValue();
            if (client.getLastReportedStep() != step && client.isConnected()) {
                if (client.getReinitializationAttempts() >= MAX_REINITIALIZATION_ATTEMPTS) {
                    client.setDisconnected();
                } else {
                    client.setUnreported();
                }
            }
        }
    }

    public boolean setConnected(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setConnected();
            return true;
        } else {
            return false;
        }
    }

    public boolean setReported(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.setReported();
            return true;
        } else {
            return false;
        }
    }

    public boolean incReinitializationAttempts(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.incReinitializationAttempts();
            return true;
        } else {
            return false;
        }
    }

    public boolean clearReinitializationAttempts(long clientId) {
        Client client;

        if ( (client = clientsById.get(clientId)) != null) {
            client.clearReinitializationAttempts();
            return true;
        } else {
            return false;
        }
    }
}
