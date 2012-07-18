package ru.azuchan.minimmorts.actions {
    import ru.azuchan.minimmorts.messages.MoveRequestMessage;
    import ru.azuchan.minimmorts.movement.Point;
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * ...
     * @author sorc
     */
    public class ActionManager {
        
        private var movementRequests:Array;
        
        public function ActionManager() {
            movementRequests = [];
        }
        
        /**
         * Добавляет запросы движения выделенных юнитов в заданную клетку.
         * При этом все существующие запросы движения удаляются.
         * @param dest клетка назначения
         */
        public function moveSelectedUnits(dest:Point):void {
            movementRequests = [];
            var selectedUnits:Array = Main.unitManager.getSelectedUnits();
            
            for (var i:int = 0; i < selectedUnits.length; i++) {
                movementRequests.push(new MoveRequestMessage(selectedUnits[i].getId(), dest.j, dest.i));
            }
            
            /*var neighbourCells = [dest];
            while (neighbourCells.length < selected_units.length && neighbourCells.length < map.w * map.h) {
                neighbourCells = getNeighboursCells(neighbourCells);
            }
            for (var i = 0; i < Math.min(neighbourCells.length, selected_units.length); i++) {
                if (selected_units[i].getJ() == neighbourCells[i].getJ() && selected_units[i].getI() == neighbourCells[i].getI()) {
                    continue;   // В себя не ходим.
                }
                this.actions.MOVEMENT.push(
                    new MoveRequestMessage(
                        selected_units[i].getId(),
                        neighbourCells[i].getJ(),
                        neighbourCells[i].getI()
                    )
                );
                removeFollowing(selected_units[i]);
            }*/
        }
        
        /**
         * Добавляет запрос движения юнита в заданную клетку.
         * @param unit юнит
         * @param dest клетка назначения
         */
        public function moveUnit(unit:Unit, dest:Point):void {
            movementRequests.push(new MoveRequestMessage(unit.getId(), dest.j, dest.i));
        }
        
        /**
         * Возвращает запросы движения и очищает массив запросов движения.
         */
        public function takeMovementRequests():Array {
            var tmp:Array = movementRequests;
            movementRequests = [];
            return tmp;
        }
    }
}
