package minimmorts.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.Client;
import minimmorts.Server;
import minimmorts.movement.Movement;
import minimmorts.units.Unit;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 *
 * @author Sorc
 */
public class MessageManager extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(MessageManager.class.getName());

    private final LinkedList<InboxMessage> commonInbox = new LinkedList<>();

    private Server gameServer;
    private final Gson gson = new Gson();

    public static StateReportMessage createStateReportMessage(long step, LinkedList<Unit> units, LinkedList<Message> actions) {
        LinkedList<Message> unitMessages = new LinkedList<>();

        for (Unit u : units) {
            unitMessages.add(new UnitMessage(u.getOwnerId(), u.getId(), u.getJ(), u.getI()));
        }

        return new StateReportMessage(step, unitMessages, actions);
    }
    
    public static MoveMessage createMoveMessage(Movement m) {
        return new MoveMessage(m.getBegStep(), m.getUnit().getId(), m.getJ(), m.getI());
    }

    public static LinkedList<Message> createAllDelUnitMessage(LinkedList<Unit> removedUnits) {
        LinkedList<Message> delUnitMessages = new LinkedList<>();
        for (Unit unit : removedUnits) {
            delUnitMessages.add(new DelUnitMessage(unit.getId()));
        }
        return delUnitMessages;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        Object frame = e.getMessage();
        String request = ((TextWebSocketFrame) frame).getText();
        JsonParser parser = new JsonParser();

        /*
         * Распарсим сообщение, все сообщения представляют собой массив [],
         * который содержит массивы с сообщениями. Эти массивы с сообщениями
         * имеют формат ["TYPE", ...]. Нулевой элемент задаёт тип сообщения,
         * дальше идут различные поля, которые зависят от типа сообщения.
         * [["TYPE", ...], ["TYPE", ...], ...]
         */
        JsonArray jsonMessageArray = parser.parse(request).getAsJsonArray();
        JsonArray messageFields;
        String type;

        for (JsonElement jsonMessage : jsonMessageArray) {
            messageFields = jsonMessage.getAsJsonArray();
            type = gson.fromJson(messageFields.get(0), String.class);

            switch (type) {
                case "LOGIN":
                    LoginMessage lm = gson.fromJson(messageFields.get(1), LoginMessage.class);
                    synchronized (commonInbox) {
                        commonInbox.add(new InboxMessage(ctx.getChannel(), lm));
                    }
                    break;
                case "STATE":
                    if (!gameServer.isClientExists(ctx.getChannel())) {
                        break;
                    }
                    
                    long step = Long.parseLong(gson.fromJson(messageFields.get(1), String.class));
                    JsonArray units = messageFields.get(2).getAsJsonArray();
                    JsonArray actions = messageFields.get(3).getAsJsonArray();
                    JsonArray unit;
                    JsonArray action;

                    LinkedList<Message> unitsList = new LinkedList<>();
                    LinkedList<Message> actionsList = new LinkedList<>();
                    UnitMessage unitMessage;
                    MoveMessage moveMessage;
                    String subType;

                    for (JsonElement elem : units) {
                        unit = elem.getAsJsonArray();
                        subType = gson.fromJson(unit.get(0), String.class);
                        switch (subType) {
                            case "UNIT":
                                unitMessage = gson.fromJson(unit.get(1), UnitMessage.class);
                                unitsList.add(unitMessage);
                                break;
                            default:
                                logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    for (JsonElement elem : actions) {
                        action = elem.getAsJsonArray();
                        subType = gson.fromJson(action.get(0), String.class);
                        switch (subType) {
                            case "MOVE":
                                moveMessage = gson.fromJson(action.get(1), MoveMessage.class);
                                actionsList.add(moveMessage);
                                break;
                            default:
                                logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    synchronized (commonInbox) {
                        commonInbox.add(new InboxMessage(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList)));
                    }
                    gameServer.clientReported(ctx.getChannel(), step);
                    break;
            }
        }
    }
    
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        gameServer.clientDisconnected(ctx.getChannel());
    }

    public void setGameServer(Server gameServer) {
        this.gameServer = gameServer;
    }

    public NewUnitMessage createNewUnitMessage(long ownerId, long id, int j, int i) {
        return new NewUnitMessage(ownerId, id, j, i);
    }

    public NewUnitMessage createNewUnitMessage(Unit unit) {
        return new NewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI());
    }

    public LinkedList<Message> createAllNewUnitMessage(LinkedList<Unit> units) {
        LinkedList<Message> newUnitMessages = new LinkedList<>();
        for (Unit unit : units) {
            newUnitMessages.add(new NewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI()));
        }
        return newUnitMessages;
    }

    public LinkedList<Message> createAllMoveMessage(LinkedList<Movement> movements) {
        LinkedList<Message> moveMessages = new LinkedList<>();
        for (Movement movement : movements) {
            moveMessages.add(new MoveMessage(movement.getBegStep(), movement.getUnit().getId(), movement.getJ(), movement.getI()));
        }
        return moveMessages;
    }

    public LinkedList<DelUnitMessage> createDelUnitMessages(LinkedList<Unit> excessUnits) {
        LinkedList<DelUnitMessage> delUnitMessages = new LinkedList<>();
        for (Unit u : excessUnits) {
            delUnitMessages.add(new DelUnitMessage(u.getId()));
        }
        return delUnitMessages;
    }

    public ErrorMessage createErrorMessage(String text) {
        return new ErrorMessage(text);
    }

    public LinkedList<InboxMessage> takeFromInbox() {
        LinkedList<InboxMessage> clone;

        synchronized (commonInbox) {
            clone = (LinkedList<InboxMessage>) commonInbox.clone();
            commonInbox.clear();
        }

        return clone;
    }

    public void sendConfirm(Client client, long step, LinkedList<Message> messages) {
        if (!client.isDisconnected()) {
            String packet = new ConfirmMessage(step, messages).toJSON();
            logger.log(Level.INFO, "Attempt to send confirm: {0}", packet);
            client.getChannel().write(new TextWebSocketFrame(packet));
        } else {
            logger.log(Level.WARNING, "Can't send message to disconnected client.");
        }
    }

    public void sendMessage(Client client, Message message) {
        if (!client.isDisconnected()) {
            String packet = message.toJSON();
            logger.log(Level.INFO, "Attempt to send packet: {0}", packet);
            client.getChannel().write(new TextWebSocketFrame(packet));
        } else {
            logger.log(Level.WARNING, "Can't send message to disconnected client.");
        }
    }
}
