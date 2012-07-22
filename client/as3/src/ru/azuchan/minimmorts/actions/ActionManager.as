package ru.azuchan.minimmorts.actions {
    import ru.azuchan.minimmorts.follow.FollowManager;
    import ru.azuchan.minimmorts.map.Cell;
    import ru.azuchan.minimmorts.messages.MoveRequestMessage;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.units.UnitManager;
    import ru.azuchan.minimmorts.map.Map;

	/**
     * Управляет и хранит запросы действий пользователя.
     * @author sorc
     */
    public class ActionManager {
        
        /**
         * Менеджер юнитов.
         */
        private var unitManager:UnitManager;
        
        /**
         * Менеджер преследований.
         */
        private var followManager:FollowManager;
        
        /**
         * Карта игрового мира.
         */
        private var map:Map;
        
        /**
         * Запросы движения.
         */
        private var movementRequests:Array;
        
        /**
         * Создаёт менеджер действий.
         * @param movementManager менеджер движений.
         * @param unitManager менеджер юнитов.
         */
        public function ActionManager(unitManager:UnitManager, followManager:FollowManager, map:Map) {
            this.unitManager = unitManager;
            this.followManager = followManager;
            this.map = map;
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
            
            while (neighbourCells.length < selectedUnits.length && neighbourCells.length < map.width * map.height) {
                neighbourCells = map.getNeighboursCells(neighbourCells);
            }
            for (var i:int = 0; i < Math.min(neighbourCells.length, selectedUnits.length); i++) {
                if (neighbourCells[i].equals2(selectedUnits[i].j, selectedUnits[i].i)) {
                    continue;   // В себя не ходим.
                }
                movementRequests.push(new MoveRequestMessage(selectedUnits[i].id, neighbourCells[i]));
                followManager.removeFollowing(selectedUnits[i]);
            }
        }
        
        /**
         * Добавляет запрос движения юнита в клетку карты.
         * @param unit юнит
         * @param dest клетка назначения
         */
        public function moveUnit(unit:Unit, dest:Cell):void {
            movementRequests.push(new MoveRequestMessage(unit.id, dest));
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
