/**
 * Движение юнита.
 */
function Movement (unit, speed, beg_step, waypoints, path) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Юнит.
     */
    this.unit = unit;

    /**
     * Скорость движения в клетках в шаг.
     */
    this.speed = speed;

    /**
     * Путь движения.
     */
    this.path = path;

    /**
     * Номер хода, когда движение началось.
     */
    this.beg_step = beg_step;

    /**
     * Точки, по которым строится путь движения.
     */
    this.waypoints = waypoints;

    /**
     * Флаг окончания движения.
     */
    this.finished = false;

    this.getBegJ = function() {
        return this.waypoints[0].j;
    }

    this.getBegI = function() {
        return this.waypoints[0].i;
    }

    this.getEndJ = function() {
        return this.waypoints[this.waypoints.length - 1].j;
    }

    this.getEndI = function() {
        return this.waypoints[this.waypoints.length - 1].i;
    }

    this.setPath = function(path) {
        this.path = path;
    }

    this.setWaypoints = function(waypoints) {
        this.waypoints = waypoints;
    }

    this.getWaypoints = function() {
        return this.waypoints;
    }

    this.updatePosition = function(step) {
        var n = Math.floor((step - this.beg_step) * this.speed);
        if (this.path == null) {
            console.log("Положение юнита id=" + this.unit.getId() + " не может быть обновлено. Путь - null");
            return;
        }
        if (n >= this.path.length - 1) {
            if (DEBUG_MOVEMENTS) {
                console.log(step + ": Юнит id=" + unit.getId() + " сходил [" + this.unit.j + "," + this.unit.i + "]->[" + this.path[this.path.length - 1].j + "," + this.path[this.path.length - 1].i + "] и движение звершилось.");
            }
            this.unit.j = this.path[this.path.length - 1].j;
            this.unit.i = this.path[this.path.length - 1].i;
            this.finished = true;
            //movementManager.removeFinishedMovements();
        } else {
            if (DEBUG_MOVEMENTS) {
                console.log(step + ": Юнит id=" + unit.getId() + " сходил [" + this.unit.j + "," + this.unit.i + "]->[" + this.path[n].j + "," + this.path[n].i + "]");
            }
            this.unit.j = this.path[n].j;
            this.unit.i = this.path[n].i;
        }
    }

    this.getUnit = function() {
        return this.unit;
    }

    this.getBegStep = function() {
        return this.beg_step;
    }

    this.isFinished = function() {
        return this.finished;
    }

    this.toString = function() {
        return "{begJ=" + this.getBegJ() +
               ",begI=" +this.getBegI() +
               ",endJ=" +this.getEndJ() +
               ",endI=" +this.getEndI() +
               ",finished=" +this.isFinished() +
               ",beg_step=" +this.getBegStep() +
               "}";
    }

    return this;
}
