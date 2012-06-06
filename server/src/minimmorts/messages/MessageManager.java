package minimmorts.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.client.Client;
import minimmorts.client.ClientManager;
import minimmorts.movement.Movement;
import minimmorts.units.Unit;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Обработчик сообщений. Принимает сообщения, расшифровывает сообщения, создаёт
 * сообщения, зашифровывает сообщения, отправляет сообщения.
 * @author Sorc
 */
public class MessageManager extends SimpleChannelUpstreamHandler {
    private static final Logger logger = Logger.getLogger(MessageManager.class.getName());

    /**
     * Список входящих сообщений от клиентов.
     */
    private final LinkedList<InboxMessage> commonInbox = new LinkedList<>();

    private final ClientManager clientManager;
    
    /**
     * Кодировщик и декодировщик сообщений в JSON.
     */
    private final Gson gson = new Gson();

    public MessageManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /**
     * Создаёт сообщения об удалении юнитов.
     * @param removedUnits удалённые юниты
     * @return Сообщения об удалении юнитов.
     */
    public static LinkedList<Message> createAllDelUnitMessage(LinkedList<Unit> removedUnits) {
        LinkedList<Message> delUnitMessages = new LinkedList<>();
        for (Unit unit : removedUnits) {
            delUnitMessages.add(new DelUnitMessage(unit.getId()));
        }
        return delUnitMessages;
    }

    /**
     * Создаёт сообщение об ошибке.
     * @param text текст ошибки
     * @return Сообщение об ошибке.
     */
    public static ErrorMessage createErrorMessage(String text) {
        return new ErrorMessage(text);
    }

    /**
     * Создаёт сообщения о движении.
     * @param movements движения
     * @return Сообщения о движении.
     */
    public static LinkedList<Message> createAllMoveMessage(LinkedList<Movement> movements) {
        LinkedList<Message> moveMessages = new LinkedList<>();
        for (Movement movement : movements) {
            moveMessages.add(
                new MoveMessage(
                    movement.getBegStep(),
                    movement.getUnit().getId(),
                    movement.getBegJ(),
                    movement.getBegI(),
                    movement.getEndJ(),
                    movement.getEndI()
                )
            );
        }
        return moveMessages;
    }

    /**
     * Создаёт сообщения о появлении новых юнитов.
     * @param units новые юниты
     * @return Сообщения о появлении новых юнитов.
     */
    public static LinkedList<Message> createAllNewUnitMessage(LinkedList<Unit> units) {
        LinkedList<Message> newUnitMessages = new LinkedList<>();
        for (Unit unit : units) {
            newUnitMessages.add(new NewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI(), unit.getColor().getRed(), unit.getColor().getGreen(), unit.getColor().getBlue()));
        }
        return newUnitMessages;
    }
    
    /**
     * Создаёт сообщение о движении.
     * @param m движение
     * @return Сообщение о движении.
     */
    public static MoveMessage createMoveMessage(Movement m) {
        return new MoveMessage(
            m.getBegStep(),
            m.getUnit().getId(),
            m.getBegJ(),
            m.getBegI(),
            m.getEndJ(),
            m.getEndI()
        );
    }
    
    /**
     * Создаёт сообщение об обновлении пути движения.
     * @param m обновлённое движение
     * @return Сообщение об обновлении пути движения.
     */
    public static MoveUpdateMessage createMoveUpdateMessage(Movement m) {
        return new MoveUpdateMessage(
            m.getBegStep(),
            m.getUnit().getId(),
            m.getBegJ(),
            m.getBegI(),
            m.getEndJ(),
            m.getEndI()
        );
    }

    /**
     * Создаёт сообщение о появлении нового юнита.
     * @param unit новый юнит
     * @return Сообщение о появлении нового юнита.
     */
    public static NewUnitMessage createNewUnitMessage(Unit unit) {
        return new NewUnitMessage(unit.getOwnerId(), unit.getId(), unit.getJ(), unit.getI(), unit.getColor().getRed(), unit.getColor().getGreen(), unit.getColor().getBlue());
    }
    
    /**
     * Обработчик разрыва соединения клиента с сервером.
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        clientManager.markDisconnected(ctx.getChannel());
    }

    /**
     * Обработчик получения сервером сообщения от клиента.
     */
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
                    /*if (!gameServer.isClientExists(ctx.getChannel())) {
                        break;
                    }*/
                    
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
                    clientManager.setLastReportedStep(ctx.getChannel(), step);
                    break;
                case "PING":
                    PingMessage pm = gson.fromJson(messageFields.get(1), PingMessage.class);
                    Client client = clientManager.getClient(ctx.getChannel());
                    if (client.getClientPingTime() != 0) {
                        if (pm.getTime() - client.getClientPingTime() > 200) {
                            logger.log(Level.SEVERE, "Client {0} JS become extremely slow! {1}", new Object[] {client.getId(), pm.getTime() - client.getClientPingTime()});
                        }
                    }
                    clientManager.setClientPingTime(client.getId(), pm.getTime());
                    break;
            }
        }
    }

    /**
     * Возвращает входящие сообщения от клиентов, очищая список входящих сообщений.
     * @return Входящие сообщения от клиентов.
     */
    public LinkedList<InboxMessage> takeFromInbox() {
        LinkedList<InboxMessage> clone;

        synchronized (commonInbox) {
            clone = (LinkedList<InboxMessage>) commonInbox.clone();
            commonInbox.clear();
        }

        return clone;
    }

    /**
     * Отправляет клиенту подтверждение хода.
     * 
     * @param step номер хода
     * @param messages уведомления о событиях за подтверждаемый ход
     */
    public void sendConfirm(Channel channel, long step, LinkedList<Message> messages) {
        String jsonMessage = new ConfirmMessage(step, messages).toJSON();
        //logger.log(Level.INFO, "Attempt to send confirm: {0}", jsonMessage);
        channel.write(new TextWebSocketFrame(jsonMessage));
    }

    /**
     * Отправляет клиенту сообщение.
     * 
     * @param message сообщение
     */
    public void sendMessage(Channel channel, Message message) {
        String jsonMessage = message.toJSON();
        //logger.log(Level.INFO, "Attempt to send message: {0}", jsonMessage);
        channel.write(new TextWebSocketFrame(jsonMessage));
    }

    /**
     * Отправляет клиенту сообщение об ошибке и отключает клиента от сервера.
     * @param channel канал
     * @param message сообщение
     */
    public void sendErrorMessage(Channel channel, ErrorMessage message) {
        String jsonMessage = message.toJSON();
        //logger.log(Level.INFO, "Attempt to send message: {0}", jsonMessage);
        channel.write(new TextWebSocketFrame(jsonMessage)).addListener(new ChannelTerminator(channel));
    }
}
