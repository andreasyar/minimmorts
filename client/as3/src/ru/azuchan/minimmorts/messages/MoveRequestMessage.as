package ru.azuchan.minimmorts.messages {
    
	/**
     * Требование хода.
     * @author sorc
     */
    public class MoveRequestMessage {
        
        /**
         * Идентификатор движущегося юнита.
         */
        private var unitId:int;
        
        /**
         * j-индекс клетки назначения.
         */
        private var j:int;
        
        /**
         * i-индекс клетки назначения.
         */
        private var i:int;
        
        /**
         * Создаёт требование хода.
         * @param unitId Идентификатор движущегося юнита.
         * @param j j-индекс клетки назначения.
         * @param i i-индекс клетки назначения.
         */
        public function MoveRequestMessage(unitId:int, j:int, i:int) {
            this.unitId = unitId;
            this.j = j;
            this.i = i;
        }
        
        /**
         * Преобразовывает сообщение в формат Protocol-1.
         */
        public function toProtocol1():String {
            return "MOVEREQ," + unitId + "," + j + "," + i;
        }
    }
}
