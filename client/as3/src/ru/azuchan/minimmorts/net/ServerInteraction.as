package ru.azuchan.minimmorts.net {
    import com.worlize.websocket.*;
    import ru.azuchan.minimmorts.messages.MessageManager;

	/**
     * Взаимодействие с сервером. Установление соединения, отключение, приём сообщений.
     * @author sorc
     */
    public class ServerInteraction {
        
        private var url:String;
        
        private var websocket:WebSocket;
        
        private var messageManager:MessageManager;
        
        public function ServerInteraction(messageManager:MessageManager) {
            this.messageManager = messageManager;
        }
        
        public function connect(url:String):void {
            this.url = url;
            
            Main.tf.text += ",Create new web socket";
            websocket = new WebSocket(this.url, "*");
            websocket.addEventListener(WebSocketEvent.CLOSED, handleWebSocketClosed);
            websocket.addEventListener(WebSocketEvent.OPEN, handleWebSocketOpen);
            websocket.addEventListener(WebSocketEvent.MESSAGE, handleWebSocketMessage);
            websocket.addEventListener(WebSocketErrorEvent.CONNECTION_FAIL, handleConnectionFail);
            websocket.connect();
            Main.tf.text += ",Call connect()";
        }
        
        private function handleWebSocketOpen(event:WebSocketEvent):void {
            Main.tf.text += ",Connected to server: " + url;
            trace("Connected to server: " + url);
            sendMessage("LOGIN,as3,as3"); // TODO: На самом деле пользователь должен вводить логин и пароль.
        }
        
        private function handleWebSocketClosed(event:WebSocketEvent):void {
            Main.tf.text += ",Disconnected from server";
            trace("Disconnected from server.");
        }
        
        private function handleConnectionFail(event:WebSocketErrorEvent):void {
            Main.tf.text += ",Connection failure: " + event.text;
            trace("Connection failure: " + event.text);
        }
        
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
         * @param message Сообщение.
         */
        public function sendMessage(message:String):void {
            if (Config.DEBUG_PROTOCOL) {
                trace("-> " + message);
            }
            websocket.sendUTF(message);
        }
    }
}
