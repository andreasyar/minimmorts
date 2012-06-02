package minimmorts.messages;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

/**
 * Закрывает канал после завершения асинхронной операции, для которой он был
 * добавлен в качестве слушателя.
 * @author Sorc
 */
class ChannelTerminator implements ChannelFutureListener {
    
    /**
     * Канал для закрытия.
     */
    private final Channel channel;

    ChannelTerminator(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void operationComplete(ChannelFuture cf) throws Exception {
        this.channel.close();
    }
}
