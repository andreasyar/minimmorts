package ru.azuchan.minimmorts.messages {
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.units.UnitManager;

	/**
     * Менеджер сообщений. Работает с сообщениями от и к серверу.
     * @author sorc
     */
    public class MessageManager {
        
        private var movementManager:MovementManager;
        
        private var unitManager:UnitManager;
        
        private var welcomeReceivedCallback:Function;
        
        private var notificationsCache:Array;
        
        public function MessageManager(movementManager:MovementManager, unitManager:UnitManager, welcomeReceivedCallback:Function) {
            this.movementManager = movementManager;
            this.unitManager = unitManager;
            this.welcomeReceivedCallback = welcomeReceivedCallback;
            notificationsCache = [];
        }
        
        public function handleErrorMessage(message:Array):void {
            
        }
        
        public function handleSyncErrorMessage(message:Array):void {
            
        }
        
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
            
            welcomeReceivedCallback();
        }
        
        public function handleConfirmMessage(message:Array):void {
            addNotification(message);
        }
        
        public function handlePingMessage(message:Array):void {
            
        }
        
        public function handleStepTimeMessage(message:Array):void {
            
        }
        
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
        
        public function handleNewUnitMessage(message:Array):void {
            // Example: ["NEWUNIT",{"ownerId":6133,"id":6134,"j":20,"i":20,"r":184,"g":222,"b":105}]
            
            var unit:Object = message[1];
            unitManager.addUnit(unit.ownerId, unit.id, unit.j, unit.i, unit.r, unit.g, unit.b);
            
            if (Config.DEBUG_UNITS) {
                trace("Создан юнит id=" + unit.id);
            }
        }
        
        public function handleDelUnitMessage(message:Array):void {
            // Example: ["DELUNIT",18191]
            
            if (!unitManager.delUnitById(message[1]) && Config.DEBUG_UNITS) {
                trace("Юнит не может быть удалён. Юнит id=" + message[1] + " не найден.");
            } else if (Config.DEBUG_UNITS) {
                trace("Удалён юнит id=" + message[1]);
            }
        }
        
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
        
        public function takeNotification(step:int):Array {
            for (var i:int = 0; i < notificationsCache.length; i++) {
                if (notificationsCache[i][1] == step) {
                    return notificationsCache.splice(i, 1);
                }
            }
            
            return null;
        }
        
        public function getNotificationsLength():int {
            return notificationsCache.length;
        }
        
        private function addNotification(notification:Array):void {
            notificationsCache.push(notification);
        }
    }
}
