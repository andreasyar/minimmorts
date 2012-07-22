package ru.azuchan.minimmorts.follow {
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    import ru.azuchan.minimmorts.actions.ActionManager;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.map.Cell;

	/**
     * Цикл преследования.
     * @author sorc
     */
    public class FollowLoop {
        
        /**
         * Частота итераций цикла преследования в милисекундах.
         */
        private static const FREQUENCY:int = 500;
        
        /**
         * Менеджер действий пользователя.
         */
        private var actionManager:ActionManager;
        
        /**
         * Менеджер преследований.
         */
        private var followManager:FollowManager;
        
        /**
         * Таймер цикла преследования.
         */
        private var followLoopTimer:Timer;
        
        /**
         * Создаёт новый цикл преследования.
         * @param actionManager менеджер действий пользователя
         * @param followManager менеджер преследований
         */
        public function FollowLoop(actionManager:ActionManager, followManager:FollowManager) {
            this.actionManager = actionManager;
            this.followManager = followManager;
        }
        
        /**
         * Тело цикла преследования.
         * @param event событие таймера
         */
        private function run(event:TimerEvent):void {
            var objectPos:Cell;
            var followings:Array = followManager.getFollowings();
            
            for (var i:int = 0; i < followings.length; i++) {
                if (followings[i].subjectDest == null) {
                    followings[i].subjectDest = new Cell(followings[i].subject.j, followings[i].subject.i);
                }
                objectPos = new Cell(followings[i].object.j, followings[i].object.i);
                
                if (!objectPos.equals(followings[i].subjectDest)) {
                    actionManager.moveUnit(followings[i].subject, objectPos);
                    followings[i].subjectDest = objectPos;
                }
            }
        }
        
        /**
         * Запускает цикл преследования.
         */
        public function start():void {
            followLoopTimer = new Timer(FollowLoop.FREQUENCY);
            followLoopTimer.addEventListener("timer", run);
            followLoopTimer.start();
        }
        
        /**
         * Останавливает цикл преследования.
         */
        public function stop():void {
            followLoopTimer.stop();
        }
    }
}
