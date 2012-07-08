
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

    /**
     * Проверяет, движется ли юнит.
     */
    this.isUnitMove = function(unit) {
        for (var i = 0; i < this.movements.length; i++) {
            if (this.movements[i].getUnit() == unit && !this.movements[i].isFinished()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Создаёт новое движение.
     */
    this.startNewMovement = function(begStep, unit, waypoints) {
        var path = generatePath(map, waypoints, unit);
        if (path == null) {
            console.log("Путь null. Движение не может быть создано.");
            return;
        }
        if (DEBUG_MOVEMENTS) {
            console.log(step + ": Создан путь юнита " + unit + " " + pathToString(path));
        }

        var movement = new Movement(unit, DEFAULT_SPEED, begStep, waypoints, path);

        this.movements.push(movement);
    }

    /**
     * Обновляет существующее движение.
     */
    this.updateMovement = function(unit, waypoints) {
        var existedMovement = null;
        for (var i = 0; i < this.movements.length; i++) {
            if (this.movements[i].getUnit() == unit) {
                existedMovement = this.movements[i];
                break;
            }
        }
        if (existedMovement == null) {
            console.log("Движение не найдено. Движение не может быть обновлено.");
            return;
        }
        
        var path = generatePath(map, waypoints, existedMovement.getUnit());
        if (path == null) {
            console.log("Путь null. Движение не может быть обновлено.");
            return;
        }
        if (DEBUG_MOVEMENTS) {
            console.log(step + ": Обновлён путь юнита " + unit + " " + pathToString(path));
        }

        existedMovement.setWaypoints(waypoints);
        existedMovement.setPath(path);
    }

    /**
     * Обновляет позиции юнитов.
     */
    this.updatePositions = function(step) {
        this.removeFinishedMovements(); // TODO В том чтобы удалять движения именно тут есть глубинный смысл, надо от него избавится.
        for (var i = 0; i < this.movements.length; i++) {
            if (!(this.movements[i] instanceof Movement)) {
                throw new Error("Движение не является объектом типа Movement: " + this.movements[i]);
            }
            this.movements[i].updatePosition(step);
        }
    }
        
    /**
     * Обновляет позиции спрайтов юнитов.
     */
    this.updateSpritePositions = function() {
        for (var i = 0; i < this.spriteMovements.length; i++) {
            if (!(this.spriteMovements[i] instanceof SpriteMovement)) {
                throw new Error("Движение спрайта не является объектом типа SpriteMovement: " + this.spriteMovements[i]);
                }
            this.spriteMovements[i].updatePosition();
            }
            }

    /**
     * Удаляет завершенные движения.
     */
    this.removeFinishedMovements = function() {
        var movementsInProgress = new Array();
        for (var i = 0; i < this.movements.length; i++) {
            if (!(this.movements[i] instanceof Movement)) {
                throw new Error("Движение не является объектом типа Movement.");
        }
            if (!this.movements[i].isFinished()) {
                movementsInProgress.push(this.movements[i]);
    }
        }
        this.movements = movementsInProgress;
    }

    /**
     * Удаляет завершенные движения спрайтов.
     */
    this.removeFinishedSpriteMovements = function() {
        var spriteMovementsInProgress = new Array();
        for (var i = 0; i < this.spriteMovements.length; i++) {
            if (!(this.spriteMovements[i] instanceof SpriteMovement)) {
                throw new Error("Движение спрайта не является объектом типа SpriteMovement.");
            }
            if (!this.spriteMovements[i].isFinished()) {
                spriteMovementsInProgress.push(this.spriteMovements[i]);
            }
            }
        this.spriteMovements = spriteMovementsInProgress;
        }

    this.delMovementByUnitId = function(id) {
        var i;

        for (i = 0; i < this.movements.length; i++) {
            if (this.movements[i].getUnit().getId() == id) {
                this.movements.splice(i, 1);
                if (DEBUG_MOVEMENTS || DEBUG_UNITS) {
                    console.log("Удалено движение юнита id=" + id);
                }
                break;
            }
        }
        for (i = 0; i < this.spriteMovements.length; i++) {
            if (this.spriteMovements[i].getUnit().getId() == id) {
                this.spriteMovements.splice(i, 1);
                if (DEBUG_MOVEMENTS || DEBUG_UNITS) {
                    console.log("Удалено движение спрайта юнита id=" + id);
                }
                break;
            }
        }
    }

    return this;
}

/**
 * Возвращает ориентацию движения между двумя точками или null, если
 * ориентацию не удалось вычислить.
 */
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
