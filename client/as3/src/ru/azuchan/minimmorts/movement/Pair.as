package ru.azuchan.minimmorts.movement {

	/**
     * ...
     * @author sorc
     */
    public class Pair {
        
        private var key:Array;
        
        private var value:int;
        
        public function Pair(key:Array, value:int) {
            this.key = key;
            this.value = value;
        }
        
        public function getKey():Array {
            return key;
        }
        
        public function getValue():int {
            return value;
        }
        
        public function setValue(value:int):int {
            var oldValue:int = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
