package minimmorts.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.GlobalLogFile;
import minimmorts.IdGenerator;
import minimmorts.LastErrorService;
import minimmorts.ServerLauncher;
import minimmorts.client.Client;
import minimmorts.client.ClientManager;
import minimmorts.map.Map;
import minimmorts.messages.*;
import minimmorts.movement.Movement;
import minimmorts.movement.MovementManager;
import minimmorts.movement.Point;
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
        Iterator clientsIterator;

        Client client;
        
        /*
         * Пометим всех отключившихся клиентов.
         */
        clientManager.markDisconnectedClients();
        
        clientsIterator = clientManager.getActiveClients().iterator();
        while(clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            
            /*if (ServerMainLoop.isLoggingEnabled()) {
                logger.info("Клиент id=" + client.getId() + " step=" + ServerMainLoop.getStep() + " lastReportedStep=" + client.getLastReportedStep());
            }*/
            if (client.getLastReportedStep() != -1 && ServerMainLoop.getStep() - client.getLastReportedStep() >= 30) {
                clientManager.addDisctonnectedClient(client);
                if (ServerMainLoop.isLoggingEnabled()) {
                    logger.info(ServerMainLoop.getStep() + ": Клиент id=" + client.getId() + " не отчитывался больше 30 шагов lastReportedStep=" + client.getLastReportedStep() + " и будет помечен как отключенный.");
                }
            }
        }
        
        /*
         * Сохраним текущее состояние игры.
         */
        try {
            saveCurrentState();
        } catch (CloneNotSupportedException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        
        /*
         * Обработаем все входящие сообщения.
         */
        handleInbox();
        
        try {
            /*
             * Выполним запросы действий клиентов.
             */
            handleActionRequests();
        } catch (NullPointerException | IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            ServerLauncher.handle.cancel(false);    // TODO Удалить эту хуету
            return;
        }

        /*
         * Произведём очистку и удалим отключенных клиентов.
         */
        deleteDisconnectedClients();

        /*
         * Создаёт два новых юнита для новых клиентов клиентов.
         */
        createNewUnits();

        /*
         * Отправим CONFIRM (исходящие сообщения) всем активным клиентам.
         */
        sendOutMessages();

        /*
         * Удалим юнитов, помеченных на удаление.
         */
        unitManager.deleteMarkedUnits();

        /*
         * Отправим всем не инициализированным клиентам WELCOME (начальное состояние) и пометим их как инициализированных.
         */
        sendWelcomeMessages();

        step++;

        /*
         * Обновим позиции юнитов.
         */
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
            if (ServerMainLoop.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Добавлен новый запрос действия от клиента {1} {2}.",
                    new Object[] {ServerMainLoop.getStep(), client, r.toJSON()}
                );
            }
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
    
    /**
     * Отправляет CONFIRM (исходящие сообщения) всем активным клиентам.
     */
    private void sendOutMessages() {
        Client client;
        Channel channel;
        LinkedList<Message> outbox;
        Iterator clientsIterator = clientManager.getActiveClients().iterator();
        
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                State currentState = getState(ServerMainLoop.getStep());
                if (currentState == null) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Состояние за текущий ход не найдено.",
                        new Object[] {ServerMainLoop.getStep()}
                    );
                    continue;
                }
                
                if ( (outbox = currentState.getOutboxes().get(client.getId())) != null) {
                    messageManager.sendConfirm(channel, ServerMainLoop.getStep(), outbox);
                } else {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Не удалось получить исходящие сообщения клиента. Клиент {1} не найден.",
                        new Object[] {ServerMainLoop.getStep(), client}
                    );
                }
            } else {
                logger.log(
                    Level.SEVERE,
                    "{0}: Невозможно отправить CONFIRM. Клиент {1} не найден.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
            }
        }
    }
    
    /**
     * Выполняет запросы действий клиентов.
     */
    private void handleActionRequests() throws NullPointerException, IOException {
        State currentState = getState(ServerMainLoop.getStep());
        Movement movement;
        Unit unit;
        MoveRequestMessage moveRequestMessage;
        Message action;
        ActionRequest actionRequest;
        Client client;
        Iterator actionRequestsIterator = actionsRequests.iterator();

        if (currentState == null) {
            logger.log(
                Level.SEVERE,
                "{0}: Состояние за текущий ход не найдено.",
                new Object[] {ServerMainLoop.getStep()}
            );
            return;
        }
        
        while (actionRequestsIterator.hasNext()) {
            actionRequest = (ActionRequest) actionRequestsIterator.next();
            action = actionRequest.getRequest();
            client = actionRequest.getClient();
            
            if (action instanceof MoveRequestMessage) {
                moveRequestMessage = (MoveRequestMessage) action;
                
                if ( (unit = unitManager.getById(moveRequestMessage.getUnitId())) == null) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Запрос движения не может быть выполнен. Юнит id={1} не найден. Запрос исходил от клиента {2}",
                        new Object[] {ServerMainLoop.getStep(), moveRequestMessage.getUnitId(), client}
                    );
                    if (!unitManager.isClientHasUnit(client.getId(), moveRequestMessage.getUnitId())){
                        logger.log(
                            Level.SEVERE,
                            "{0}: Клиент {1} запросил движение не своего юнита id={2}.",
                            new Object[] {ServerMainLoop.getStep(), client, moveRequestMessage.getUnitId()}
                        );
                    }
                    continue;   // Переходим к следующему запросу действия.
                }
                if (unit.isDeleted()) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Запрос движения не может быть выполнен. Юнит {1} помечен на удаление.",
                        new Object[] {ServerMainLoop.getStep(), unit}
                    );
                    continue;   // Переходим к следующему запросу действия.
                }
                
                if (unit.getJ() == moveRequestMessage.getJ() && unit.getI() == moveRequestMessage.getI()) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Запрос движения не может быть выполнен. Так как юнит {1} хочет сходить в ту же самую точку, в которой он находится.",
                        new Object[] {ServerMainLoop.getStep(), unit}
                    );
                    continue;   // Переходим к следующему запросу действия.
                }
                
                if (movementManager.isUnitMove(unit)) {
                    movement = movementManager.updateMovement(
                        ServerMainLoop.getStep(),
                        map,
                        unit,
                        moveRequestMessage.getJ(),
                        moveRequestMessage.getI()
                    );
                    if (movement != null) {
                        notifyClients(currentState, MessageManager.createMoveUpdateMessage(movement));
                    } else {
                        logger.log(
                            Level.SEVERE,
                            "{0}: Движение юнита {1} в клетку {2} не может быть обновлено.",
                            new Object[] {ServerMainLoop.getStep(), unit, new Point(moveRequestMessage.getJ(), moveRequestMessage.getI())}
                        );
                    }
                } else {
                    movement = movementManager.startNewMovement(
                        ServerMainLoop.getStep(),
                        map,
                        unit,
                        moveRequestMessage.getJ(),
                        moveRequestMessage.getI()
                    );
                    if (movement != null) {
                        notifyClients(currentState, MessageManager.createMoveMessage(movement));
                    } else {
                        logger.log(
                            Level.SEVERE,
                            "{0}: Новое движение юнита {1} в клетку {2} не может быть создано.",
                            new Object[] {ServerMainLoop.getStep(), unit, new Point(moveRequestMessage.getJ(), moveRequestMessage.getI())}
                        );
                    }
                }
            }
        }
        
        actionsRequests.clear();
    }
    
    /**
     * Производит необходимую очистку и удаляет отключенных клиентов.
     */
    private void deleteDisconnectedClients() {
        Client client;
        Channel channel;
        LinkedList<Unit> removedUnits;
        State currentState;
        Iterator clientsIterator = clientManager.getDisconnectedClients().iterator();

        currentState = getState(ServerMainLoop.getStep());
        if (currentState == null) {
            logger.log(
                Level.SEVERE,
                "{0}: Состояние за текущий ход не найдено.",
                new Object[] {ServerMainLoop.getStep()}
            );
            return;
        }
        
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) == null) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Клиент не может быть удалён. Клиент {1} не найден.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
                continue;   // Переходим к следующему клиенту.
            }
            
            removedUnits = unitManager.markUnitsDeletedByOwnerId(client.getId());
            if (UnitManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Помечено на удаление {1} юнитов клиента {2}",
                    new Object[] {ServerMainLoop.getStep(), removedUnits.size(), client}
                );
            }
            
            movementManager.removeByUnits(removedUnits);
            
            notifyClients(currentState, MessageManager.createAllDelUnitMessage(removedUnits));
            
            if (!clientManager.removeClient(client.getId())) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Клиент не может быть удалён. Клиент {1} не найден.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
                continue;   // Переходим к следующему клиенту.
            } else if (ClientManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Клиент {1} удалён.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
            }
            
            if (channel.isOpen()) {
                channel.close();
            }
        }
    }
    
    /**
     * Создаёт два новых юнита для не инициализированных клиентов.
     */
    private void createNewUnits() {
        Client client;
        int randJ;
        int randI;
        Unit unit;
        State currentState;
        Random randomGenerator = new Random();
        Iterator newClientsIterator = clientManager.getUninitializedClients().iterator();

        currentState = getState(ServerMainLoop.getStep());
        if (currentState == null) {
            logger.log(
                Level.SEVERE,
                "{0}: Состояние за текущий ход не найдено.",
                new Object[] {ServerMainLoop.getStep()}
            );
            return;
        }
        
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            
            randJ = randomGenerator.nextInt(map.getWidth() - 1);
            randI = randomGenerator.nextInt(map.getWidth() - 1);
            
            try {
                unit = unitManager.createUnit(client, randJ, randI);
            } catch (NullPointerException ex) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Не удалось создать юнита. {1}",
                    new Object[] {ServerMainLoop.getStep(), ex.getMessage()}
                );
                continue;
            }
            notifyClients(currentState, MessageManager.createNewUnitMessage(unit));
            if (UnitManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Создан юнит {1}",
                    new Object[] {ServerMainLoop.getStep(), unit}
                );
            }
            
            try {
                unit = unitManager.createUnit(client, randJ + 1, randI + 1);
            } catch (NullPointerException ex) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Не удалось создать юнита. {1}",
                    new Object[] {ServerMainLoop.getStep(), ex.getMessage()}
                );
                continue;
            }
            notifyClients(currentState, MessageManager.createNewUnitMessage(unit));
            if (UnitManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Создан юнит {1}",
                    new Object[] {ServerMainLoop.getStep(), unit}
                );
            }
        }
    }
    
    /**
     * Отправляет всем не инициализированным клиентам WELCOME (начальное состояние) и
     * помечает их как инициализированных.
     */
    private void sendWelcomeMessages() {
        Client client;
        Channel channel;
        Iterator newClientsIterator = clientManager.getUninitializedClients().iterator();
        
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                messageManager.sendMessage(
                    channel,
                    new WelcomeMessage(
                        client.getId(),
                        getStep(),
                        MessageManager.createAllNewUnitMessage(unitManager.getAllActiveUnits()),
                        MessageManager.createAllMoveMessage(movementManager.getAllActiveMovements())
                    )
                );
            } else {
                logger.log(
                    Level.SEVERE,
                    "{0}: Не удалось отправить начальное состояние. Клиент {1} не найден.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
                continue;   // Переходим к следующему клиенту.
            }
            
            if (!clientManager.setInitialized(client.getId())) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Не удалось пометить клиента как инициализированного. Клиент {1} не найден.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
                continue;   // Переходим к следующему клиенту.
            } else if (ClientManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Клиент {1} помечен как инициализированный.",
                    new Object[] {ServerMainLoop.getStep(), client}
                );
            }
        }
    }
    
    /**
     * Обрабатывает все входящие сообщения.
     */
    private void handleInbox() {
        InboxMessage inboxMessage;
        Channel channel;
        Message message;
        Client client;
        StateReportMessage stateReportMessage;
        boolean res;
        Iterator inboxIterator = messageManager.takeFromInbox().iterator();
        
        while (inboxIterator.hasNext()) {
            inboxMessage = (InboxMessage) inboxIterator.next();
            channel = inboxMessage.getFrom();
            message = inboxMessage.getMessage();

            if (message instanceof StateReportMessage) {
                if ( (client = clientManager.getClient(channel)) == null) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Отчёт не может быть обработан. Его отправитель клиент channel={1} не найден.",
                        new Object[] {ServerMainLoop.getStep(), channel}
                    );
                    continue;
                }
                if (!client.isConnected()) {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Отчёт не может быть обработан. Его отправитель клиент {1} отключен.",
                        new Object[] {ServerMainLoop.getStep(), client}
                    );
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
                    if (!clientManager.setLastConfirmedStep(client.getId(), stateReportMessage.getStep())) {
                        logger.log(
                            Level.SEVERE,
                            "{0}: Не удалось установить последний подтверждённый шаг клиента. Клиент {1} не найден.",
                            new Object[] {ServerMainLoop.getStep(), client}
                        );
                    }

                    addAllActionsRequests(client, stateReportMessage.getActions());
                } else {
                    if (!clientManager.setDisconnected(client.getId())) {
                        logger.log(
                            Level.SEVERE,
                            "{0}: Клиент не может быть помечен как отключенный. Клиент {1} не найден.",
                            new Object[] {ServerMainLoop.getStep(), client}
                        );
                    } else {
                        if (ClientManager.isLoggingEnabled()) {
                            logger.log(
                                Level.INFO,
                                "{0}: Неверное состояние клиента. Клиент {1} помечен как отключенный.",
                                new Object[] {ServerMainLoop.getStep(), client}
                            );
                        }
                        
                        messageManager.sendMessage(channel, LastErrorService.getLastError());
                    }
                }
            } else if (message instanceof LoginMessage) {
                //LoginMessage loginMessage = (LoginMessage) message;

                // Здесь может быть какая-то проверка логина и пароля.
                
                long newClientId = IdGenerator.generateId();
                if (clientManager.createClient(newClientId, channel)) {
                    if (ClientManager.isLoggingEnabled()) {
                        logger.log(
                            Level.INFO,
                            "{0}: Создан новый клиент id={1}",
                            new Object[] {ServerMainLoop.getStep(), newClientId}
                        );
                    }
                    if (!clientManager.setConnected(newClientId)) {
                        logger.log(
                            Level.SEVERE,
                            "{0}: Клиент не может быть помечен как подключенный. Клиент id={1} не найден.",
                            new Object[] {ServerMainLoop.getStep(), newClientId}
                        );
                    }
                } else {
                    logger.log(
                        Level.SEVERE,
                        "{0}: Клиент id={1},channel={2} не может быть создан, так как такой клиент уже существует.",
                        new Object[] {ServerMainLoop.getStep(), newClientId, channel}
                    );
                }
            }
        }
    }
}
