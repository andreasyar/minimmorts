package minimmorts.messages;

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
                    movement.getWaypoints2()
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
            m.getWaypoints2()
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
            m.getWaypoints2()
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
        if (!request.contains(",")) {
            // TODO: Может быть добавить сообщение в лог или какую-то другую реакцию?
            return;
        }
        
        String[] messageArray = request.split(",");
        String type = messageArray[0];
        Client client;

        switch (type) {
            case "LOGIN":
                // Example: LOGIN,test,test

                synchronized (commonInbox) {
                    commonInbox.add(new InboxMessage(ctx.getChannel(), new LoginMessage(messageArray[1], messageArray[2])));
                }
                break;
            case "PING":
                // Example: PING,1342373917083

                if ( (client = clientManager.getClient(ctx.getChannel())) == null || !client.isConnected()) {
                    // TODO: Может быть добавить сообщение в лог или какую-то другую реакцию?
                    return;
                }

                ctx.getChannel().write(frame);
                break;
            case "STATE":
                // Example: STATE,65,2,1,2,69,54,1,3,70,55,2,MOVEREQ,2,68,58,MOVEREQ,3,67,57

                if ( (client = clientManager.getClient(ctx.getChannel())) == null || !client.isConnected()) {
                    // TODO: Может быть добавить сообщение в лог или какую-то другую реакцию?
                    return;
                }

                long step = Long.parseLong(messageArray[1]);

                int unitsCount = Integer.parseInt(messageArray[2]);
                LinkedList<Message> unitsList = new LinkedList<>();
                int u = 3;
                for (int i = 0; i < unitsCount; i++, u += 4) {
                    unitsList.add(
                        new UnitMessage(
                            Long.parseLong(messageArray[u + 0]),
                            Long.parseLong(messageArray[u + 1]),
                            Integer.parseInt(messageArray[u + 2]),
                            Integer.parseInt(messageArray[u + 3])
                        )
                    );
                }

                int actionsCount = Integer.parseInt(messageArray[u]);
                LinkedList<Message> actionsList = new LinkedList<>();
                if (actionsCount > 0) {
                    int a = u + 1;
                    String actionType;
                    for (int i = 0; i < actionsCount; i++) {
                        actionType = messageArray[a + 0];
                        switch (actionType) {
                            case "MOVEREQ":
                                actionsList.add(
                                    new MoveRequestMessage(
                                        Long.parseLong(messageArray[a + 1]),
                                        Integer.parseInt(messageArray[a + 2]),
                                        Integer.parseInt(messageArray[a + 3])
                                    )
                                );
                                a += 4;
                                break;
                        }
                    }
                }

                boolean res = clientManager.setLastReportedStep(ctx.getChannel(), step);
                if (res) {
                    synchronized (commonInbox) {
                        commonInbox.add(new InboxMessage(ctx.getChannel(), new StateReportMessage(step, unitsList, actionsList)));
                    }
                } else {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Не удалось установить последний шаг клиента, за который он отчитался. Клиент channel={1} не найден.",
                        new Object[] {ServerMainLoop.getStep(), ctx.getChannel()}
                    );
                }
                break;
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
