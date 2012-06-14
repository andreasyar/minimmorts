
/**
 * Менеджер движений. Хранит и управляет движением.
 * Для работы требует подключения файлов: routines.js, Movement.js, SpriteMovement, constants.js
 */
function MovementManager() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Движения юнитов.
     */
    this.movements = new Array();

    /**
     * Движения спрайтов юнитов.
     */
    this.spriteMovements = new Array();

    this.isUnitMove = function(unit) {
        for (var i in this.movements) {
            if (this.movements[i].getUnit() == unit) {
                return true;
            }
        }

        return false;
    }

    this.startNewMovement = function(step, unit, begJ, begI, endJ, endI) {
        var path = getPath(new Cell(begI, begJ, 0, 0, null), new Cell(endI, endJ, 0, 0, null));
        if (path == false) {
            console.log("Путь [" + begJ + ", " + begI + "]->[" + endJ + ", " + endI + "] не может быть построен. Новое движение не может быть создано.");
            return;
        }
        var movement = new Movement(unit, begJ, begI, endJ, endI, DEFAULT_SPEED, path, step);

        this.movements.push(movement);
        this.spriteMovements.push(
            new SpriteMovement(
                unit,
                path,
                movement.getPosition(step)
            )
        );
    }

    this.updateMovement = function(step, unit, endJ, endI) {
        var existedMovement = null;
        var existedSpriteMovement = null;
        var nextPoint;
        var updatedMovement;
        var updatedSpriteMovement;
        var path;

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
        for (var j in this.spriteMovements) {
            if (this.spriteMovements[j].getUnit() == unit) {
                existedSpriteMovement = this.spriteMovements[j];
                break;
            }
        }
        if (existedSpriteMovement == null) {
            console.log("Sprite movement not found.");
            return;
        }
        if ( (nextPoint = existedMovement.getNextPathPoint(unit.getJ(), unit.getI())) == null) {
            console.log("Next path point not found.");
            return;
        }
        //console.log("Next point is", nextPoint);
        path = getPath(new Cell(nextPoint.i, nextPoint.j, 0, 0, null), new Cell(endI, endJ, 0, 0, null));
        if (path == false) {
            console.log("Путь [" + nextPoint.j + ", " + nextPoint.i + "]->[" + endJ + ", " + endI + "] не может быть построен. Движение не может быть обновлено.");
            return;
        }
        updatedMovement = new Movement(
            existedMovement.getUnit(),
            nextPoint.j,
            nextPoint.i,
            endJ,
            endI,
            DEFAULT_SPEED,
            path,
            step
        );
        if (updatedMovement == null) {
            console.log("Cant create movement!");
            return;
        }
        updatedSpriteMovement = new SpriteMovement(
            existedSpriteMovement.getUnit(),
            path,
            updatedMovement.getPosition(step)
        );
        if (updatedSpriteMovement == null) {
            console.log("Cant create sprite movement!");
            return;
        }

        this.movements.splice(i, 1);
        this.movements.push(updatedMovement);
        this.spriteMovements.splice(j, 1);
        this.spriteMovements.push(updatedSpriteMovement);
    }

    this.updatePositions = function(step) {
        var movementsInProgress = new Array();
        var spriteMovementsInProgress = new Array();
        var i, j;

        for (i in this.movements) {
            if (!this.movements[i].isStopped()) {
                movementsInProgress.push(this.movements[i]);
            } else {
                //console.log("Movement of unit", this.movements[i].getUnit().getId(), "removed on step", step);
            }
        }
        this.movements = movementsInProgress;
        
        // TODO Движение спрайта каким-то образом может развершится позже движения самого юнита. Видимо не правильно вычисляется время движения.
        for (j in this.spriteMovements) {
            if (!(this.spriteMovements[j] instanceof SpriteMovement)) {
                if (DEBUG_MOVEMENTS) {
                    console.log(step + ": " + this.spriteMovements[j] + " не является движением спрайта юнита и будет проигнорировано.");
                }
                continue;
            }
            if (!this.spriteMovements[j].finished) {
                spriteMovementsInProgress.push(this.spriteMovements[j]);
                continue;
            } else if (DEBUG_MOVEMENTS) {
                console.log(step + ": Движение спрайта юнита id=" + this.spriteMovements[j].getUnit().getId() + " удалено.");
            }
        }
        this.spriteMovements = spriteMovementsInProgress;

        for (i in this.movements) {
            this.movements[i].updatePosition(step);
        }
    }

    this.updateSpritePositions = function() {
        for (var i in this.spriteMovements) {
            this.spriteMovements[i].updatePosition();
        }
    }

    this.delMovementByUnitId = function(id) {
        var i;

        for (i in this.movements) {
            if (this.movements[i].getUnit().getId() == id) {
                this.movements.splice(i, 1);
                if (DEBUG_MOVEMENTS || DEBUG_UNITS) {
                    console.log("Удалено движение юнита id=" + id);
                }
                break;
            }
        }
        for (i in this.spriteMovements) {
            if (this.spriteMovements[i].getUnit().getId() == id) {
                this.spriteMovements.splice(i, 1);
                if (DEBUG_MOVEMENTS || DEBUG_UNITS) {
                    console.log("Удалено движение спрайта юнита id=" + id);
                }
                break;
            }
        }
    }

    this.getMovementOrientation = function(begJ, begI, endJ, endI) {
        var jDiff = endJ - begJ;
        var iDiff = endI - begI;

        if (jDiff == 0 && iDiff == -1) {
            return "N";
        } else if (jDiff == 1 && iDiff == -1) {
            return "NE";
        } else if (jDiff == 1 && iDiff == 0) {
            return "E";
        } else if (jDiff == 1 && iDiff == 1) {
            return "SE";
        } else if (jDiff == 0 && iDiff == 1) {
            return "S";
        } else if (jDiff == -1 && iDiff == 1) {
            return "SW";
        } else if (jDiff == -1 && iDiff == 0) {
            return "W";
        } else if (jDiff == -1 && iDiff == -1) {
            return "NW";
        } else {
            return null;
        }
    }

    return this;
}
