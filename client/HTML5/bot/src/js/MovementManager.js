
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

    this.startNewMovement = function(step, unit, waypoints) {
        var movement = new Movement(unit, DEFAULT_SPEED, step, waypoints);
        var path = this.regeneratePath(map, movement);
        if (path == null) {
            console.log(unit, DEFAULT_SPEED, step, waypoints);
            alert("Path is null");
            throw new Error("Path is null");
        }
        movement.setPath(path);

        this.movements.push(movement);
        /*this.spriteMovements.push(
            new SpriteMovement(
                unit,
                path,
                movement.getPosition(step),
                movement
            )
        );*/
    }

    this.updateMovement = function(unit, waypoints) {
        var existedMovement = null;
        var newPath;

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
        
        existedMovement.setWaypoints(waypoints);
        newPath = this.regeneratePath(map, existedMovement);
        //console.log("derp: " + pathToString(newPath));
        existedMovement.setPath(newPath);
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
            if (!(this.spriteMovements[i] instanceof SpriteMovement)) {
                console.log(this.spriteMovements[i], "is not sprite movement. Ignore.");
                continue;
            }
            if (!this.spriteMovements[i].finished) {
                this.spriteMovements[i].updatePosition2();
            }
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

    this.removeFinishedSpriteMovements = function() {
        var spriteMovementsInProgress = new Array();
        
        // TODO Движение спрайта каким-то образом может развершится позже движения самого юнита. Видимо не правильно вычисляется время движения.
        for (var j in this.spriteMovements) {
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
    }
    
    this.regeneratePath = function(map, existedMovement) {
        var waypoints = existedMovement.getWaypoints();
        var newPath = new Array();        
        var path;
        
        for (var i = 1; i < waypoints.length; i++) {
            path = getPath(
                new Cell(waypoints[i - 1].i, waypoints[i - 1].j, 0, 0, null),
                new Cell(waypoints[i].i, waypoints[i].j, 0, 0, null)
            );
            if (path == false) {
                console.log("Путь [" + waypoints[i - 1].j + ", " + waypoints[i - 1].i + "]->[" + waypoints[i].j + ", " + waypoints[i].i + "] не может быть построен. Движение не может быть обновлено.");
                return null;
            }
            path = convertLinkedPathToArray(path);
            if (newPath.length > 0) {
                path.splice(0, 1);
            }
            newPath = newPath.concat(path);
        }
        
        return newPath;
    }

    return this;
}

MovementManager.getMovementOrientation = function(begJ, begI, endJ, endI) {
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
