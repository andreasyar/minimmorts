package ru.azuchan.minimmorts.movement {

	/**
     * ...
     * @author sorc
     */
    public class CellA {
        
        public var i:int;
        
        public var j:int;
        
        public var f:int;
        
        public var g:int;
        
        public var h:int;
        
        public var parent:CellA;
        
        public function CellA(i:int, j:int, g:int, h:int, parent:CellA) {
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
