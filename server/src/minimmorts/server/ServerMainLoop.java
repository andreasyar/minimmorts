package minimmorts.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.IdGenerator;
import minimmorts.ErrnoErrorService;
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
    private static final Logger logger = Logger.getLogger(ServerMainLoop.class.getName());
    
    /**
     * Количество итераций, которые главный цикл сервера прождал отчётов от клиентов.
     */
    private int waitIterations = 0;
    
    /**
     * Максимальное количество итераций, которые главный цикл сервера ждет отчётов от клиентов.
     */
    private final int maxWaitIterations = 5;

    private final UnitManager unitManager;
    private final MovementManager movementManager;
    private final MessageManager messageManager;
    private final ClientManager clientManager;
    
    private final Map map;
    
    private long step = 1;

    /**
     * Список запросов действий от всех клиентов на текущем шаге.
     */
    private final LinkedList<ActionRequest> actionsRequests = new LinkedList<>();

    public ServerMainLoop(UnitManager unitManager, MovementManager movementManager, MessageManager messageManager, ClientManager clientManager) {
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

        if (!clientManager.isAllClientsReported(step) && waitIterations < maxWaitIterations) {
            logger.log(Level.INFO, "Wait for clients report for step {0}", step);
            waitIterations++;
            return;
        } else if (!clientManager.isAllClientsReported(step)) {
            waitIterations = 0;
            disconnectUnreportedClients(step);
        } else {
            waitIterations = 0;
        }

        /*
         * Пройдём по всем входящим сообщениям и обработаем их.
         */
        //logger.log(Level.INFO, "Process incoming messages.");
        inboxIterator = messageManager.takeFromInbox().iterator();
        while (inboxIterator.hasNext()) {
            inboxMessage = (InboxMessage) inboxIterator.next();
            channel = inboxMessage.getFrom();
            message = inboxMessage.getMessage();

            if (message instanceof StateReportMessage) {
                if ( (client = clientManager.getClient(channel)) == null) {
                    logger.log(Level.SEVERE, "Client not found. State report message ignored.");
                }
                stateReportMessage = (StateReportMessage) message;

                if (client.getLastConfirmedStep() == step) {
                    // Клиент прислал два или более отчётов за один и тот же шаг? Тут явно что-то не так.
                    if (!clientManager.markDisconnected(client.getId())) {
                        logger.log(Level.SEVERE, "Client id={0} not found. Can't mark client as disconnected.", client.getId());
                    }
                    messageManager.sendErrorMessage(channel, MessageManager.createErrorMessage("Dublicated state report."));
                    continue;
                }
                if (stateReportMessage.getStep() == step) {
                    if (unitManager.isStateValid(stateReportMessage.getUnits())) {
                        clientManager.setLastConfirmedStep(client.getId(), step);
                        addAllActionsRequests(client, stateReportMessage.getActions());
                    } else {
                        if (!clientManager.markDisconnected(client.getId())) {
                            logger.log(Level.SEVERE, "Client id={0} not found. Can't mark client as disconnected.", client.getId());
                        }
                        messageManager.sendErrorMessage(channel, MessageManager.createErrorMessage(ErrnoErrorService.getErrno()));
                    }
                } else {
                    if (!clientManager.markDisconnected(client.getId())) {
                        logger.log(Level.SEVERE, "Client id={0} not found. Can't mark client as disconnected.", client.getId());
                    }
                    messageManager.sendErrorMessage(
                        channel,
                        MessageManager.createErrorMessage(
                            "Reported step not equal current step! " + stateReportMessage.getStep() + "!=" + step
                        )
                    );
                }
            } else if (message instanceof LoginMessage) {
                loginMessage = (LoginMessage) message;

                // Здесь может быть какая-то проверка логина и пароля.
                if (!clientManager.createClient(IdGenerator.generateId(), channel)) {
                    logger.log(Level.SEVERE, "Can't create client. Login message ignored.");
                }
            }
        }

        /*
         * Пройдём по всем клиентам и произведём отчистку отключившихся
         * клиентов и клиентов с неверным состоянием.
         */
        //logger.log(Level.INFO, "Disconnect clients.");
        clientsIterator = clientManager.getDisconnectedClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            cleanupClient(client);
            clientManager.disconnectClient(client.getId());
            logger.log(Level.INFO, "Сlient id={0} disconnected.", client.getId());
        }

        /*
         * Пройдём по запросам действий и выполним их.
         */
        //logger.log(Level.INFO, "Handle clients actions.");
        actionRequestsIterator = actionsRequests.iterator();
        while (actionRequestsIterator.hasNext()) {
            actionRequest = (ActionRequest) actionRequestsIterator.next();
            client = actionRequest.getClient();
            action = actionRequest.getRequest();
            if (action instanceof MoveMessage) {
                moveMessage = (MoveMessage) action;
                movement = movementManager.startMovement(step, map, unitManager.getById(moveMessage.getUnitId()), moveMessage.getEndJ(), moveMessage.getEndI());
                if (movement != null) {
                    clientManager.notifyAllOtherClients(client, MessageManager.createMoveMessage(movement));
                }
            }
            actionRequestsIterator.remove();
        }

        /*
         * Создадим для новых клиентов по два юнита и уведомим всех
         * остальных клиентах о новых юнитах.
         */
        //logger.log(Level.INFO, "Create new units.");
        newClientsIterator = clientManager.getAwaitingInitializingClients().iterator();
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            clientManager.notifyAllOtherClients(
                client,
                MessageManager.createNewUnitMessage(
                    unitManager.createUnit(client.getId(), 4, 4)
                )
            );
            clientManager.notifyAllOtherClients(
                client,
                MessageManager.createNewUnitMessage(
                    unitManager.createUnit(client.getId(), 5, 5)
                )
            );
        }

        /*
         * Пройдём по всем клиентам и отправим им всё, что накопилось
         * в их исходящих сообщениях.
         */
        //logger.log(Level.INFO, "Send messages.");
        clientsIterator = clientManager.getClients().iterator();
        while (clientsIterator.hasNext()) {
            client = (Client) clientsIterator.next();
            if ( (channel = clientManager.getClientChannel(client.getId())) == null) {
                logger.log(Level.SEVERE, "Client id={0} not found. Can't get channel.", client.getId());
            }
            messageManager.sendConfirm(channel, step, clientManager.takeFromOutbox(client.getId()));
        }

        /*
         * Удалим юнитов, помеченных на удаление.
         */
        unitManager.removeMarkedUnits();

        /*
         * После всех действий можно обработать новых клиентов.
         */
        //logger.log(Level.INFO, "Send welcome messages.");
        newClientsIterator = clientManager.getAwaitingInitializingClients().iterator();
        while (newClientsIterator.hasNext()) {
            client = (Client) newClientsIterator.next();
            if ( (channel = clientManager.getClientChannel(client.getId())) == null) {
                logger.log(Level.SEVERE, "Client id={0} not found. Can't get channel.", client.getId());
            }
            messageManager.sendMessage(
                channel,
                new WelcomeMessage(
                    client.getId(),
                    step,
                    MessageManager.createAllNewUnitMessage(unitManager.getAllUnits()),
                    MessageManager.createAllMoveMessage(movementManager.getAllMovements())
                )
            );
            if (!clientManager.setInitialized(client.getId())) {
                logger.log(Level.SEVERE, "Client id={0} not found. Can't mark client as initialized.", client.getId());
            }
            newClientsIterator.remove();
        }

        step++;

        /*
         * Обновим позиции игроков.
         */
        //logger.log(Level.INFO, "Update units positions.");
        movementManager.updatePositions(step);
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
        
        Iterator i = clientManager.getClients().iterator();
        while (i.hasNext()) {
            client = (Client) i.next();
            if (client.getLastReportedStep() != step) {
                // TODO if-ы @ проверки
                clientManager.markDisconnected(client.getId());
                messageManager.sendErrorMessage(
                    clientManager.getClientChannel(client.getId()),
                    MessageManager.createErrorMessage("You has been disconnected because report timeout.")
                );
            }
        }
    }

    /**
     * Очистка, которая производится при удалении клиента.
     */
    private void cleanupClient(Client client) {
        LinkedList<Unit> removedClients = unitManager.removeUnitsByOwnerId(client.getId());
        clientManager.notifyAllOtherClients(
            client,
            MessageManager.createAllDelUnitMessage(removedClients)
        );
    }
}
