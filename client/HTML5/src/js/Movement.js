
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

    this.path = path_array;
    //console.log("Path for", unit.getId(), pathToString(this.path));
    this.beg_step = beg_step;
    
    this.updatePosition = function(step, stopMovementCallback) {
        var n = Math.floor((step - this.beg_step) * this.speed);
        if (n >= this.path.length - 1) {
            this.unit.j = this.path[this.path.length - 1].j
            this.unit.i = this.path[this.path.length - 1].i
            //console.log("Unit", unit.getId(), "comes to [", this.unit.j, ",", this.unit.i, "] on step " + step + " Final");
            //movementManager.delMovementByUnitId(this.unit.getId());
            this.stopped = true;
        } else {
            this.unit.j = this.path[n].j
            this.unit.i = this.path[n].i
            //console.log("Unit", unit.getId(), "comes to [", this.unit.j, ",", this.unit.i, "] on step " + step);
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
