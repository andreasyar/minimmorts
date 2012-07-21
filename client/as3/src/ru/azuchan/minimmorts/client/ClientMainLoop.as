package ru.azuchan.minimmorts.client {
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    import ru.azuchan.minimmorts.actions.ActionManager;
    import ru.azuchan.minimmorts.messages.MessageManager;
    import ru.azuchan.minimmorts.messages.StateReportMessage;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.net.ServerInteraction;
    import ru.azuchan.minimmorts.units.UnitManager;

	/**
     * ...
     * @author sorc
     */
    public class ClientMainLoop {
        
        private var messageManager:MessageManager;
        
        private var movementManager:MovementManager;
        
        private var actionManager:ActionManager;
        
        private var unitManager:UnitManager;
        
        private var serverInteraction:ServerInteraction;
        
        private var onStepComplete:Function;
        
        private var firstClientStep:Boolean;
        
        private var idleMode:Boolean;
        
        public static var stepBegTime:Number;
        
        public static var prevStepTime:Number;
        
        private var speedUp:Boolean;
        
        private var slowDown:Boolean;
        
        private var clientMainLoopTimer:Timer;
        
        public function ClientMainLoop(messageManager:MessageManager, movementManager:MovementManager, actionManager:ActionManager, unitManager:UnitManager, serverInteraction:ServerInteraction, onStepComplete:Function) {
            this.messageManager = messageManager;
            this.movementManager = movementManager;
            this.actionManager = actionManager;
            this.unitManager = unitManager;
            this.serverInteraction = serverInteraction;
            this.onStepComplete = onStepComplete;
            firstClientStep = true;
            idleMode = false;
            stepBegTime = -1;
            prevStepTime = -1;
            speedUp = false;
            slowDown = false;
        }
        
        public function run(event:TimerEvent):void {
            if (!firstClientStep) {
                var confirm:Array = messageManager.takeNotification(Main.step);
                if (confirm != null) {
                    confirm = confirm[0];
                    for (var m:int = 0; m < confirm[2].length; m++) {
                        switch(confirm[2][m][0]) {
                            case "NEWUNIT":
                                messageManager.handleNewUnitMessage(confirm[2][m]);
                                break;
                            case "DELUNIT":
                                messageManager.handleDelUnitMessage(confirm[2][m]);
                                break;
                            case "MOVEUPDATE":
                                messageManager.handleMoveUpdateMessage(confirm[2][m]);
                                break;
                            case "MOVE":
                                messageManager.handleMoveMessage(confirm[2][m]);
                                break;
                        }
                    }
                    if (Config.DEBUG_MODES) {
                        trace(Main.step + ": Действия за текущий ход найдены. Кеш действий: "/* + confirmsCacheToString()*/);
                    }
                } else {
                    if (Config.DEBUG_MODES) {
                        trace(Main.step + ": Действия за текущий ход не найдены. Кеш действий: "/* + confirmsCacheToString()*/);
                    }
                    if (!idleMode) {
                        return;
                    }
                }
            } else {
                firstClientStep = false;
                if (Config.DEBUG_MODES) {
                    trace(Main.step + ": Первая итерация главного цикла.");
                }
            }
            
            if (stepBegTime != -1) {
                prevStepTime = (new Date()).getTime() - stepBegTime;
            }
            stepBegTime = (new Date()).getTime();
            
            Main.step++;
            
            movementManager.updatePositions(Main.step);
            //Main.map.updateMinimap();
            
            var unitsHash:Object = unitManager.getUnitsHash();
            var unitsCount:int = unitsHash.unitsCount;
            var unitsString:String = unitsHash.unitsString;
            var actions:Array = actionManager.takeMovementRequests();
            var actionsCount:int = actions.length;
            var actionsString:String = "";
            for (var i:int = 0; i < actions.length; i++) {
                actionsString += actions[i].toProtocol1() + ",";
            }
            if (actions.length > 0) {
                actionsString = actionsString.substring(0, actionsString.length - 1);
            }
            serverInteraction.sendMessage(new StateReportMessage(Main.step, unitsCount, unitsString, actionsCount, actionsString).toProtocol1());
            
            if (!idleMode) {
                if (!firstClientStep) {
                    if (messageManager.getNotificationsLength() > 3 && (speedUp || (!slowDown && !speedUp))) {
                        // Overflow.
                        restart(Main.stepTime - 3);
                        slowDown = true;
                        speedUp = false;
                        trace(Main.step + ": Slow down client loop.");
                    } else if (messageManager.getNotificationsLength() < 2 && (slowDown || (!slowDown && !speedUp))) {
                        // Underflow.
                        restart(Main.stepTime + 3);
                        slowDown = false;
                        speedUp = true;
                        trace(Main.step + ": Speed up client loop.");
                    }
                }
            }
            
            onStepComplete();
        }
        
        public function start():void {
            clientMainLoopTimer = new Timer(Main.stepTime);
            clientMainLoopTimer.addEventListener("timer", run);
            clientMainLoopTimer.start();
        }
        
        public function restart(stepTime:int):void {
            clientMainLoopTimer.stop();
            clientMainLoopTimer = new Timer(stepTime);
            clientMainLoopTimer.addEventListener("timer", run);
            clientMainLoopTimer.start();
        }
    }
}
