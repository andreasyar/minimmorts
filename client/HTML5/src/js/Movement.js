
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
    //console.log(this.path);
    
    /*this.smoothMovementPath = new Array();
    this.smoothMovementPath.push(
        new SmoothMovementWayPoint(
            this.path[0].j,
            this.path[0].i,
            (new Date()).getTime() - ((1 / 0.4) * 100),
            null,
            null,
            null,
            false
        )
    );
    for (var k = 1; k < this.path.length - 1; k++) {
        this.smoothMovementPath.push(
            new SmoothMovementWayPoint(
                this.path[k].j,
                this.path[k].i,
                ((1 / 0.4) * 100) + this.smoothMovementPath[this.smoothMovementPath.length - 1].arrivalTime,
                this.smoothMovementPath[this.smoothMovementPath.length - 1].arrivalTime,
                32 / ((1 / 0.4) * 100),
                movementManager.getMovementOrientation(
                    this.smoothMovementPath[this.smoothMovementPath.length - 1].j,
                    this.smoothMovementPath[this.smoothMovementPath.length - 1].i,
                    this.path[k].j,
                    this.path[k].i
                ),
                false
            )
        );
    }
    this.smoothMovementPath.push(
        new SmoothMovementWayPoint(
            this.path[this.path.length - 1].j,
            this.path[this.path.length - 1].i,
            ((1 / 0.4) * 100) + this.smoothMovementPath[this.smoothMovementPath.length - 1].arrivalTime,
            this.smoothMovementPath[this.smoothMovementPath.length - 1].arrivalTime,
            32 / ((1 / 0.4) * 100),
            movementManager.getMovementOrientation(
                this.smoothMovementPath[this.smoothMovementPath.length - 1].j,
                this.smoothMovementPath[this.smoothMovementPath.length - 1].i,
                this.path[this.path.length - 1].j,
                this.path[this.path.length - 1].i
            ),
            true
        )
    );*/
    //console.log(this.smoothMovementPath);
    //this.unit.smoothMovementWay = this.smoothMovementPath;
    
    this.beg_step = beg_step;
    
    this.updatePosition = function(step) {
        var n = Math.floor((step - this.beg_step) * this.speed);
        if (n >= this.path.length - 1) {
            this.unit.j = this.path[this.path.length - 1].j;
            this.unit.i = this.path[this.path.length - 1].i;
            //console.log("Unit", unit.getId(), "comes to [", this.unit.j, ",", this.unit.i, "] on step " + step + " Final");
            //console.log(PrintSmoothMovementWay(this.unit.smoothMovementWay));
            this.stopped = true;
        } else {
            this.unit.j = this.path[n].j;
            this.unit.i = this.path[n].i;
            //console.log("Unit", unit.getId(), "comes to [", this.unit.j, ",", this.unit.i, "] on step " + step);
        }
        /*if (this.unit.smoothMovementWay.length > 0) {
            if (this.unit.j != this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].j
                    || this.unit.i != this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].i) {
                
                var curTime = (new Date()).getTime();
                console.log(
                    curTime,
                    this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].arrivalTime,
                    curTime - this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].arrivalTime
                );
                this.unit.smoothMovementWay.push(
                    new SmoothMovementWayPoint(
                        this.unit.j,
                        this.unit.i,
                        ((1 / 0.4) * 100) + this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].arrivalTime, //curTime,
                        this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].arrivalTime,
                        32 / ((1 / 0.4) * 100), //32 / (curTime - this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].arrivalTime),
                        movementManager.getMovementOrientation(
                            this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].j,
                            this.unit.smoothMovementWay[this.unit.smoothMovementWay.length - 1].i,
                            this.unit.j,
                            this.unit.i
                        ),
                        n >= this.path.length - 1
                    )
                );
            }
        } else {
            this.unit.smoothMovementWay.push(
                new SmoothMovementWayPoint(
                    this.unit.j,
                    this.unit.i,
                    (new Date()).getTime(),
                    null,
                    null,
                    null,
                    n >= this.path.length - 1
                )
            );
        }*/
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
