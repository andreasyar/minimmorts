package minimmorts.messages;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.GlobalLogFile;
import minimmorts.client.Client;
import minimmorts.client.ClientManager;
import minimmorts.movement.Movement;
import minimmorts.server.ServerMainLoop;
import minimmorts.units.Unit;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * Обработчик сообщений. Принимает сообщения, расшифровывает сообщения, создаёт
 * сообщения, зашифровывает сообщения, отправляет сообщения.
 * @author Sorc
 */
public class MessageManager extends SimpleChannelUpstreamHandler {
    
    /**
     *
     */
    private static Logger logger = null;

    /**
     * Флаг включения логирования.
     */
    private static boolean loggingEnabled = false;

    /**
     * Список входящих сообщений от клиентов.
     */
    private final LinkedList<InboxMessage> commonInbox = new LinkedList<>();

    private final ClientManager clientManager;
    
    /**
     * Кодировщик и декодировщик сообщений в JSON.
     */
    private final Gson gson = new Gson();

    public static void enableLogging() {
        MessageManager.loggingEnabled = true;
    }

    public static void disableLogging() {
        MessageManager.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return MessageManager.loggingEnabled;
    }

    public MessageManager(ClientManager clientManager) throws IOException {
        if (MessageManager.logger == null) {
            MessageManager.logger = Logger.getLogger(MessageManager.class.getName());
            FileHandler h = new FileHandler(MessageManager.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        }
        
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
                    movement.getWaypoints()
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
            m.getWaypoints()
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
            m.getWaypoints()
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

    public static StepTimeMessage createStepTimeMessage(long newTime) {
        return new StepTimeMessage(newTime);
    }

    public static SyncErrorMessage createSyncErrorMessage(long unitId) {
        return new SyncErrorMessage(unitId);
    }
    
    /**
     * Обработчик разрыва соединения клиента с сервером.
     */
    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
        clientManager.addDisctonnectedClient(ctx.getChannel());
        if (MessageManager.isLoggingEnabled()) {
            logger.log(
                Level.INFO,
                "{0}: Связь с клиентом потеряна. Клиент channel={1} будет помечен как отключенный.",
                new Object[] {ServerMainLoop.getStep(), ctx.getChannel()}
            );
        }
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
        Client client;

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
                    if ( (client = clientManager.getClient(ctx.getChannel())) == null || !client.isConnected()) {
                        return;
                    }
                    
                    long step = Long.parseLong(gson.fromJson(messageFields.get(1), String.class));
                    
                    if ( (client = clientManager.getClient(ctx.getChannel())) == null) {
                        if (MessageManager.isLoggingEnabled()) {
                            logger.info(ServerMainLoop.getStep() + ": Клиент отправитель отчёта не существует на сервере. Отчёт будет проигнорирован. Клиент channel=" + ctx.getChannel() + " не найден.");
                        }
                        break;
                    }
                    if (!client.isConnected() || !client.isInitialized()) {
                        if (MessageManager.isLoggingEnabled()) {
                            logger.info(ServerMainLoop.getStep() + ": Отчёты от отключенных или не инициализированных клиентов не принимаются. Клиент id=" + client.getId() + " отключен или не инициализирован.");
                        }
                        break;
                    }
                    /*if (client.getLastReportedStep() != client.getLastConfirmedStep()) {
                        if (MessageManager.isLoggingEnabled()) {
                            logger.info(ServerMainLoop.getStep() + ": Последний отчёт не был подтверждён. Новые отчёты не принимаются. Клиент lastReportedStep=" + client.getLastReportedStep() + " lastConfirmedStep=" + client.getLastConfirmedStep());
                        }
                        break;
                    }*/
                    
                    JsonArray units = messageFields.get(2).getAsJsonArray();
                    JsonArray actions = messageFields.get(3).getAsJsonArray();
                    JsonArray unit;
                    JsonArray action;

                    LinkedList<Message> unitsList = new LinkedList<>();
                    LinkedList<Message> actionsList = new LinkedList<>();
                    UnitMessage unitMessage;
                    MoveRequestMessage moveRequestMessage;
                    String subType;
                    
                    boolean res;

                    for (JsonElement elem : units) {
                        unit = elem.getAsJsonArray();
                        subType = gson.fromJson(unit.get(0), String.class);
                        switch (subType) {
                            case "UNIT":
                                unitMessage = gson.fromJson(unit.get(1), UnitMessage.class);
                                unitsList.add(unitMessage);
                                break;
                            default:
                                //logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }

                    for (JsonElement elem : actions) {
                        action = elem.getAsJsonArray();
                        subType = gson.fromJson(action.get(0), String.class);
                        switch (subType) {
                            case "MOVEREQ":
                                moveRequestMessage = gson.fromJson(action.get(1), MoveRequestMessage.class);
                                actionsList.add(moveRequestMessage);
                                break;
                            default:
                                //logger.log(Level.SEVERE, "Unknown message type {0}", subType);
                                break;
                        }
                    }
                    
                    res = clientManager.setLastReportedStep(ctx.getChannel(), step);
                    if (res) {
                        synchronized (commonInbox) {
                            commonInbox.add(new InboxMessage(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList)));
                        }
                    } else if (ClientManager.isLoggingEnabled()) {
                        logger.info(ServerMainLoop.getStep() + ": Не удалось установить последний шаг клиента, за который он отчитался. Клиент channel=" + ctx.getChannel() + " не найден.");
                    }

                    break;
                case "PING":
                    if ( (client = clientManager.getClient(ctx.getChannel())) == null || !client.isConnected()) {
                        return;
                    }
                    
                    /*PingMessage pm = gson.fromJson(messageFields.get(1), PingMessage.class);
                    if ( (client = clientManager.getClient(ctx.getChannel())) == null) {
                        if (ClientManager.isLoggingEnabled()) {
                            logger.info(ServerMainLoop.getStep() + ": Сообщение PING не может быть обработано. Клиент channel=" + ctx.getChannel() + " не найден.");
                        }
                        break;  // Не обрабатываем дальше это сообщение.
                    }
                    if (client.getClientPingTime() != 0) {
                        if (pm.getTime() - client.getClientPingTime() > 500 && ClientManager.isLoggingEnabled()) {
                            logger.log(Level.INFO, "Client id={0} JS become extremely slow! {1}", new Object[] {client.getId(), pm.getTime() - client.getClientPingTime()});
                        }
                    }
                    if (!clientManager.setClientPingTime(client.getId(), pm.getTime()) && ClientManager.isLoggingEnabled()) {
                        logger.info(ServerMainLoop.getStep() + ": Не удалось установить время последнего пинга. Клиент id=" + client.getId() + " не найден.");
                    }*/
                    ctx.getChannel().write(frame);
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
        if (channel.isWritable()) {
            channel.write(new TextWebSocketFrame(jsonMessage));
        } else {
            clientManager.addDisctonnectedClient(channel);
            if (MessageManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Невозможно отправить сообщение. Связь с клиентом потеряна. Клиент channel={1} будет помечен как отключенный.",
                    new Object[] {ServerMainLoop.getStep(), channel}
                );
            }
        }
    }

    /**
     * Отправляет клиенту сообщение.
     * 
     * @param message сообщение
     */
    public void sendMessage(Channel channel, Message message) {
        String jsonMessage = message.toJSON();
        if (channel.isWritable()) {
            channel.write(new TextWebSocketFrame(jsonMessage));
        } else if (isLoggingEnabled()) {
            logger.warning("Невозможно отправить сообщение. Channel=" + channel + " is not writable.");
        }
    }

    /**
     * Отправляет клиенту сообщение об ошибке и отключает клиента от сервера.
     * @param channel канал
     * @param message сообщение
     */
    public void sendErrorMessage(Channel channel, ErrorMessage message) {
        String jsonMessage = message.toJSON();
        if (channel.isWritable()) {
            channel.write(new TextWebSocketFrame(jsonMessage)).addListener(new ChannelTerminator(channel));
        } else if (isLoggingEnabled()) {
            logger.warning("Невозможно отправить сообщение об ошибке. Channel=" + channel + " is not writable.");
        }
    }
}
