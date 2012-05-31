package minimmorts.messages;

import org.jboss.netty.channel.Channel;

/**
 *
 * @author andrey
 */
public class InboxMessage {
    
    /**
     * Ссылка на канал, из которого пришло сообщение.
     */
    private final Channel from;
    
    /**
     * Сообщение.
     */
    private final Message message;

    public InboxMessage(Channel from, Message message) {
        this.from = from;
        this.message = message;
    }

    public Channel getFrom() {
        return from;
    }

    public Message getMessage() {
        return message;
    }
}
