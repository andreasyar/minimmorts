/**
 * Управляет и хранит запросы действий пользователя.
 */
function ActionManager() {

    /*
     * Эту функцию можно вызвать только как конструктор (с new).
     */
    if (this.constructor !== arguments.callee) {
        throw new Error("Конструктор не может быть вызван как обычная функция.");
    }

    /**
     * Запросы действий пользователя.
     */
    this.actions = {
        MOVEMENT: new Array()
    };

    /**
     * Добавляет запросы движения от всех своих юнитов в случайные клетки. При
     * этом все существующие запросы движения удаляются.
     */
    this.randomMoveUnits = function() {
        this.actions.MOVEMENT = new Array();
        
        for (var i = 0; i < self_units_list.length; i++) {
            this.actions.MOVEMENT.push(
                new MoveRequestMessage(
                    self_units_list[i].getId(),
                    getRandomInt(map.w - 1),
                    getRandomInt(map.h - 1)
                )
            );
        }
    }

    /**
     * Добавляет запросы движения выделенных юнитов в заданную клетку.
     * При этом все существующие запросы движения удаляются.
     * @param dest - клетка назначения
     */
    this.moveSelectedUnits = function(dest) {
        if (!(dest instanceof Point)) {
            throw new Error("Клетка назначения не является объектом типа Point.");
        }

        this.actions.MOVEMENT = new Array();
        
        var neighbourCells = [dest];
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
        }
    }
    
    /**
     * Добавляет запрос движения юнита в заданную клетку.
     * @param unit - юнит
     * @param dest - клетка назначения
     */
    this.moveUnit = function(unit, dest) {
        if (!(dest instanceof Point)) {
            throw new Error("Клетка назначения не является объектом типа Point.");
        }
        if (!(unit instanceof Unit)) {
            throw new Error("Юнит не является объектом типа Unit.");
        }

        this.actions.MOVEMENT.push(new MoveRequestMessage(unit.getId(), dest.getJ(), dest.getI()));
    }

    /**
     * Возвращает запросы движения и очищает массив запросов движения.
     */
    this.takeMovementRequests = function() {
        var tmp = this.actions.MOVEMENT;
        this.actions.MOVEMENT = new Array();
        return tmp;
    }
}
