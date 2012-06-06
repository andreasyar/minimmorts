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
     * Добавляет запрос движения. Не проверялось, работает или нет!
     */
    this.createMovementRequest = function(clickX, clickY, camera) {
        var tAxis = camera.reTranslateAxis(clickX, clickY);
        var neighbourCells = new Array({j: Map.getCellJOnMap(tAxis.x), i: Map.getCellIOnMap(tAxis.y)});
        while (neighbourCells.length < selected_units.length && neighbourCells.length < map.w * map.h) {
            neighbourCells = getNeighboursCells(neighbourCells);
        }
        for (var i = 0; i < Math.min(neighbourCells.length, selected_units.length); i++) {
            if (selected_units[i].getJ() == neighbourCells[i].j && selected_units[i].getI() == neighbourCells[i].i) {
                continue;
            }
            this.actions.MOVEMENT.push(
                new MoveMessage(
                    step,
                    selected_units[i].getId(),
                    -1,
                    -1,
                    neighbourCells[i].j,
                    neighbourCells[i].i
                )
            );
        }
    }
    
    /**
     * Добавляет запросы движения от всех своих юнитов в случайные клетки.
     */
    this.createRandomMovementRequests = function() {
        this.actions.MOVEMENT = new Array();
        for (var i = 0; i < self_units_list.length; i++) {
            this.actions.MOVEMENT.push(
                new MoveMessage(
                    step,
                    self_units_list[i].getId(),
                    -1,
                    -1,
                    getRandomInt(map.w - 1),
                    getRandomInt(map.h - 1)
                )
            );
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
