package minimmorts.client;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;
import minimmorts.messages.Message;
import org.jboss.netty.channel.Channel;

/**
 * 
 * @author Sorc
 */
public class Client {
    
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
     * Пометка клиента как отключенного от сервера.
     */
    private boolean disconnected = false;
    
    private boolean awaitingInitializing = true;
    
    private long clientPingTime = 0;
    
    private Color color;

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

    public boolean isDisconnected() {
        return disconnected;
    }

    void markDisconnected() {
        this.disconnected = true;
    }

    public boolean isAwaitingInitializing() {
        return awaitingInitializing;
    }

    void setInitialized() {
        this.awaitingInitializing = false;
    }

    public long getClientPingTime() {
        return clientPingTime;
    }

    void setClientPingTime(long clientPingTime) {
        this.clientPingTime = clientPingTime;
    }

    public Color getColor() {
        return color;
    }
}
