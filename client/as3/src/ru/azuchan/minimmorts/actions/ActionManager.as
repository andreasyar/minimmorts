package ru.azuchan.minimmorts.actions {
    import ru.azuchan.minimmorts.map.Cell;
    import ru.azuchan.minimmorts.messages.MoveRequestMessage;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.movement.Point;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.units.UnitManager;

	/**
     * Управляет и хранит запросы действий пользователя.
     * @author sorc
     */
    public class ActionManager {
        
        /**
         * Менеджер движений.
         */
        private var movementManager:MovementManager;
        
        /**
         * Менеджер юнитов.
         */
        private var unitManager:UnitManager;
        
        /**
         * Запросы движения.
         */
        private var movementRequests:Array;
        
        /**
         * Создаёт менеджер действий.
         * @param movementManager менеджер движений.
         * @param unitManager менеджер юнитов.
         */
        public function ActionManager(movementManager:MovementManager, unitManager:UnitManager) {
            this.movementManager = movementManager;
            this.unitManager = unitManager;
            movementRequests = [];
        }
        
        /**
         * Добавляет запросы движения выделенных юнитов в заданную клетку карты.
         * При этом все существующие запросы движения удаляются.
         * @param dest клетка назначения
         */
        public function moveSelectedUnits(dest:Cell):void {
            movementRequests = [];
            
            var selectedUnits:Array = unitManager.getSelectedUnits();            
            var neighbourCells:Array = [dest];
            
            while (neighbourCells.length < selectedUnits.length && neighbourCells.length < Main.map.getWidth() * Main.map.getHeight()) {
                neighbourCells = Map.getNeighboursCells(neighbourCells);
            }
            for (var i:int = 0; i < Math.min(neighbourCells.length, selectedUnits.length); i++) {
                if (selectedUnits[i].j == neighbourCells[i].j && selectedUnits[i].i == neighbourCells[i].i) {
                    continue;   // В себя не ходим.
                }
                movementRequests.push(new MoveRequestMessage(selectedUnits[i].id, neighbourCells[i].j, neighbourCells[i].i));
                movementManager.removeFollowing(selectedUnits[i]);
            }
        }
        
        /**
         * Добавляет запрос движения юнита в клетку карты.
         * @param unit юнит
         * @param dest клетка назначения
         */
        public function moveUnit(unit:Unit, dest:Cell):void {
            movementRequests.push(new MoveRequestMessage(unit.id, dest.j, dest.i));
        }
        
        /**
         * Возвращает запросы движения и очищает массив запросов движения.
         * @return запросы движения
         */
        public function takeMovementRequests():Array {
            var tmp:Array = movementRequests;
            movementRequests = [];
            return tmp;
        }
    }
}
