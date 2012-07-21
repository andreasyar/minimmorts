package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * ...
     * @author sorc
     */
    public class Follow {
        
        private var object:Unit;
        
        private var target:Unit;
        
        private var objectDest:Point;
        
        public function Follow(object:Unit, target:Unit) {
            this.object = object;
            this.target = target;
            objectDest = null;
        }
        
        public function getObject():Unit {
            return object;
        }
        
        public function getTarget():Unit {
            return target;
        }
        
        public function setTarget(target:Unit):void {
            this.target = target;
        }
        
        public function getObjectDest():Point {
            return objectDest;
        }
        
        public function setObjectDest(objectDest:Point):void {
            this.objectDest = objectDest;
        }
    }
}
