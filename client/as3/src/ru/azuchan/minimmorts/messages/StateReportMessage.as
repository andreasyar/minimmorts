package ru.azuchan.minimmorts.messages {

	/**
     * Отчёт о состоянии клиента.
     * @author sorc
     */
    public class StateReportMessage {
        
        /**
         * Номер шага.
         */
        private var step:int;
        
        /**
         * Количество юнитов в строке юнитов.
         */
        private var unitsCount:int;
        
        /**
         * Строка юнитов.
         */
        private var unitsString:String;
        
        /**
         * Количество действий в строке действий.
         */
        private var actionsCount:int;
        
        /**
         * Строка действий.
         */
        private var actionsString:String;
        
        /**
         * Создаёт отчёт о состоянии клиента.
         * @param step Номер шага.
         * @param unitsCount Количество юнитов в строке юнитов.
         * @param unitsString Строка юнитов.
         * @param actionsCount Количество действий в строке действий.
         * @param actionsString Строка действий.
         */
        public function StateReportMessage(step:int, unitsCount:int, unitsString:String, actionsCount:int, actionsString:String) {
            this.step = step;
            this.unitsCount = unitsCount;
            this.unitsString = unitsString;
            this.actionsCount = actionsCount;
            this.actionsString = actionsString;
        }
        
        /**
         * Преобразовывает сообщение в формат Protocol-1.
         */
        public function toProtocol1():String {
            var out:String = "STATE," + step + "," + unitsCount;
            if (unitsCount > 0) {
                out += "," + unitsString;
            }
            out += "," + actionsCount;
            if (actionsCount > 0) {
                out += "," + actionsString;
            }
            return out;
        }
    }
}
