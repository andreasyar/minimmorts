package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * Движение.
     * @author sorc
     */
    public class Movement {
        
        /**
         * Юнит.
         */
        private var _unit:Unit;
        
        /**
         * Скорость движения в клетках в шаг.
         */
        private var _speed:Number;
        
        /**
         * Номер хода, когда движение началось.
         */
        private var _begStep:int;
        
        /**
         * Точки, по которым строится путь движения.
         */
        private var _waypoints:Array;
        
        /**
         * Путь движения.
         */
        private var _path:Array;
        
        /**
         * Флаг окончания движения.
         */
        private var _finished:Boolean;
        
        /**
         * Создаёт новое движение.
         * @param unit юнит
         * @param speed скорость движения в клетках в шаг
         * @param begStep номер хода, когда движение началось
         * @param waypoints точки, по которым строится путь движения
         * @param path путь движения
         */
        public function Movement(unit:Unit, speed:Number, begStep:int, waypoints:Array, path:Array) {
            _unit = unit;
            _speed = speed;
            _begStep = begStep;
            _waypoints = waypoints;
            _path = path;
            _finished = false;
        }
        
        /**
         * Юнит.
         */
        public function get unit():Unit {
            return _unit;
        }
        
        /**
         * Скорость движения в клетках в шаг.
         */
        public function get speed():Number {
            return _speed;
        }
        
        /**
         * Номер хода, когда движение началось.
         */
        public function get begStep():int {
            return _begStep;
        }
        
        public function set begStep(begStep:int):void {
            _begStep = begStep;
        }
        
        /**
         * Точки, по которым строится путь движения.
         */
        public function get waypoints():Array {
            return _waypoints;
        }
        
        public function set waypoints(waypoints:Array):void {
            _waypoints = waypoints;
        }
        
        /**
         * Путь движения.
         */
        public function get path():Array {
            return _path;
        }
        
        public function set path(path:Array):void {
            _path = path;
        }
        
        /**
         * Флаг окончания движения.
         */
        public function get finished():Boolean {
            return _finished;
        }
        
        /**
         * Обвновляет положение юнита.
         * @param step текущий шаг
         */
        internal function updatePosition(step:int):void {
            var n:int = Math.floor((step - _begStep) * _speed);
            if (_path == null) {
                trace("Положение юнита " + _unit + " не может быть обновлено. Путь null");
                return;
            }
            if (_path.length <= 0) {
                trace("Положение юнита " + _unit + " не может быть обновлено. Путь пуст");
                return;
            }
            if (n >= _path.length - 1) {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Юнит " + _unit + " сходил [" + _unit.j + "," + _unit.i + "]->[" + _path[_path.length - 1].j + "," + _path[_path.length - 1].i + "] и движение звершилось.");
                }
                _unit.j = _path[_path.length - 1].j;
                _unit.i = _path[_path.length - 1].i;
                _finished = true;
            } else {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Юнит " + _unit + " сходил [" + _unit.j + "," + _unit.i + "]->[" + _path[n].j + "," + _path[n].i + "]");
                }
                _unit.j = _path[n].j;
                _unit.i = _path[n].i;
            }
        }
        
        /**
         * Возвращает строковое представляение движения.
         * @return строковое представляение движения
         */
        public function toString():String {
            return "begJ=" + _waypoints[0].j +
                   ",begI=" + _waypoints[0].i +
                   ",endJ=" + _waypoints[_waypoints.length - 1].j +
                   ",endI=" + _waypoints[_waypoints.length - 1].i +
                   ",finished=" + _finished +
                   ",beg_step=" + _begStep;
        }
    }
}
