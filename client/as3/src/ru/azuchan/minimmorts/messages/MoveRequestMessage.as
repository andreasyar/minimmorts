package ru.azuchan.minimmorts.messages {
    import ru.azuchan.minimmorts.map.Cell;
    
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
         * Клетка назначения.
         */
        private var dest:Cell;
        
        /**
         * Создаёт требование хода.
         * @param unitId идентификатор движущегося юнита
         * @param j индекс клетки назначения по ширине
         * @param i индекс клетки назначения по высоте
         */
        public function MoveRequestMessage(unitId:int, dest:Cell) {
            this.unitId = unitId;
            this.dest = dest;
        }
        
        /**
         * Возвращает сообщение в формате Protocol-1.
         * @return сообщение в формате Protocol-1
         */
        public function toProtocol1():String {
            return "MOVEREQ," + unitId + "," + dest.toProtocol1();
        }
    }
}
