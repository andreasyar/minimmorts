package minimmorts.websocketserver;

import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.util.CharsetUtil;

class WebSocketServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(WebSocketServerHandler.class.getName());
    private static final String WEBSOCKET_PATH = "/";

    private WebSocketServerHandshaker handshaker;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object msg = e.getMessage();

        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }

            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(this.getWebSocketLocation(req), null, false);
            this.handshaker = wsFactory.newHandshaker(req);
            if (this.handshaker == null) {
                wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            } else {
                this.handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
            }
        } else if (msg instanceof WebSocketFrame) {
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof CloseWebSocketFrame) {
                this.handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
                return;
            } else if (frame instanceof PingWebSocketFrame) {
                ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
                return;
            } else if (!(frame instanceof TextWebSocketFrame)) {
                throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
            }

            // Передаём сообщение дальше по конвейеру.
            ctx.sendUpstream(e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ClosedChannelException) {
            logger.log(Level.WARNING, "Probably attempt to write to closed channel.");
            return;
        }
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getWebSocketLocation(HttpRequest req) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
    }
}
