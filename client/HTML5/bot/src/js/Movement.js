
// Движение.

function Movement (unit, begJ, begI, endJ, endI, speed, path, beg_step) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.unit = unit;
    this.begJ = begJ;
    this.begI = begI;
    this.endJ = endJ;
    this.endI = endI;
    this.speed = speed;
    this.stopped = false;
    
    var path_array = new Array();
    var cur_path_point = path;
    do {
        path_array.push(new Point(cur_path_point.j, cur_path_point.i));
        cur_path_point = cur_path_point.parent;
    } while (cur_path_point != null)
    path_array.reverse();
    //console.log(this.unit.getId(), step, path_array);

    this.path = path_array;    
    this.beg_step = beg_step;
    
    this.updatePosition = function(step) {
        var n = Math.floor((step - this.beg_step) * this.speed);
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
        return "{begJ=" + this.begJ +
               ",begI=" +this.begI +
               ",endJ=" +this.endJ +
               ",endI=" +this.endI +
               ",stopped=" +this.stopped +
               ",beg_step=" +this.beg_step +
               "}";
    }
    
    this.update = function(step, path, endJ, endI) {
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
    }
    
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
