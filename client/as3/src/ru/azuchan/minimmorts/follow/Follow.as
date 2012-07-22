package ru.azuchan.minimmorts.follow {
    import ru.azuchan.minimmorts.map.Cell;
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * Преследование.
     * @author sorc
     */
    public class Follow {
        
        /**
         * Субъект.
         */
        private var _subject:Unit;
        
        /**
         * Объект.
         */
        private var _object:Unit;
        
        /**
         * Клетка назначения движения субъекта.
         */
        private var _subjectDest:Cell;
        
        /**
         * Создаёт новое преследование.
         * @param subject субъект
         * @param object объект
         */
        public function Follow(subject:Unit, object:Unit) {
            _subject = subject;
            _object = object;
            _subjectDest = null;
        }
        
        /**
         * Субъект.
         */
        public function get subject():Unit {
            return _subject;
        }
        
        /**
         * Объект.
         */
        public function get object():Unit {
            return _object;
        }
        
        /**
         * Клетка назначения движения субъекта.
         */
        public function get subjectDest():Cell {
            return _subjectDest;
        }
        
        public function set object(object:Unit):void {
            _object = object;
        }
        
        public function set subjectDest(subjectDest:Cell):void {
            _subjectDest = subjectDest;
        }
    }
}
