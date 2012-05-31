package minimmorts.websocketserver;

import minimmorts.Server;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import static org.jboss.netty.channel.Channels.pipeline;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class WebSocketServerPipelineFactory implements ChannelPipelineFactory {
    private Server gameServer;

    public WebSocketServerPipelineFactory(Server gameServer) {
        this.gameServer = gameServer;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("handler", new WebSocketServerHandler());
        pipeline.addLast("jsonDecoder", gameServer.getMessageManager());
        return pipeline;
    }
}
