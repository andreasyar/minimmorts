package ru.azuchan.minimmorts.messages {
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.units.UnitManager;

	/**
     * Менеджер сообщений. Работает с сообщениями от и к серверу.
     * @author sorc
     */
    public class MessageManager {
        
        /**
         * Менеджер движений.
         */
        private var movementManager:MovementManager;
        
        /**
         * Менеджер юнитов.
         */
        private var unitManager:UnitManager;
        
        /**
         * Обработчик получения сообщения WELCOME.
         */
        private var welcomeReceivedCallback:Function;
        
        /**
         * Обработчик получения сообщения STEPTIME.
         */
        private var stepTimeReceivedCallback:Function;
        
        /**
         * Кеш уведомление от сервера.
         */
        private var notificationsCache:Array;
        
        /**
         * Создаёт новый менеджер сообщений.
         * @param movementManager менеджер движений
         * @param unitManager менеджер юнитов
         * @param welcomeReceivedCallback обработчик получения сообщения WELCOME
         */
        public function MessageManager(movementManager:MovementManager, unitManager:UnitManager) {
            this.movementManager = movementManager;
            this.unitManager = unitManager;
            notificationsCache = [];
        }
        
        /**
         * Добавляет обработчик события для различных типов событий.
         * @param type событие
         * @param listener обработчик
         */
        public function addEventListener(type:String, listener:Function):void {
            if (type == "WELCOME") {
                welcomeReceivedCallback = listener;
            } else if (type == "STEPTIME") {
                stepTimeReceivedCallback = listener;
            }
        }
        
        /**
         * Обработчик сообщения об ошибке.
         * @param message сообщение об ошибке
         */
        public function handleErrorMessage(message:Array):void {
            Main.tf.text = Main.tf.text.concat(",Error");
        }
        
        /**
         * Обработчик сообщения об ошибке синхронизации.
         * @param message сообщение об ошибке синхронизации
         */
        public function handleSyncErrorMessage(message:Array):void {
            Main.tf.text = Main.tf.text.concat(",Sync Error");
        }
        
        /**
         * Обработчик сообщения WELCOME.
         * @param message сообщение WELCOME
         */
        public function handleWelcomeMessage(message:Array):void {
            // Example: ["WELCOME",6139,314284,[["NEWUNIT",{"ownerId":6133,"id":6134,"j":20,"i":20,"r":184,"g":222,"b":105}]],[["MOVE",{"begStep":314256,"unitId":6134,"waypoints":[{"j":28,"i":12},{"j":6,"i":46}]}]],100]
            
            Main.id = parseInt(message[1]);
            Main.step = parseInt(message[2]);
            var units:Array = message[3];
            var actions:Array = message[4];
            Main.stepTime = parseInt(message[5]);
            
            var i:int;
            for (i = 0; i < units.length; i++) {
                handleNewUnitMessage(units[i]);
            }
            for (i = 0; i < actions.length; i++) {
                switch(actions[i][0]) {
                    case "MOVE":
                        handleMoveMessage(actions[i]);
                        break;
                }
            }
            
            if (welcomeReceivedCallback is Function) {
                welcomeReceivedCallback();
            }
        }
        
        /**
         * Обработчик сообщения CONFIRM (уведомления от сервера).
         * @param message сообщение CONFIRM (уведомление от сервера)
         */
        public function handleConfirmMessage(message:Array):void {
            addNotification(message);
        }
        
        /**
         * Обработчик сообщения PING.
         * @param message сообщение PING
         */
        public function handlePingMessage(message:Array):void {
            
        }
        
        /**
         * Обработчик сообщения STEPTIME (сообщение об увеличении времени хода).
         * @param message сообщение STEPTIME
         */
        public function handleStepTimeMessage(message:Array):void {
            trace(Main.step + ": Время хода изменилось на " + message[1]);
            Main.stepTime = message[1] as int;
            if (stepTimeReceivedCallback is Function) {
                stepTimeReceivedCallback();
            }
        }
        
        /**
         * Обработчик сообщения MOVE.
         * @param message сообщение MOVE
         */
        public function handleMoveMessage(message:Array):void {
            // Example: ["MOVE",{"begStep":314256,"unitId":6134,"waypoints":[{"j":28,"i":12},{"j":6,"i":46}]}]
            
            var moveMessage:Object = message[1];
            var unit:Unit = unitManager.getUnitById(moveMessage.unitId);
            if (unit == null) {
                trace("Движение не может быть создано. Юнит id=" + moveMessage.unitId + " не найден.");
                return;
            }
            
            movementManager.startNewMovement(moveMessage.begStep, unit, moveMessage.waypoints);
        }
        
        /**
         * Обработчик сообщения NEWUNIT.
         * @param message сообщение NEWUNIT
         */
        public function handleNewUnitMessage(message:Array):void {
            // Example: ["NEWUNIT",{"ownerId":6133,"id":6134,"j":20,"i":20,"r":184,"g":222,"b":105}]
            
            var unit:Object = message[1];
            unitManager.addUnit(unit.ownerId, unit.id, unit.j, unit.i, unit.r, unit.g, unit.b);
            
            if (Config.DEBUG_UNITS) {
                trace("Создан юнит id=" + unit.id);
            }
        }
        
        /**
         * Обработчик сообщения DELUNIT.
         * @param message сообщение DELUNIT
         */
        public function handleDelUnitMessage(message:Array):void {
            // Example: ["DELUNIT",18191]
            
            if (!unitManager.delUnitById(message[1]) && Config.DEBUG_UNITS) {
                trace("Юнит не может быть удалён. Юнит id=" + message[1] + " не найден.");
            } else if (Config.DEBUG_UNITS) {
                trace("Удалён юнит id=" + message[1]);
            }
        }
        
        /**
         * Обработчик сообщения MOVEUPDATE.
         * @param message сообщение MOVEUPDATE
         */
        public function handleMoveUpdateMessage(message:Array):void {
            // Example: ["MOVEUPDATE",{"begStep":890724,"unitId":18194,"waypoints":[{"j":56,"i":43},{"j":57,"i":44},{"j":43,"i":10}]}]

            var moveMessage:Object = message[1];
            var unit:Unit = unitManager.getUnitById(moveMessage.unitId);
            
            if (unit == null) {
                trace("Движение не может быть обновлено. Юнит id=" + moveMessage.unitId + " не найден.");
                return;
            }

            movementManager.updateMovement(
                moveMessage.begStep,
                unit,
                moveMessage.waypoints
            );
        }
        
        /**
         * Возвращает уведомление из кеша уведомлений от сервера. Возвращаемое
         * уведомление удаляется из кеша.
         * @param step шаг, за который требуется уведомление
         * @return уведомление от сервера
         */
        public function takeNotification(step:int):Array {
            for (var i:int = 0; i < notificationsCache.length; i++) {
                if (notificationsCache[i][1] == step) {
                    return notificationsCache.splice(i, 1);
                }
            }
            
            return null;
        }
        
        /**
         * Возвращает количество уведомлений от сервера.
         * @return количество уведомлений от сервера
         */
        public function getNotificationsLength():int {
            return notificationsCache.length;
        }
        
        /**
         * Добавляет уведомление от сервера в кеш уведомлений.
         * @param notification уведомление
         */
        private function addNotification(notification:Array):void {
            notificationsCache.push(notification);
        }
        
        public function notificationsCacheToString():String {
            var out:String = "";
            for (var i:int = 0; i < notificationsCache.length; i++) {
                out += notificationsCache[i][1] + ","
            }
            return out;
        }
    }
}
