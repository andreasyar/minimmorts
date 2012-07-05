
// Движение.

function Movement (unit, speed, beg_step, waypoints) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Ссылка на движущегося юнита.
     */
    this.unit = unit;

    /**
     * Скорость движения в клетках в шаг.
     */
    this.speed = speed;

    /**
     * TODO нужно подумать где и как правильней создавать путь.
     */
    this.path = null;

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
    this.stopped = false;
    
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
            this.stopped = true;
        } else {
            if (DEBUG_MOVEMENTS) {
                console.log(step + ": Юнит id=" + unit.getId() + " сходил [" + this.unit.j + "," + this.unit.i + "]->[" + this.path[n].j + "," + this.path[n].i + "]");
            }
            this.unit.j = this.path[n].j;
            this.unit.i = this.path[n].i;
        }
    }

    this.getPosition = function(step) {
        var n = Math.floor((step - this.beg_step) * this.speed);
        if (n >= this.path.length - 1) {
            return this.path[this.path.length - 1];
        } else {
            return this.path[n];
        }
    }

    this.getUnit = function() {
        return this.unit;
    }

    this.getBegStep = function() {
        return this.beg_step;
    }

    this.getNextPathPoint = function(j, i) {
        for (var k = 0; k < this.path.length; k++) {
            if (this.path[k].j == j && this.path[k].i == i) {
                if (this.path[k + 1] != undefined) {
                    return this.path[k + 1];
                } else {
                    return this.path[k];
                }
            }
        }

        return null;
    }

    this.isStopped = function() {
        return this.stopped;
    }

    this.getNextPathPointOrientation = function(j, i) {
        var nextPathPoint = null;

        for (var k = 0; k < this.path.length; k++) {
            if (this.path[k].j == j && this.path[k].i == i) {
                if (this.path[k + 1] != undefined) {
                    nextPathPoint = this.path[k + 1];
                }
            }
        }

        if (nextPathPoint == null) {
            return null;
        }

        var jDiff = nextPathPoint.j - j;
        var iDiff = nextPathPoint.i - i;

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
        } else {
            return "NW";
        }
    }

    this.toString = function() {
        return "{begJ=" + this.getBegJ() +
               ",begI=" +this.getBegI() +
               ",endJ=" +this.getEndJ() +
               ",endI=" +this.getEndI() +
               ",stopped=" +this.isStopped() +
               ",beg_step=" +this.getBegStep() +
               "}";
    }

    /*this.update = function(step, path, endJ, endI) {
        var newPath = new Array();
        var pathArray = convertLinkedPathToArray(path);
        var n = Math.floor((step - this.beg_step) * this.speed);

        var i;
        for (i = 0; i <= n && i < this.path.length; i++) {
            newPath.push(this.path[i]);
        }
        for (i = 0; i < pathArray.length; i++) {
            newPath.push(pathArray[i]);
        }

        this.endJ = endJ;
        this.endI = endI;
        this.path = newPath;

        if (DEBUG_MOVEMENTS) {
            console.log(n + " Обновлен путь движения: " + pathToString(this.path));
        }
    }*/

    return this;
}

function Point(j, i) {
    this.j = j;
    this.i = i;
    this.toString = function () {
        return "[" + this.j + "," + this.i + "]";
    }
    return this;
}
