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
     * Возывращает запросы движения и очищает массив запросов движения.
     */
    this.takeMovementRequests = function() {
        var tmp = this.actions.MOVEMENT;
        this.actions.MOVEMENT = new Array();
        return tmp;
    }
}
