package minimmorts.server;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.ErrnoErrorService;
import minimmorts.GlobalLogFile;
import minimmorts.IdGenerator;
import minimmorts.LastErrorService;
import minimmorts.client.Client;
import minimmorts.client.ClientManager;
import minimmorts.map.Map;
import minimmorts.messages.*;
import minimmorts.movement.Movement;
import minimmorts.movement.MovementManager;
import minimmorts.units.Unit;
import minimmorts.units.UnitManager;
import org.jboss.netty.channel.Channel;

/**
 *
 * @author Sorc
 */
public class ServerMainLoop implements Runnable {
    
    /**
     *
     */
    private static Logger logger = null;
    
    /**
     *
     */
    private static boolean loggingEnabled = false;

    /**
     *
     */
    private final UnitManager unitManager;
    
    /**
     *
     */
    private final MovementManager movementManager;
    
    /**
     *
     */
    private final MessageManager messageManager;
    
    /**
     *
     */
    private final ClientManager clientManager;
    
    /**
     * Карта. TODO: Карта и работа с картой должны быть в отдельном пакете.
     */
    private final Map map;
    
    /**
     * Номер хода.
     */
    private static long step = 1;
        
    /**
     *
     */
    private final Random randomGenerator = new Random();

    /**
     * Список запросов действий от всех клиентов на текущем шаге.
     */
    private final LinkedList<ActionRequest> actionsRequests = new LinkedList<>();
    
    /**
     *
     */
    private final LinkedList<State> states = new LinkedList();

    public static void enableLogging() {
        ServerMainLoop.loggingEnabled = true;
    }

    public static void disableLogging() {
        ServerMainLoop.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return ServerMainLoop.loggingEnabled;
    }

    public static long getStep() {
        return step;
    }

    public ServerMainLoop(UnitManager unitManager, MovementManager movementManager, MessageManager messageManager, ClientManager clientManager) throws IOException {
        if (ServerMainLoop.logger == null) {
            ServerMainLoop.logger = Logger.getLogger(ServerMainLoop.class.getName());
            FileHandler h = new FileHandler(ServerMainLoop.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        }
        
        this.unitManager = unitManager;
        this.movementManager = movementManager;
        this.messageManager = messageManager;
        this.clientManager = clientManager;
        this.map = new Map(100, 100);
    }

    @Override
    public void run() {
        MoveMessage moveMessage;
        StateReportMessage stateReportMessage;
        LoginMessage loginMessage;

        Iterator clientsIterator;
        Iterator actionRequestsIterator;
        Iterator newClientsIterator;
        Iterator unreportedClientsIterator;

        Client client;


        ActionRequest actionRequest;

        Movement movement;
        
        int randJ, randI;
        
        Unit unit;
        
        boolean res;
        
        LinkedList<Unit> removedUnits;
        
        State currentState = null;
        
        Iterator inboxIterator;
        InboxMessage inboxMessage;
        
        Channel channel;
        Message message, action;
        
        /*
         * Пометим всех отключившихся клиентов.
         */
        clientManager.markDisconnectedClients();
        
        /*
         * Сохраним текущее состояние игры.
         */
        try {
            currentState = saveCurrentState();
        } catch (CloneNotSupportedException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }

        /*
         * Пройдём по всем входящим сообщениям и обработаем их.
         */
        inboxIterator = messageManager.takeFromInbox().iterator();
        while (inboxIterator.hasNext()) {
            inboxMessage = (InboxMessage) inboxIterator.next();
            channel = inboxMessage.getFrom();
            message = inboxMessage.getMessage();

            if (message instanceof StateReportMessage) {
                if ( (client = clientManager.getClient(channel)) == null) {
                    if (isLoggingEnabled()) {
                        logger.warning(getStep() + ": Отчёт будет проигнорирован. Его отправитель клиент channel=" + channel + " не найден.");
                    }
                    continue;
                }
                if (!client.isConnected()) {
                    if (isLoggingEnabled()) {
                        logger.warning(getStep() + ": Отчёт будет проигнорирован. Его отправитель клиент id=" + client.getId() + " отключился.");
                    }
                    continue;
                }
                
                stateReportMessage = (StateReportMessage) message;
                
                try {
                    res = isStateValid(stateReportMessage);
                } catch (NullPointerException ex) {
                    logger.log(Level.SEVERE, null, ex);
                    LastErrorService.setLastError(MessageManager.createErrorMessage("Состояние не может быть проверено по причине внутренней ошибки сервера."));
                    res = false;
                }
                
                if (res) {
                    res = clientManager.setLastConfirmedStep(client.getId(), stateReportMessage.getStep());
                    if (ClientManager.isLoggingEnabled() && !res) {
                        logger.warning(getStep() + ": Не удалось установить последний подтверждённый шаг клиента. Клиент id=" + client.getId() + " не найден.");
                    }

                    addAllActionsRequests(client, stateReportMessage.getActions());
                } else {
                    res = clientManager.setDisconnected(client.getId());
                    if (ClientManager.isLoggingEnabled()) {
                        if (res) {
                            logger.info(getStep() + ": Неверное состояние клиента. Клиент id=" + client.getId() + " помечен как отключенный.");
                        } else {
                            logger.warning(getStep() + ": Неверное состояние клиента. Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как отключенный.");
                        }
                    }
                    if (res) {
                        messageManager.sendMessage(channel, LastErrorService.getLastError());
                    }
                }
            } else if (message instanceof LoginMessage) {
                loginMessage = (LoginMessage) message;

                // Здесь может быть какая-то проверка логина и пароля.
                
                long newClientId = IdGenerator.generateId();
                res = clientManager.createClient(newClientId, channel);
                if (res) {
                    if (ClientManager.isLoggingEnabled()) {
                        logger.info(getStep() + ": Создан новый клиент id=" + newClientId);
                    }
                    res = clientManager.setConnected(newClientId);
                    if (!res && ClientManager.isLoggingEnabled()) {
                        logger.severe(getStep() + ": Клиент не может быть помечен как подключенный. Клиент id=" + newClientId + " не найден.");
                    }
                } else if (ClientManager.isLoggingEnabled()) {
                    logger.severe(getStep() + ": Клиент id=" + newClientId + ",channel=" + channel + " не может быть создан, так как такой клиент уже существует.");
                }
            }
        }

        /*
         * Пройдём по запросам действий и выполним их.
         */
        actionRequestsIterator = actionsRequests.iterator();
        while (actionRequestsIterator.hasNext()) {
            actionRequest = (ActionRequest) actionRequestsIterator.next();
            client = actionRequest.getClient();
            action = actionRequest.getRequest();
            
            if (action instanceof MoveMessage) {
                moveMessage = (MoveMessage) action;
                if ( (unit = unitManager.getById(moveMessage.getUnitId())) == null) {
                    if (UnitManager.isLoggingEnabled()) {
                        logger.warning(getStep() + ": Запрос движения не может быть обработан. Юнит id=" + moveMessage.getUnitId() + " не найден.");
                    }
                    continue;   // Переходим к следующему запросу действия.
                }
                if (movementManager.isUnitMove(unit)) {
                    movement = movementManager.updateMovement(
                        getStep(),
                        map,
                        unit,
                        moveMessage.getEndJ(),
                        moveMessage.getEndI()
                    );
                    if (movement != null) {
                        //clientManager.notifyAllOtherClients(client, MessageManager.createMoveUpdateMessage(movement));
                        notifyClients(currentState, MessageManager.createMoveUpdateMessage(movement));
                    } else {
                        //logger.log(Level.SEVERE, "Movement not created.");
                    }
                } else {
                    movement = movementManager.startNewMovement(
                        getStep(),
                        map,
                        unit,
                        moveMessage.getEndJ(),
                        moveMessage.getEndI()
                    );
                    if (movement != null) {
                        //clientManager.notifyAllOtherClients(client, MessageManager.createMoveMessage(movement));
                        notifyClients(currentState, MessageManager.createMoveMessage(movement));
                    } else {
                        //logger.log(Level.SEVERE, "Movement not created.");
                    }
                }
            }
        }
        actionsRequests.clear();

        /*
         * Пройдём по всем отключенным клиентам и произведём очистку.
         */
        clientsIterator = clientManager.getDisconnectedClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            channel = clientManager.getClientChannel(client.getId());
            
            removedUnits = unitManager.markUnitsDeletedByOwnerId(client.getId());
            if (UnitManager.isLoggingEnabled()) {
                logger.info(getStep() + ": Помеченно на удаление " + removedUnits.size() + " юнитов клиента id=" + client.getId());
            }
            movementManager.removeByUnits(removedUnits);
            //clientManager.notifyAllOtherClients(client, MessageManager.createAllDelUnitMessage(removedUnits));
            notifyClients(currentState, MessageManager.createAllDelUnitMessage(removedUnits));
            
            res = clientManager.removeClient(client.getId());
            if (ClientManager.isLoggingEnabled()) {
                if (res) {
                    logger.info(getStep() + ": Удаление отключенного клиента. Клиент id=" + client.getId() + " был успешно удалён.");
                } else {
                    logger.info(getStep() + ": Удаление отключенного клиента. Клиент id=" + client.getId() + " не найден, поэтому не может быть удалён.");
                }
            }
            
            if (channel.isOpen()) {
                channel.close();
            }
            
            if (isLoggingEnabled()) {
                logger.info(getStep() + ": Клиент id=" + client.getId() + " был отключен и теперь удалён.");
            }
        }

        /*
         * Создадим для новых клиентов по два юнита и уведомим всех
         * остальных клиентах о новых юнитах.
         */
        newClientsIterator = clientManager.getUninitializedClients().iterator();
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            
            randJ = randomGenerator.nextInt(map.getWidth() - 1);
            randI = randomGenerator.nextInt(map.getWidth() - 1);
            try {
                unit = unitManager.createUnit(client, randJ, randI);
            } catch (NullPointerException ex) {
                logger.severe(getStep() + ": Не удалось создать юнита. " + ex.getMessage());
                continue;
            }
            //clientManager.notifyAllOtherClients(client, MessageManager.createNewUnitMessage(unit));
            notifyClients(currentState, MessageManager.createNewUnitMessage(unit));
            try {
                unit = unitManager.createUnit(client, randJ + 1, randI + 1);
            } catch (NullPointerException ex) {
                logger.severe(getStep() + ": Не удалось создать юнита. " + ex.getMessage());
                continue;
            }
            //clientManager.notifyAllOtherClients(client, MessageManager.createNewUnitMessage(unit));
            notifyClients(currentState, MessageManager.createNewUnitMessage(unit));
        }

        /*
         * Пройдём по всем клиентам и отправим им всё, что накопилось
         * в их исходящих сообщениях.
         */
        clientsIterator = clientManager.getActiveClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                State state = getState(client.getLastConfirmedStep());
                if (state == null) {
                    if (isLoggingEnabled()) {
                        logger.info(getStep() + ": Состояние за ход step=" + client.getLastConfirmedStep() + " не найдено.");
                    }
                    continue;
                }
                LinkedList<Message> outbox;
                if ( (outbox = state.getOutboxes().get(client.getId())) != null) {
                    messageManager.sendConfirm(channel, client.getLastConfirmedStep(), outbox);
                } else if (ClientManager.isLoggingEnabled()) {
                    logger.info(getStep() + ": Не удалось получить исходящие сообщения клиента. Клиент id=" + client.getId() + " не найден.");
                }
            } else if (ClientManager.isLoggingEnabled()) {
                logger.info(getStep() + ": Не удалось отправить подтверждение. Клиент id=" + client.getId() + " не найден.");
            }
        }

        /*
         * Удалим юнитов, помеченных на удаление.
         */
        unitManager.deleteMarkedUnits();

        /*
         * После всех действий можно обработать новых клиентов.
         */
        newClientsIterator = clientManager.getUninitializedClients().iterator();
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                messageManager.sendMessage(
                    channel,
                    new WelcomeMessage(
                        client.getId(),
                        getStep(),
                        MessageManager.createAllNewUnitMessage(unitManager.getAllUnits()),
                        MessageManager.createAllMoveMessage(movementManager.getAllMovements())
                    )
                );
            } else if (ClientManager.isLoggingEnabled()) {
                logger.info(getStep() + ": Не удалось отправить начальное состояние. Клиент id=" + client.getId() + " не найден.");
            }
            
            // Очистим входящие клиента от событий этого шага, они его не интересуют, так как он получит полное состояние.
            if (clientManager.takeFromOutbox(client.getId()) == null && ClientManager.isLoggingEnabled()) {
                logger.info(getStep() + ": Не удалось получить исходящие сообщения клиента. Клиент id=" + client.getId() + " не найден.");
            }
            
            res = clientManager.setInitialized(client.getId());
            if (ClientManager.isLoggingEnabled()) {
                if (res) {
                    logger.info(getStep() + ": Клиент id=" + client.getId() + " был успешно помечен как инициализированный.");
                } else {
                    logger.info(getStep() + ": Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как инициализированный.");
                }
            }
            
            newClientsIterator.remove();
        }

        step++;

        /*
         * Обновим позиции игроков.
         */
        //logger.log(Level.INFO, "Update units positions.");
        movementManager.updatePositions(getStep());
    }

    /**
     * Добавляет все запросы действий в список запросов действий клиентов.
     * @param client клиент, от которого исходят запросы действий.
     * @param requests запросы действий.
     */
    private void addAllActionsRequests(Client client, Collection<Message> requests) {
        for (Message r : requests) {
            actionsRequests.add(new ActionRequest(client, r));
        }
    }

    /**
     * Удаляет всех клиентов, не отчитавшихся за шаг step.
     * @param step шаг
     */
    private void disconnectUnreportedClients(long step) {
        Client client;
        Channel channel;
        boolean res;
        
        Iterator i = clientManager.getActiveClients().iterator();
        while (i.hasNext()) {
            client = (Client) i.next();
            if (client.getLastReportedStep() != step) {
                res = clientManager.setDisconnected(client.getId());
                if (ClientManager.isLoggingEnabled()) {
                    if (res) {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " помечен как отключенный.");
                    } else {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как отключенный.");
                    }
                }
                if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                    messageManager.sendErrorMessage(
                        channel,
                        MessageManager.createErrorMessage("You has been disconnected because report timeout.")
                    );
                } else if (ClientManager.isLoggingEnabled()) {
                    logger.info(getStep() + ": Не удалось отправить сообщение об отключении. Клиент id=" + client.getId() + " не найден.");
                }
            }
        }
    }

    /**
     * Помечает всех не отчитавшихся клиентов, как не инициализированных и
     * производит их очистку.
     * @param step шаг
     */
    private void resetUnreportedClients(long step) {
        Client client;
        boolean res;
        
        Iterator i = clientManager.getActiveClients().iterator();
        while (i.hasNext()) {
            client = (Client) i.next();
            if (client.getLastReportedStep() != step) {
                // TODO: Add reinitialized flag to client.
                if (client.isInitialized() && client.getLastConfirmedStep() != -1 && client.getLastReportedStep() != -1) {
                    res = clientManager.setUninitialized(client.getId());
                    if (ClientManager.isLoggingEnabled()) {
                        if (res) {
                            logger.info(getStep() + ": Клиент id=" + client.getId() + " был успешно помечен как неинициализированный.");
                        } else {
                            logger.info(getStep() + ": Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как неинициализированный.");
                        }
                    }
                    cleanupClient(client);
                } else {
                    if (clientManager.setDisconnected(client.getId()) && ClientManager.isLoggingEnabled()) {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " был успешно помечен как отключенный.");
                    } else if (ClientManager.isLoggingEnabled()) {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как отключенный.");
                    }
                }
            }
        }
        if (isLoggingEnabled()) {
            logger.info(getStep() + ": Некоторые клиенты были переинициализированы.");
        }
    }

    /**
     * Очистка, которая производится при удалении клиента.
     */
    private void cleanupClient(Client client) throws NullPointerException {
        if (client == null) {
            throw new NullPointerException("Клиент - null.");
        }
        
        LinkedList<Unit> removedUnits = unitManager.markUnitsDeletedByOwnerId(client.getId());
        if (UnitManager.isLoggingEnabled()) {
            logger.info(getStep() + ": Помеченно на удаление " + removedUnits.size() + " юнитов клиента id=" + client.getId());
        }
        clientManager.notifyAllOtherClients(client, MessageManager.createAllDelUnitMessage(removedUnits));
        
        Iterator actionsRequestsIterator = actionsRequests.iterator();
        while (actionsRequestsIterator.hasNext()) {
            if (client.equals(actionsRequestsIterator.next())) {
                actionsRequestsIterator.remove();
            }
        }
        
        // Очистим список исходящих сообщений.
        clientManager.takeFromOutbox(client.getId());
        clientManager.setLastConfirmedStep(client.getId(), -1);
        clientManager.setLastReportedStep(clientManager.getClientChannel(client.getId()), -1);
    }
    
    /**
     * Создаёт, сохраняет и возвращает текущее состояние игры.
     */
    private State saveCurrentState() throws CloneNotSupportedException {
        State newState = new State(getStep());
        
        newState.addAllUnits(unitManager.cloneUnits());
        
        for (Client client : clientManager.getActiveClients()) {
            newState.addClient(client.getId(), new LinkedList<Message>());
        }
        
        if (states.size() == 30) {
            states.removeFirst();
        }
        states.addLast(newState);
        
        return newState;
    }
    
    /**
     * Проверяет корректность состояния клиента. Возвращает true, если состояние
     * верное и false в противном случае.
     */
    private boolean isStateValid(StateReportMessage message) throws NullPointerException {
        if (message == null) {
            throw new NullPointerException("State report message is null.");
        }
        
        State state = getState(message.getStep());
        if (state == null) {
            if (isLoggingEnabled()) {
                logger.info(getStep() + ": Состояние за ход step=" + message.getStep() + " не найдено.");
            }
            
            LastErrorService.setLastError(MessageManager.createErrorMessage("Состояние за ход step=" + message.getStep() + " не найдено. Возможно, клиент имеет слишком большую суммарную задержку."));
            return false;
        }
        
        return unitManager.isStateValid(state.getUnits(), message.getUnits());
    }
    
    private State getState(long step) {
        for (State s : states) {
            if (s.getStep() == step) {
                return s;
            }
        }
        
        return null;
    }

    private void notifyClients(State currentState, Message action) {
        Entry pair;
        LinkedList<Message> outbox;

        Iterator i = currentState.getOutboxes().entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            outbox = (LinkedList<Message>) pair.getValue();
            outbox.add(action);
        }
    }

    private void notifyClients(State currentState, LinkedList<Message> actions) {
        Entry pair;
        LinkedList<Message> outbox;

        Iterator i = currentState.getOutboxes().entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            outbox = (LinkedList<Message>) pair.getValue();
            outbox.addAll(actions);
        }
    }
}
