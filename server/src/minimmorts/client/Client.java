package minimmorts.client;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.messages.Message;
import org.jboss.netty.channel.Channel;

/**
 * 
 * @author Sorc
 */
public class Client {
    /**
     * 
     */
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    
    /**
     * Идентификатор клиента.
     */
    private long id;

    /**
     * Канал связи с клиентом.
     */
    private final Channel channel;
    
    /**
     * Список исходящих сообщений от сервера клиенту.
     */
    private final LinkedList<Message> outbox = new LinkedList<>();

    /**
     * Номер последнего шага, за который отчитался клиент.
     */
    private long lastReportedStep = -1;

    /**
     * Номер последнего шага, состояние на котором было подтверждено.
     */
    private long lastConfirmedStep = -1;
    
    /**
     * Цвет юнитов.
     */
    private Color color;
    
    private boolean connected = false;
    
    private boolean initialized = false;
    
    private boolean reported = false;
    
    private int reinitializationAttempts = 0;
    
    private long clientPingTime = 0;

    Client(long id, Channel channel, Color color) {
        this.id = id;
        this.channel = channel;
        this.color = color;
    }

    public long getId() {
        return id;
    }

    Channel getChannel() {
        return channel;
    }

    void addToOubox(Message m) {
        outbox.add(m);
    }

    void addAllToOubox(Collection<Message> m) {
        outbox.addAll(m);
    }

    LinkedList<Message> takeFromOutbox() {
        LinkedList<Message> clone = (LinkedList<Message>) outbox.clone();
        outbox.clear();
        return clone;
    }

    public long getLastReportedStep() {
        return lastReportedStep;
    }

    void setLastReportedStep(long step) {
        this.lastReportedStep = step;
    }

    public long getLastConfirmedStep() {
        return lastConfirmedStep;
    }

    void setLastConfirmedStep(long step) {
        this.lastConfirmedStep = step;
    }

    public Color getColor() {
        return color;
    }

    public boolean isConnected() {
        return connected;
    }

    void setConnected() {
        connected = true;
    }

    void setDisconnected() {
        connected = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    void setInitialized() {
        initialized = true;
    }

    void setUninitialized() {
        initialized = false;
    }

    public boolean isReported() {
        return reported;
    }

    void setReported() {
        reported = true;
    }

    void setUnreported() {
        reported = false;
    }

    public int getReinitializationAttempts() {
        return reinitializationAttempts;
    }

    void incReinitializationAttempts() {
        reinitializationAttempts++;
    }

    void clearReinitializationAttempts() {
        reinitializationAttempts = 0;
    }

    public LinkedList<Message> cloneOutbox() {
        return (LinkedList<Message>) outbox.clone();
    }

    public long getClientPingTime() {
        return clientPingTime;
    }

    void setClientPingTime(long clientPingTime) {
        this.clientPingTime = clientPingTime;
    }
}
