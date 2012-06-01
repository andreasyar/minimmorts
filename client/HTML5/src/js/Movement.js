
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
    var path_array = new Array();
    var cur_path_point = path;
    do {
        path_array.push({j: cur_path_point.j, i: cur_path_point.i});
        cur_path_point = cur_path_point.parent;
    } while (cur_path_point != null)
    path_array.reverse();
    this.path = path_array;
    console.log("Path for", unit.getId(), this.path);
    this.beg_step = beg_step;
    
    this.updatePosition = function(step, stopMovementCallback) {
        var n = Math.floor((step - this.beg_step) * this.speed);
        /*console.log(
            (step - this.beg_step),
            (step - this.beg_step) * this.speed,
            Math.floor((step - this.beg_step) * this.speed),
            n
        )*/
        if (n >= this.path.length - 1) {
            this.unit.j = this.path[this.path.length - 1].j
            this.unit.i = this.path[this.path.length - 1].i
            stopMovementCallback(this);
        } else {
            this.unit.j = this.path[n].j
            this.unit.i = this.path[n].i
        }
        console.log(
            "On step ",
            step,
            " is ",
            (step - this.beg_step),
            " steps left, so we are at ",
            n,
            " cell in the path."
        )
    }
    
    this.getUnit = function () {
        return this.unit;
    }
    
    return this;
}
