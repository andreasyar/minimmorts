package minimmorts;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class Server {
    // TODO А зачем логгеру тут (да и в других классах) быть статиком?
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private UnitManager unitManager = new UnitManager();
    private MovementManager movementManager = new MovementManager();
    private Map map = new Map(100, 100);
    private Runnable mainLoop;
    //private final ConcurrentHashMap<Channel, Client> clients = new ConcurrentHashMap();
    //private final ConcurrentHashMap<Client, Channel> channels = new ConcurrentHashMap();
    private long step = 1;
    private MessageManager messageManager;
    private final ClientChannelMaps clientChannelMaps = new ClientChannelMaps();

    /**
    * Вновь подключившиеся клиенты, ожидающие начального состояния, чтобы начать работу.
    */
    private final LinkedList<Client> newClients = new LinkedList<>();

    /**
     * Список запросов действий от всех клиентов на текущем шаге.
     */
    private final LinkedList<ActionRequest> actionsRequests = new LinkedList<>();

    public void start() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        mainLoop = new Runnable() {
            private int waitIterations = 0;

            @Override
            public void run() {
                Iterator clientsIterator, messagesIterator, actionsIterator, actionRequestsIterator;
                Entry pair;
                Channel channel;
                Client client;
                LinkedList<Message> messages;
                LinkedList<Channel> clientsToDisconnect = new LinkedList<>();
                Message message;
                MoveMessage moveMessage;
                LinkedList<Unit> missedUnits, excellUnits;

                // Список уведомлений, которые должны быть отправлены всем клиентам после выполнения всех действий.
                LinkedList<Message> messagesForAllClients = new LinkedList<>();
                Iterator messagesForAllClientsIterator;

                // Входящие сообщения.
                LinkedList<InboxMessage> inbox;
                Iterator inboxIterator;
                InboxMessage inboxMessage;
                StateReportMessage stateReportMessage;
                LoginMessage loginMessage;

                ActionRequest actionRequest;
                Message action;

                Iterator newClientsIterator;
                
                Movement movement;

                if (!isAllClientsReported(step) && waitIterations < 5) {
                    logger.log(Level.INFO, "Wait for clients report for step {0}", step);
                    waitIterations++;
                    return;
                } else if (!isAllClientsReported(step)) {
                    waitIterations = 0;
                    disconnectUnreportedClients(step);
                } else {
                    waitIterations = 0;
                }

                synchronized (clientChannelMaps) {

                    /*
                     * Пройдём по всем входящим сообщениям и обработаем их.
                     */
                    //logger.log(Level.INFO, "Process incoming messages.");
                    inbox = messageManager.takeFromInbox();
                    inboxIterator = inbox.iterator();
                    while (inboxIterator.hasNext()) {
                        inboxMessage = (InboxMessage) inboxIterator.next();
                        channel = inboxMessage.getFrom();
                        message = inboxMessage.getMessage();

                        if (message instanceof StateReportMessage) {
                            client = clientChannelMaps.getClients().get(channel);
                            stateReportMessage = (StateReportMessage) message;

                            if (client.getLastConfirmedStep() == step) {
                                // Клиент прислал два или более отчётов за один и тот же шаг? Тут явно что-то не так.
                                messageManager.sendMessage(client, MessageManager.createErrorMessage("Dublicated state report."));
                                clientsToDisconnect.add(channel);
                                continue;
                            }
                            if (stateReportMessage.getStep() == step && unitManager.isStateValid(stateReportMessage.getUnits())) {
                                client.setLastConfirmedStep(step);
                                addAllActionsRequests(client, stateReportMessage.getActions());
                            } else {
                                messageManager.sendMessage(client, MessageManager.createErrorMessage(SimpleErrorService.getLastError()));
                                clientsToDisconnect.add(channel);
                            }
                        } else if (message instanceof LoginMessage) {
                            loginMessage = (LoginMessage) message;

                            // Здесь может быть какая-то проверка логина и пароля.
                            newClients.add(new Client(channel));
                        }
                    }

                    /*
                     * Отключим от сервера клиентов с неверным состоянием.
                     */
                    //logger.log(Level.INFO, "Disconnect invalid clients.");
                    for (Channel c : clientsToDisconnect) {
                        client = clientChannelMaps.getClients().get(c);
                        cleanupClient(client);
                        clientChannelMaps.getChannels().remove(client);
                        clientChannelMaps.getClients().remove(c);
                        //client.getChannel().close();    // TODO Наверное это надо сделать где-то в другом месте.
                        logger.log(Level.INFO, "Invalid client disconnected.");
                    }
                    clientsToDisconnect.clear();

                    /*
                     * Пройдём по всем клиентам и произведём отчистку отключившихся
                     * клиентов.
                     */
                    //logger.log(Level.INFO, "Disconnect clients.");
                    clientsIterator = clientChannelMaps.getClients().entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        channel = (Channel) pair.getKey();
                        client = (Client) pair.getValue();
                        
                        if (client.isDisconnected()) {
                            cleanupClient(client);
                            clientChannelMaps.getChannels().remove(client);
                            clientChannelMaps.getClients().remove(channel);
                            logger.log(Level.INFO, "Сlient disconnected.");
                        }
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
                                notifyAllOtherClients(client, MessageManager.createMoveMessage(movement));
                            }
                        }

                        actionRequestsIterator.remove();
                    }

                    /*
                     * Создадим для новых клиентов по два юнита и уведомим всех
                     * остальных клиентах о новых юнитах.
                     */
                    //logger.log(Level.INFO, "Create new units.");
                    newClientsIterator = newClients.iterator();
                    while (newClientsIterator.hasNext()) {
                        client = (Client) newClientsIterator.next();

                        notifyAllOtherClients(
                            client,
                            MessageManager.createNewUnitMessage(
                                unitManager.createUnit(client.getId(), 4, 4)
                            )
                        );
                        notifyAllOtherClients(
                            client,
                            MessageManager.createNewUnitMessage(
                                unitManager.createUnit(client.getId(), 5, 5)
                            )
                        );
                    }

                    /*
                     * Пройдём по всем клиентам и отправим им всё что накопилось
                     * в их исходящих сообщениях.
                     */
                    //logger.log(Level.INFO, "Send messages.");
                    clientsIterator = clientChannelMaps.getClients().entrySet().iterator();
                    while (clientsIterator.hasNext()) {
                        pair = (Entry) clientsIterator.next();
                        client = (Client) pair.getValue();

                        messageManager.sendConfirm(client, step, client.takeFromOutbox());
                    }

                    /*
                     * После всех действий можно обработать новых клиентов.
                     */
                    //logger.log(Level.INFO, "Send welcome messages.");
                    newClientsIterator = newClients.iterator();
                    while (newClientsIterator.hasNext()) {
                        client = (Client) newClientsIterator.next();

                        clientChannelMaps.getClients().put(client.getChannel(), client);
                        messageManager.sendMessage(
                            client,
                            new WelcomeMessage(
                                client.getId(),
                                step,
                                MessageManager.createAllNewUnitMessage(unitManager.getAllUnits()),
                                MessageManager.createAllMoveMessage(movementManager.getAllMovements())
                            )
                        );

                        newClientsIterator.remove();
                    }

                    step++;

                    /*
                     * Обновим позиции игроков.
                     */
                    //logger.log(Level.INFO, "Update units positions.");
                    movementManager.updatePositions(step);
                }
            }
        };
        ScheduledFuture<?> handle = scheduler.scheduleAtFixedRate(mainLoop, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void clientConnected(Channel channel) {
        /*synchronized (clients) {
            Client client = new Client();
            clients.put(channel, client);
            client.getUnits().add(unitManager.createUnit(client.getId(), 4, 4));
            client.getUnits().add(unitManager.createUnit(client.getId(), 5, 5));
            client.getOutbox().add(new WelcomeMessage(client.getId()));
            client.getOutbox().add(MessageManager.createStateReportMessage(step, client.getUnits(), client.getRequestedActions()));
            logger.log(Level.INFO, "Client id:{0},channel:{1} connected.", new Object[] { client.getId(), channel.toString() });
        }*/
    }
    
    public void clientDisconnected(Channel channel) {
       /*
        * Здесь мы не заходим в блок синхронизации, потому что должны
        * немедленно пометить клиента как отключившегося.
        */
        Client client = clientChannelMaps.getClients().get(channel);
        if (client != null) {
            client.setDisconnected(true);
        }
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    /**
     * Утверждает, что клиент отчитался за шаг step.
     * @param c канал
     * @param step шаг
     */
    public void clientReported(Channel c, long step) {
        synchronized (clientChannelMaps) {
            Client client = clientChannelMaps.getClients().get(c);
            if (client != null) {
                client.setLastReportedStep(step);
            } else {
                logger.log(Level.SEVERE, "Attempt to update last reported step of unexisted client. Ignore.");
            }
        }
    }

    /**
     * Добавляет уведомление о действии в исходящие сообщения всем клиентам кроме client.
     * @param client клиент
     * @param action уведомление
     */
    private void notifyAllOtherClients(Client client, Message action) {
        Entry pair;
        Client curClient;

        Iterator i = clientChannelMaps.getClients().entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            curClient = (Client) pair.getValue();
            if (!curClient.equals(client)) {
                curClient.addToOubox(action);
            }
        }
    }

    /**
     * Добавляет уведомления о действиях в исходящие сообщения всем клиентам кроме client.
     * @param client клиент
     * @param actions уведомления
     */
    private void notifyAllOtherClients(Client client, LinkedList<Message> actions) {
        Entry pair;
        Client curClient;

        Iterator i = clientChannelMaps.getClients().entrySet().iterator();
        while (i.hasNext()) {
            pair = (Entry) i.next();
            curClient = (Client) pair.getValue();
            if (!curClient.equals(client)) {
                curClient.addAllToOubox(actions);
            }
        }
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
     * Проверяет, все ли клиенты отчитались за шаг step.
     * @param step шаг
     */
    private boolean isAllClientsReported(long step) {
        Entry pair;
        Client client;

        synchronized (clientChannelMaps) {
            Iterator i = clientChannelMaps.getClients().entrySet().iterator();
            while (i.hasNext()) {
                pair = (Entry) i.next();
                client = (Client) pair.getValue();
                if (!client.isDisconnected() && client.getLastReportedStep() != step) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Удаляет всех клиентов, не отчитавшихся за шаг step.
     * @param step шаг
     */
    private void disconnectUnreportedClients(long step) {
        Entry pair;
        Client client;

        synchronized (clientChannelMaps) {
            Iterator i = clientChannelMaps.getClients().entrySet().iterator();
            while (i.hasNext()) {
                pair = (Entry) i.next();
                client = (Client) pair.getValue();
                if (client.getLastReportedStep() != step) {
                    i.remove();
                    messageManager.sendMessage(
                        client,
                        MessageManager.createErrorMessage("You has been disconnected because report timeout.")
                    );
                    cleanupClient(client);
                    //client.getChannel().close();    // TODO Наверное это надо сделать где-то в другом месте.
                }
            }
        }
    }

    /**
     * Очистка, которая производится при удалении клиента.
     */
    private void cleanupClient(Client client) {
        LinkedList<Unit> removedClients = unitManager.removeUnitsByOwnerId(client.getId());
        notifyAllOtherClients(
            client,
            MessageManager.createAllDelUnitMessage(removedClients)
        );
    }
    
    /**
     * Проверяет, существует ли клиент.
     */
    public boolean isClientExists(Channel channel) {
        synchronized (clientChannelMaps) {
            return clientChannelMaps.getClients().containsKey(channel);
        }
    }

    /**
     * Класс-обёртка для синхронизации доступа к хешам клиентов и каналов.
     */
    private class ClientChannelMaps {

        /**
         * Хеш клиентов. Канал -> Клиент.
         */
        private final ConcurrentHashMap<Channel, Client> clients = new ConcurrentHashMap();

        /**
         * Хеш каналов. Клиент -> Канал.
         */
        private final ConcurrentHashMap<Client, Channel> channels = new ConcurrentHashMap();

        public ConcurrentHashMap<Channel, Client> getClients() {
            return clients;
        }

        public ConcurrentHashMap<Client, Channel> getChannels() {
            return channels;
        }
    }

    /**
     * Запрос действия.
     */
    private class ActionRequest {

       /**
        * Клиент, от которого исходит запрос действия.
        */
        private final Client client;

       /**
        * Запрос действия.
        */
        private final Message request;

        public ActionRequest(Client client, Message request) {
            this.client = client;
            this.request = request;
        }

        public Client getClient() {
            return client;
        }

        public Message getRequest() {
            return request;
        }
    }
}
