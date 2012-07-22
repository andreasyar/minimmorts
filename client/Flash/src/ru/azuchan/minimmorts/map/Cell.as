package ru.azuchan.minimmorts.map {
    
	/**
     * Клетка карты.
     * @author sorc
     */
    public class Cell {
        
        /**
         * Индекс клетки карты по ширине.
         */
        private var _j:int;
        
        /**
         * Индекс клетки карты по высоте.
         */
        private var _i:int;
        
        /**
         * Создаёт новую клетку карты.
         * @param j индекс клетки карты по ширине
         * @param i индекс клетки карты по высоте
         */
        public function Cell(j:int, i:int) {
            _j = j;
            _i = i;
        }
        
        /**
         * Индекс клетки карты по ширине.
         */
        public function get j():int {
            return _j;
        }
        
        /**
         * Индекс клетки карты по высоте.
         */
        public function get i():int {
            return _i;
        }
        
        /**
         * Проверяет, равна ли клетка карты заданному объекту.
         * @param obj объект для сравнения
         * @return равна ли клетка карты заданному объекту
         */
        public function equals(obj:Object):Boolean {
            if (obj is Cell) {
                return obj.j == _j && obj.i == _i;
            }
            
            return false;
        }
        
        /**
         * Проверяет, имеет ли клетка карты заданные индексы на карте.
         * @param j индекс по ширине
         * @param i индекс по высоте
         * @return имеет ли клетка карты заданные индексы
         */
        public function equals2(j:int, i:int):Boolean {
            return _j == j && _i == i;
        }
        
        /**
         * Возвращает строковое представляение клетки карты.
         * @return строковое представляение клетки карты
         */
        public function toString():String {
            return "[" + _j + "," + _i + "]";
        }
        
        /**
         * Возвращает клетку карты в формате Protocol-1.
         * @return клетка карты в формате Protocol-1
         */
        public function toProtocol1():String {
            return _j + "," + _i;
        }
    }
}