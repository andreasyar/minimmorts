
// Менеджер движений.
// require routines.js Movement.js

function MovementManager() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.movements = new Array();
    
    this.isUnitMove = function(unit) {
        for (var i in this.movements) {
            if (this.movements[i].getUnit() == unit) {
                return true;
            }
        }
        
        return false;
    }
    
    this.startNewMovement = function(step, unit, begJ, begI, endJ, endI) {
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
    
    this.updateMovement = function(step, unit, endJ, endI) {
        var existedMovement = null;
        var nextPoint;
        var updatedMovement;
        
        for (var i in this.movements) {
            if (this.movements[i].getUnit() == unit) {
                existedMovement = this.movements[i];
                break;
            }
        }
        if (existedMovement == null) {
            console.log("Movement not found.");
            return;
        }
        if ( (nextPoint = existedMovement.getNextPathPoint(unit.getJ(), unit.getI())) == null) {
            console.log("Next path point not found.");
            return;
        }
        updatedMovement = new Movement(
            existedMovement.getUnit(),
            nextPoint.j,
            nextPoint.i,
            endJ,
            endI,
            0.4,
            getPath(new Cell(nextPoint.i, nextPoint.j, 0, 0, null), new Cell(endI, endJ, 0, 0, null)),
            step
        );
        if (updatedMovement == null) {
            console.log("Cant create movement!");
            return;
        }
        
        this.movements[i] = updatedMovement;
    }
    
    this.updatePositions = function(step) {
        var fuckJS = new Array();
        for (var i in this.movements) {
            if (!this.movements[i].isStopped()) {
                fuckJS.push(this.movements[i]);
            } else {
                console.log("Movement of unit", this.movements[i].getUnit().getId(), "removed on step", step);
            }
        }
        this.movements = fuckJS;
        for (var i in this.movements) {
            this.movements[i].updatePosition(step, this.stopMovement);
        }
    }
    
    this.delMovementByUnitId = function(id) {
        for (var i in this.movements) {
            if (this.movements[i].getUnit().getId() == id) {
                console.log("Remove movement", this.movements[i]);
                this.movements.splice(i, 1);
                console.log("Movements", this.movements);
            }
        }
    }
    
    return this;
}
