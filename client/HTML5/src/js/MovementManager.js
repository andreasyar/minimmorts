
// Менеджер движений.
// require routines.js Movement.js

function MovementManager() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.movements = new Array();
    
    this.startMovement = function(step, unit, begJ, begI, endJ, endI) {
        for (var n in this.movements) {
            if (this.movements[n].getUnit().getId() == unit.getId()) {
                this.movements.splice(n, 1);
                break;
            }
        }
        this.movements.push(
            new Movement(
                unit,
                begJ,
                begI,
                endJ,
                endI,
                0.4,
                getPath(new Cell(begI, begJ, 0, 0, null), new Cell(endI, endJ, 0, 0, null)),
                step
            )
        );
    }
    
    this.updatePositions = function(step) {
        for (var i in this.movements) {
            this.movements[i].updatePosition(step, this.stopMovement);
        }
    }
    
    this.stopMovement = function(movement) {
        for (var i in this.movements) {
            if (this.movements[i] == movement) {
                this.movements.splice(i, 1);
            }
        }
    }
    
    this.delMovementByUnitId = function(id) {
        for (var i in this.movements) {
            if (this.movements[i].getUnit().getId() == id) {
                this.movements.splice(i, 1);
            }
        }
    }
    
    return this;
}
