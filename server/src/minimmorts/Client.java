package minimmorts;

import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;
import minimmorts.messages.Message;
import org.jboss.netty.channel.Channel;

/**
 *
 * @author Sorc
 */
public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    
    private long Id;
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
     * Канал связи с клиентом.
     */
    private final Channel channel;
    
    /**
     * Пометка клиента как отключенного от сервера.
     */
    private boolean disconnected;

    Client(Channel channel) {
        this.Id = IdGenerator.generateId();
        this.channel = channel;
    }

    long getId() {
        return Id;
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

    long getLastReportedStep() {
        return lastReportedStep;
    }

    void setLastReportedStep(long lastReportedStep) {
        this.lastReportedStep = lastReportedStep;
    }

    public long getLastConfirmedStep() {
        return lastConfirmedStep;
    }

    public void setLastConfirmedStep(long lastAcceptedStep) {
        this.lastConfirmedStep = lastAcceptedStep;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void setDisconnected(boolean disconnected) {
        this.disconnected = disconnected;
    }
}
