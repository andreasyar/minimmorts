package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * ...
     * @author sorc
     */
    public class Movement {

        /**
         * Юнит.
         */
        private var unit:Unit;

        /**
         * Скорость движения в клетках в шаг.
         */
        private var speed:Number;

        /**
         * Номер хода, когда движение началось.
         */
        private var begStep:int;

        /**
         * Точки, по которым строится путь движения.
         */
        private var waypoints:Array;

        /**
         * Путь движения.
         */
        private var path:Array;

        /**
         * Флаг окончания движения.
         */
        private var finished:Boolean;
        
        public function Movement(unit:Unit, speed:Number, begStep:int, waypoints:Array, path:Array) {
            this.unit = unit;
            this.speed = speed;
            this.path = path;
            this.begStep = begStep;
            this.waypoints = waypoints;
            finished = false;
        }
        
        public function getPath():Array {
            return path;
        }
        
        internal function setPath(path:Array):void {
            this.path = path;
        }
        
        internal function setWaypoints(waypoints:Array):void {
            this.waypoints = waypoints;
        }
        
        internal function getWaypoints():Array {
            return waypoints;
        }
        
        internal function updatePosition(step:int):void {
            var n:int = Math.floor((step - begStep) * speed);
            if (path == null) {
                trace("Положение юнита " + unit + " не может быть обновлено. Путь null");
                return;
            }
            if (path.length <= 0) {
                trace("Положение юнита " + unit + " не может быть обновлено. Путь пуст");
                return;
            }
            if (n >= path.length - 1) {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Юнит " + unit + " сходил [" + unit.j + "," + unit.i + "]->[" + path[path.length - 1].j + "," + path[path.length - 1].i + "] и движение звершилось.");
                }
                unit.j = path[path.length - 1].j;
                unit.i = path[path.length - 1].i;
                finished = true;
            } else {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Юнит " + unit + " сходил [" + unit.j + "," + unit.i + "]->[" + path[n].j + "," + path[n].i + "]");
                }
                unit.j = path[n].j;
                unit.i = path[n].i;
            }
        }
        
        internal function getUnit():Unit {
            return unit;
        }
        
        public function getSpeed():Number {
            return speed;
        }
        
        public function getBegStep():int {
            return begStep;
        }
        
        internal function setBegStep(begStep:int):void {
            this.begStep = begStep;
        }
        
        internal function isFinished():Boolean {
            return finished;
        }
        
        public function toString():String {
            return "begJ=" + waypoints[0].j +
                   ",begI=" + waypoints[0].i +
                   ",endJ=" + waypoints[waypoints.length - 1].j +
                   ",endI=" + waypoints[waypoints.length - 1].i +
                   ",finished=" + finished +
                   ",beg_step=" + begStep;
        }
    }
}
