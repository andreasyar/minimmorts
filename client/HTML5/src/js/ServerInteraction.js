
// Взаимодействите с сервером.
// Require messages.js

// Отправка и приём сообщений с сервера.
function ServerInteraction() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Поддерживаются ли веб сокеты браузером клиента?
    if (!window.WebSocket && !window.MozWebSocket) {
        throw new Error("Your browser does not support Web Socket!");
    }

    // Веб сокет для обмена сообщениями.
    this.socket = null;

    // Соединяется с серевером.
    this.connect = function(addr) {
        console.log('Соединяемся с ' + addr)

        if (window.WebSocket) {
            this.socket = new WebSocket(addr)
        } else {
            this.socket = new MozWebSocket(addr)
        }
        this.socket.parent = this

        /**
         * Обработчик получения сообщения от сервера.
         */
        this.socket.onmessage = function(event) {
            if (DEBUG_PROTOCOL) {
                console.log("<- " + event.data)
            }
            var obj = JSON.parse(event.data)
            for (var key in MESSAGE_HANDLERS) {
                if ((obj[0] != undefined && obj[0] == key) || (obj[0] != undefined && obj[0][0] != undefined && obj[0][0] == key)) {
                    MESSAGE_HANDLERS[key](obj);
                    break;
                }
            }
        }

        // Обработчик открытия соединения с сервером.
        this.socket.onopen = function(event) {
            console.log("Web Socket opened.");
            server.sendMessage(new MessagePacket(new LoginMessage(getLogin(), getPassword())));
        };

        // Обработчик закрытия соединения с сервером.
        this.socket.onclose = function(event) {
            console.log("Web Socket closed.");
        };
    };

    /**
     * Отправляет сообщение.
     * @param msg Сообщение.
     */
    this.sendMessage = function(msg) {
        if (this.socket == null || this.socket.readyState != WebSocket.OPEN) {
            throw new Error("Cant send message. Connection not established.")
        } else {
            msg = JSON.stringify(msg)
            if (DEBUG_PROTOCOL) {
                console.log("-> " + msg);
            }
            this.socket.send(msg);
        }

        return true;
    }
}
