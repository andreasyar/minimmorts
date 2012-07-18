package ru.azuchan.minimmorts.movement {

	/**
     * ...
     * @author sorc
     */
    public class Cell {
        
        public var i:int;
        
        public var j:int;
        
        public var f:int;
        
        public var g:int;
        
        public var h:int;
        
        public var parent:Cell;
        
        public function Cell(i:int, j:int, g:int, h:int, parent:Cell) {
            this.i = i;
            this.j = j
            this.f = g + h;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }
        
        public function equal(i:int, j:int):Boolean {
            return this.i == i && this.j == j
        }
        
        public function toString():String {
            return "[" + j + "," + i + "]";
        }
    }
}
