package ru.azuchan.minimmorts.movement {
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    import ru.azuchan.minimmorts.actions.ActionManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.map.Cell;

	/**
     * ...
     * @author sorc
     */
    public class FollowLoop {
        
        private var movementManager:MovementManager;
        
        private var actionManager:ActionManager;
        
        private var followLoopTimer:Timer;
        
        public function FollowLoop(movementManager:MovementManager, actionManager:ActionManager) {
            this.movementManager = movementManager;
            this.actionManager = actionManager;
        }
        
        private function run(event:TimerEvent):void {
            var object:Unit;
            var target:Unit;
            var objectDest:Point;
            var targetPos:Cell;
            var followings:Array = movementManager.getFollowings();
            
            for (var i:int = 0; i < followings.length; i++) {
                object = followings[i].getObject();
                target = followings[i].getTarget();
                if ( (objectDest = followings[i].getObjectDest()) == null) {
                    objectDest = new Point(object.j, object.i);
                }
                targetPos = new Cell(target.j, target.i);
                
                if (!targetPos.equals(objectDest)) {
                    actionManager.moveUnit(object, targetPos);
                    followings[i].setObjectDest(targetPos);
                }
            }
        }
        
        public function start():void {
            followLoopTimer = new Timer(500);
            followLoopTimer.addEventListener("timer", run);
            followLoopTimer.start();
        }
        
        public function stop():void {
            followLoopTimer.stop();
        }
    }
}
