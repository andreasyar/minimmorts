package ru.azuchan.minimmorts.movement {

	/**
     * Точка.
     * @author sorc
     */
    public class Point {
        
        public var j:int;
        
        public var i:int;
        
        /**
         * Создаёт новую точку.
         * @param j индекс точки по ширине
         * @param i индекс точки по высоте
         */
        public function Point(j:int, i:int) {
            this.j = j;
            this.i = i;
        }
        
        public function toString():String {
            return "[" + j + "," + i + "]";
        }
        
        public function equals(obj:Object):Boolean {
            if (obj is Point) {
                return obj.j == j && obj.i == i;
            }
            return false;
        }
    }
}
