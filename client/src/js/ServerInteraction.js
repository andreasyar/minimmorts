/**
 * Отправка и приём сообщений с сервера.
 * Require messages.js
 */
function ServerInteraction() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Поддерживаются ли веб сокеты браузером клиента?
    if (!window.WebSocket && !window.MozWebSocket) {
        throw new Error("Your browser does not support Web Socket!");
    }

    /**
     * Веб сокет.
     */
    this.socket = null;

    /**
     * Соединяется с серевером.
     * @param addr адрес сервера
     */
    this.connect = function(addr) {
        console.log('Соединяемся с ' + addr)

        if (window.WebSocket) {
            this.socket = new WebSocket(addr);
        } else {
            this.socket = new MozWebSocket(addr);
        }
        this.socket.parent = this;

        /**
         * Обработчик получения сообщения от сервера.
         * @param event событие получения сообщения
         */
        this.socket.onmessage = function(event) {
            if (DEBUG_PROTOCOL) {
                console.log("<- " + event.data);
            }
            if (event.data.indexOf("PING") == 0) {
                MESSAGE_HANDLERS["PING"](event.data);
            } else {
                var obj = JSON.parse(event.data)
                for (var key in MESSAGE_HANDLERS) {
                    if ((obj[0] != undefined && obj[0] == key) || (obj[0] != undefined && obj[0][0] != undefined && obj[0][0] == key)) {
                        MESSAGE_HANDLERS[key](obj);
                        break;
                    }
                }
            }
        }

        /**
         * Обработчик открытия соединения с сервером.
         * @param event событие открытия соединения
         */
        this.socket.onopen = function(event) {
            console.log("Web Socket opened.");
            var login = getLogin();
            if (/^[a-zA-Z0-9]+$/.test(login)) {
                var password = getPassword();
                if (/^[a-zA-Z0-9]+$/.test(password)) {
                    server.sendMessage(new LoginMessage(login, password).toProtocol1());
                }
            }
        };

        /**
         * Обработчик закрытия соединения с сервером.
         * @param event событие закрытия соединения
         */
        this.socket.onclose = function(event) {
            console.log("Web Socket closed.");
        };
    };

    /**
     * Отправляет сообщение.
     * @param message Сообщение.
     */
    this.sendMessage = function(message) {
        if (this.socket == null || this.socket.readyState != WebSocket.OPEN) {
            throw new Error("Cant send message. Connection not established.");
        } else {
            //message = JSON.stringify(message);
            if (DEBUG_PROTOCOL) {
                console.log("-> " + message);
            }
            this.socket.send(message);
        }

        return true;
    }
}
