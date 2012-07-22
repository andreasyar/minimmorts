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
     * Главный цикл клиента.
     * @author sorc
     */
    public class ClientMainLoop {
        
        /**
         * Менеджер сообщений.
         */
        private var messageManager:MessageManager;
        
        /**
         * Менеджер движений.
         */
        private var movementManager:MovementManager;
        
        /**
         * Менеджер действий пользователя.
         */
        private var actionManager:ActionManager;
        
        /**
         * Менеджер юнитов.
         */
        private var unitManager:UnitManager;
        
        /**
         * Взаимодействие с сервером.
         */
        private var serverInteraction:ServerInteraction;
        
        /**
         * Обработчик, который вызывается при завершении шага.
         */
        private var onStepComplete:Function;
        
        private var firstClientStep:Boolean;
        
        private var idleMode:Boolean;
        
        public static var stepBegTime:Number;
        
        public static var prevStepTime:Number;
        
        private var speedUp:Boolean;
        
        private var slowDown:Boolean;
        
        private var clientMainLoopTimer:Timer;
        
        private var decraseCounter:int;
        
        private var decraseValue:int;
        
        private var incraseCounter:int;
        
        private var incraseValue:int;
        
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
            decraseCounter = 0;
            decraseValue = 0;
            incraseCounter = 0;
            incraseValue = 0;
        }
        
        public function run(event:TimerEvent):void {
            var derpy:Number = new Date().getTime();
            
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
                        trace(Main.step + ": Действия за текущий ход найдены. Кеш действий: " + messageManager.notificationsCacheToString());
                    }
                } else {
                    if (Config.DEBUG_MODES) {
                        trace(Main.step + ": Действия за текущий ход не найдены. Кеш действий: " + messageManager.notificationsCacheToString());
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
            
            /*if (!idleMode) {
                if (!firstClientStep) {
                    if (messageManager.getNotificationsLength() > 3 && (speedUp || (!slowDown && !speedUp))) {
                        // Overflow.
                        restart(clientMainLoopTimer.delay - 3);
                        slowDown = true;
                        speedUp = false;
                        trace(Main.step + ": Частота главного цикла клиента увеличена.");
                    } else if (messageManager.getNotificationsLength() < 2 && (slowDown || (!slowDown && !speedUp))) {
                        // Underflow.
                        restart(clientMainLoopTimer.delay + 3);
                        slowDown = false;
                        speedUp = true;
                        trace(Main.step + ": Частота главного цикла клиента уменьшена.");
                    }
                }
            }*/
            
            onStepComplete();
            
            if (messageManager.getNotificationsLength() > 3) {
                if (incraseCounter == 0) {
                    incraseValue = clientMainLoopTimer.delay - 3;
                    decraseCounter = 0;
                    decraseValue = 0;
                }
                incraseCounter++;
            } else if (messageManager.getNotificationsLength() < 2) {
                if (decraseCounter == 0) {
                    decraseValue = clientMainLoopTimer.delay + 3;
                    incraseCounter = 0;
                    incraseValue = 0;
                }
                decraseCounter++;
            }
            
            //trace(clientMainLoopTimer.delay, new Date().getTime() - derpy);
            
            if (incraseCounter > 10) {
                incraseCounter = 0;
                decraseCounter = 0;
                decraseValue = 0;
                restart(incraseValue);
                return;
            }
            
            if (decraseCounter > 10) {
                decraseCounter = 0;
                incraseCounter = 0;
                incraseValue = 0;
                restart(decraseValue);
                return;
            }
        }
        
        public function start():void {
            clientMainLoopTimer = new Timer(Main.stepTime);
            trace(Main.step + ": Главный цикл клиента запущен с частотой " + Main.stepTime);
            clientMainLoopTimer.addEventListener("timer", run);
            clientMainLoopTimer.start();
            decraseCounter = 0;
            decraseValue = 0;
            incraseCounter = 0;
            incraseValue = 0;
        }
        
        public function stop():void {
            clientMainLoopTimer.stop();
        }
        
        public function restart(stepTime:int):void {
            clientMainLoopTimer.stop();
            clientMainLoopTimer = new Timer(stepTime);
            trace(Main.step + ": Главный цикл клиента перезапущен с частотой " + stepTime);
            clientMainLoopTimer.addEventListener("timer", run);
            clientMainLoopTimer.start();
        }
    }
}
