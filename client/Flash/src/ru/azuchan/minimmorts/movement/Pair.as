package ru.azuchan.minimmorts.movement {

	/**
     * Пара ключ-значение.
     * @author sorc
     */
    public class Pair {
        
        /**
         * Ключ.
         */
        private var _key:Array;
        
        /**
         * Значение.
         */
        private var _value:int;
        
        /**
         * Создаёт новую пару ключ-значение.
         * @param key ключ
         * @param value значение
         */
        public function Pair(key:Array, value:int) {
            _key = key;
            _value = value;
        }
        
        /**
         * Ключ.
         */
        public function get key():Array {
            return _key;
        }
        
        /**
         * Значение.
         */
        public function get value():int {
            return _value;
        }
        
        public function set value(value:int):void {
            _value = value;
        }
    }
}
