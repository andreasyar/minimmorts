package minimmorts.server;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.ErrnoErrorService;
import minimmorts.GlobalLogFile;
import minimmorts.IdGenerator;
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
     * Количество итераций, которые главный цикл сервера прождал отчётов от клиентов.
     */
    private int waitIterations = 0;
    
    /**
     * Максимальное количество итераций, которые главный цикл сервера ждет отчётов от клиентов.
     */
    private final int maxWaitIterations = 30;

    private final UnitManager unitManager;
    private final MovementManager movementManager;
    private final MessageManager messageManager;
    private final ClientManager clientManager;
    
    private final Map map;
    
    private static long step = 1;
        
    private final Random randomGenerator = new Random();

    /**
     * Список запросов действий от всех клиентов на текущем шаге.
     */
    private final LinkedList<ActionRequest> actionsRequests = new LinkedList<>();

    public static void enableLogging() {
        ServerMainLoop.loggingEnabled = true;
    }

    public static void disableLogging() {
        ServerMainLoop.loggingEnabled = false;
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
        Message message;
        Message action;
        MoveMessage moveMessage;
        StateReportMessage stateReportMessage;
        LoginMessage loginMessage;

        Iterator clientsIterator;
        Iterator actionRequestsIterator;
        Iterator inboxIterator;
        Iterator newClientsIterator;

        Channel channel;
        Client client;

        InboxMessage inboxMessage;

        ActionRequest actionRequest;

        Movement movement;
        
        int randJ, randI;
        
        Unit unit;
        
        boolean res;

        if (!clientManager.isAllClientsReported(step) && waitIterations < maxWaitIterations) {
            if (loggingEnabled) {
                logger.info(getStep() + ": Ожидание отчётов от клиентов.");
            }
            waitIterations++;
            return;
        } else if (!clientManager.isAllClientsReported(step)) {
            waitIterations = 0;
            disconnectUnreportedClients(step);
            //reinitializeUnreportedClients(step);
        } else {
            waitIterations = 0;
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
                    if (loggingEnabled) {
                        logger.warning(getStep() + ": Сообщение не может быть обработано. Клиент channel=" + channel + " не найден.");
                    }
                    continue;   // Обработаем следующее сообщение.
                }
                stateReportMessage = (StateReportMessage) message;

                if (stateReportMessage.getStep() == getStep()) {
                    if (unitManager.isStateValid(stateReportMessage.getUnits())) {
                        res = clientManager.setLastConfirmedStep(client.getId(), getStep());
                        if (ClientManager.isLoggingEnabled() && !res) {
                            logger.warning(getStep() + ": Не удалось установить последний подтверждённый шаг клиента. Клиент id=" + client.getId() + " не найден.");
                        }
                        addAllActionsRequests(client, stateReportMessage.getActions());
                    } else {
                        res = clientManager.markDisconnected(client.getId());
                        if (ClientManager.isLoggingEnabled()) {
                            if (res) {
                                logger.info(getStep() + ": Неверное состояние клиента. Клиент id=" + client.getId() + " помечен как отключенный.");
                            } else {
                                logger.warning(getStep() + ": Неверное состояние клиента. Клиент id=" + client.getId() +
                                               " не найден, поэтому не может быть помечен как отключенный.");
                            }
                        }
                        if (res) {
                            messageManager.sendErrorMessage(
                                channel,
                                MessageManager.createErrorMessage(ErrnoErrorService.getErrno())
                            );
                        }
                    }
                }
            } else if (message instanceof LoginMessage) {
                loginMessage = (LoginMessage) message;

                // Здесь может быть какая-то проверка логина и пароля.
                
                long newClientId = IdGenerator.generateId();
                res = clientManager.createClient(newClientId, channel);
                if (ClientManager.isLoggingEnabled()) {
                    if (res) {
                        logger.info(getStep() + ": Создан новый клиент id=" + newClientId);
                    } else {
                        logger.warning(getStep() + ": Клиент id=" + newClientId + ",channel=" + channel + 
                                       " не может быть создан, так как такой клиент уже существует.");
                    }
                }
            }
        }

        /*
         * Пройдём по всем клиентам и произведём отчистку отключившихся
         * клиентов и клиентов с неверным состоянием.
         */
        clientsIterator = clientManager.getDisconnectedClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            cleanupClient(client);
            res = clientManager.removeClient(client.getId());
            if (ClientManager.isLoggingEnabled()) {
                if (res) {
                    logger.info(getStep() + ": Удаление отключенного клиента. Клиент id=" + client.getId() + " был успешно удалён.");
                } else {
                    logger.info(getStep() + ": Удаление отключенного клиента. Клиент id=" + client.getId() + " не найден, поэтому не может быть удалён.");
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
                    actionRequestsIterator.remove();
                    continue;   // Переходим к следующему запросу действия.
                }
                if (movementManager.isUnitMove(unit)) {
                    movement = movementManager.updateMovement(
                        step,
                        map,
                        unit,
                        moveMessage.getEndJ(),
                        moveMessage.getEndI()
                    );
                    if (movement != null) {
                        clientManager.notifyAllOtherClients(client, MessageManager.createMoveUpdateMessage(movement));
                    } else {
                        //logger.log(Level.SEVERE, "Movement not created.");
                    }
                } else {
                    movement = movementManager.startNewMovement(
                        step,
                        map,
                        unit,
                        moveMessage.getEndJ(),
                        moveMessage.getEndI()
                    );
                    if (movement != null) {
                        clientManager.notifyAllOtherClients(client, MessageManager.createMoveMessage(movement));
                    } else {
                        //logger.log(Level.SEVERE, "Movement not created.");
                    }
                }
            }
            actionRequestsIterator.remove();
        }

        /*
         * Создадим для новых клиентов по два юнита и уведомим всех
         * остальных клиентах о новых юнитах.
         */
        //logger.log(Level.INFO, "Create new units.");
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
            clientManager.notifyAllOtherClients(client, MessageManager.createNewUnitMessage(unit));
            try {
                unit = unitManager.createUnit(client, randJ + 1, randI + 1);
            } catch (NullPointerException ex) {
                logger.severe(getStep() + ": Не удалось создать юнита. " + ex.getMessage());
                continue;
            }
            clientManager.notifyAllOtherClients(client, MessageManager.createNewUnitMessage(unit));
        }

        /*
         * Пройдём по всем клиентам и отправим им всё, что накопилось
         * в их исходящих сообщениях.
         */
        //logger.log(Level.INFO, "Send messages.");
        clientsIterator = clientManager.getClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            
            if ( (channel = clientManager.getClientChannel(client.getId())) != null) {
                LinkedList<Message> outbox;
                if ( (outbox = clientManager.takeFromOutbox(client.getId())) != null) {
                    messageManager.sendConfirm(channel, getStep(), outbox);
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
        
        Iterator i = clientManager.getClients().iterator();
        while (i.hasNext()) {
            client = (Client) i.next();
            if (client.getLastReportedStep() != step) {
                // TODO if-ы @ проверки
                res = clientManager.markDisconnected(client.getId());
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
     * Помечает все не отчитавшихся за шаг step клиентов, как не инициализированных. Они будут
     * переинициализированы.
     * @param step шаг
     */
    private void reinitializeUnreportedClients(long step) {
        Client client;
        boolean res;
        
        Iterator i = clientManager.getClients().iterator();
        while (i.hasNext()) {
            client = (Client) i.next();
            if (client.getLastReportedStep() != step) {
                // TODO if-ы @ проверки
                res = clientManager.setUninitialized(client.getId());
                if (ClientManager.isLoggingEnabled()) {
                    if (res) {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " был успешно помечен как неинициализированный.");
                    } else {
                        logger.info(getStep() + ": Клиент id=" + client.getId() + " не найден, поэтому не может быть помечен как неинициализированный.");
                    }
                }
                cleanupClient(client);
            }
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
    }
}
