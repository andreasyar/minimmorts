package ru.azuchan.minimmorts.net {
    import com.worlize.websocket.*;
    import ru.azuchan.minimmorts.messages.MessageManager;

	/**
     * Взаимодействие с сервером. Установление соединения, отключение, приём сообщений.
     * @author sorc
     */
    public class ServerInteraction {
        
        /**
         * URL веб сокет сервера.
         */
        private var url:String;
        
        /**
         * Веб сокет сервер.
         */
        private var websocket:WebSocket;
        
        /**
         * Менеджер сообщений.
         */
        private var messageManager:MessageManager;
        
        /**
         * Создаёт объект для связи с сервером.
         * @param messageManager менеджер сообщений
         */
        public function ServerInteraction(messageManager:MessageManager) {
            url = "";
            websocket = null;
            this.messageManager = messageManager;
        }
        
        /**
         * Подключение к серверу.
         * @param url URL веб сокет сервера
         */
        public function connect(url:String):void {
            this.url = url;
            websocket = new WebSocket(this.url, "*");
            websocket.addEventListener(WebSocketEvent.CLOSED, handleWebSocketClosed);
            websocket.addEventListener(WebSocketEvent.OPEN, handleWebSocketOpen);
            websocket.addEventListener(WebSocketEvent.MESSAGE, handleWebSocketMessage);
            websocket.addEventListener(WebSocketErrorEvent.CONNECTION_FAIL, handleConnectionFail);
            websocket.connect();
        }
        
        /**
         * Отключение от сервера.
         */
        public function disconnect():void {
            if (websocket.connected) {
                websocket.close();
            }
        }
        
        /**
         * Обработчик открытия соединения с сервером.
         * @param event событие открытия соединения с сервера
         */
        private function handleWebSocketOpen(event:WebSocketEvent):void {
            trace("Выполнено подключение к серверу: " + url);
            sendMessage("LOGIN,as3,as3");   // TODO: На самом деле пользователь должен вводить логин и пароль.
        }
        
        /**
         * Обработчик закрытия соединения с сервером.
         * @param event событие закрытия соединения с сервера
         */
        private function handleWebSocketClosed(event:WebSocketEvent):void {
            trace("Произошло отключение от сервера.");
        }
        
        /**
         * Обработчик ошибки соединения.
         * @param event событие ошибки соединения
         */
        private function handleConnectionFail(event:WebSocketErrorEvent):void {
            trace("Ошибка соединения: " + event.text);
        }
        
        /**
         * Обработчик получения сообщения.
         * @param event событие получения сообщения
         */
        private function handleWebSocketMessage(event:WebSocketEvent):void {
            if (event.message.type !== WebSocketMessage.TYPE_UTF8) {
                trace("Тип сообщений веб сокетов " + event.message.type + " игнорируется.");
                return;
            }
            if (Config.DEBUG_PROTOCOL) {
                trace("<- " + event.message.utf8Data);
            }
            
            var message:Array = JSON.parse(event.message.utf8Data) as Array;
            var type:String = message[0];
            
            switch(type) {
                case "ERROR":
                    messageManager.handleErrorMessage(message);
                    break;
                case "SYNCERROR":
                    messageManager.handleSyncErrorMessage(message);
                    break;
                case "WELCOME":
                    messageManager.handleWelcomeMessage(message);
                    break;
                case "CONFIRM":
                    messageManager.handleConfirmMessage(message);
                    break;
                case "PING":
                    messageManager.handlePingMessage(message);
                    break;
                case "STEPTIME":
                    messageManager.handleStepTimeMessage(message);
                    break;
                default:
                    trace("Получено сообщение неизвестного типа " + type);
                    break;
            }
        }
        
        /**
         * Отправляет сообщение.
         * @param message сообщение
         */
        public function sendMessage(message:String):void {
            if (Config.DEBUG_PROTOCOL) {
                trace("-> " + message);
            }
            websocket.sendUTF(message);
        }
    }
}
