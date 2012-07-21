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
         * Индекс клетки назначения по ширине.
         */
        private var j:int;
        
        /**
         * Индекс клетки назначения по высоте.
         */
        private var i:int;
        
        /**
         * Создаёт требование хода.
         * @param unitId идентификатор движущегося юнита
         * @param j индекс клетки назначения по ширине
         * @param i индекс клетки назначения по высоте
         */
        public function MoveRequestMessage(unitId:int, j:int, i:int) {
            this.unitId = unitId;
            this.j = j;
            this.i = i;
        }
        
        /**
         * Возвращает сообщение в формате Protocol-1.
         * @return сообщение в формате Protocol-1
         */
        public function toProtocol1():String {
            return "MOVEREQ," + unitId + "," + j + "," + i;
        }
    }
}
