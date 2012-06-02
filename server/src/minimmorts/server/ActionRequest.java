package minimmorts.server;

import minimmorts.client.Client;
import minimmorts.messages.Message;

/**
 * Обёртка для хранения запросов действий клиентов.
 * @author Sorc
 */
class ActionRequest {

    /**
     * Клиент, от которого исходит запрос действия.
     */
    private final Client client;

    /**
     * Запрос действия.
     */
    private final Message request;

    ActionRequest(Client client, Message request) {
        this.client = client;
        this.request = request;
    }

    Client getClient() {
        return client;
    }

    Message getRequest() {
        return request;
    }
}
