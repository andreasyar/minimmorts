/**
 * Управляет и хранит запросы действий пользователя.
 */
function ActionManager() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    /**
     * Запросы действий пользователем.
     */
    this.actions = {
        MOVEMENT: new Array()
    };
    
    /**
     * Добавляет запросы движения от всех своих юнитов в случайные клетки.
     */
    this.createRandomMovementRequests = function() {
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
     * Добавляет запросы движения выделенных юнитов в заданную точку (клетку).
     * @param dest - точка назначения
     */
    this.moveSelectedUnits = function(dest) {
        if (!(dest instanceof Point)) {
            throw new Error("Точка назначения не является объектом типа Point.");
        }

        this.actions.MOVEMENT = new Array();
        
        var neighbourCells = new Array(dest);
        while (neighbourCells.length < selected_units.length && neighbourCells.length < map.w * map.h) {
            neighbourCells = getNeighboursCells(neighbourCells);
        }
        for (var i = 0; i < Math.min(neighbourCells.length, selected_units.length); i++) {
            if (selected_units[i].getJ() == neighbourCells[i].getJ() && selected_units[i].getI() == neighbourCells[i].getI()) {
                continue;
            }
            this.actions.MOVEMENT.push(new MoveRequestMessage(selected_units[i].getId(), neighbourCells[i].getJ(), neighbourCells[i].getI()));
        }
    }

    /**
     * Возывращает запросы движения и очищает массив запросов движения.
     */
    this.takeMovementRequests = function() {
        var tmp = this.actions.MOVEMENT;
        this.actions.MOVEMENT = new Array();
        return tmp;
    }
}
