
/**
 * Плавное движение спрайта юнита по клеткам.
 */
function SpriteMovement(unit, path, curUnitPos) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    if (!(unit instanceof Unit)) {
        console.log(unit, "не является юнитом.");
        return null;
    }
    
    /**
     * Юнит.
     */
    this.unit = unit;
    
    var path_array = new Array();
    var cur_path_point = path;
    do {
        path_array.push(new Point(cur_path_point.j, cur_path_point.i));
        cur_path_point = cur_path_point.parent;
    } while (cur_path_point != null)
    path_array.reverse();
    for (var l in path_array) {
        if (path_array[l].j == curUnitPos.j && path_array[l].i == curUnitPos.i) {
            break;
        }
    }
    var wayPointPath = new Array();
    l = parseInt(l);
    wayPointPath.push(new SpriteMovementWayPoint(path_array[l].j, path_array[l].i, (new Date()).getTime(), null, null));
    for (var k = l + 1; k < path_array.length - 1; k++) {
        wayPointPath.push(
            new SpriteMovementWayPoint(
                path_array[k].j,
                path_array[k].i,
                wayPointPath[wayPointPath.length - 1].arrivalTime + ((1 / 0.4) * 100),
                32 / ((1 / 0.4) * 100),
                movementManager.getMovementOrientation(
                    wayPointPath[wayPointPath.length - 1].j,
                    wayPointPath[wayPointPath.length - 1].i,
                    path_array[k].j,
                    path_array[k].i
                )
            )
        );
    }
    wayPointPath.push(
        new SpriteMovementWayPoint(
            path_array[path_array.length - 1].j,
            path_array[path_array.length - 1].i,
            wayPointPath[wayPointPath.length - 1].arrivalTime + ((1 / 0.4) * 100),
            32 / ((1 / 0.4) * 100),
            movementManager.getMovementOrientation(
                wayPointPath[wayPointPath.length - 1].j,
                wayPointPath[wayPointPath.length - 1].i,
                path_array[path_array.length - 1].j,
                path_array[path_array.length - 1].i
            )
        )
    );
    
    /**
     * Путь, по которому двигается спрайт юнита.
     */
    this.path = wayPointPath;
    
    /**
     * Обновляет координаты спрайта юнита.
     */
    this.updatePosition = function() {
        var curTime = (new Date()).getTime();
        
        for (var i = 0; i < this.path.length; i++) {
            if (this.path[i].arrivalTime > curTime) {
                if (i == 0) {
                    console.log("Попытка движения спрайта юнита по пути, с начальной точкой в будущем. Тут явно что-то не так.");
                    return;
                }
                
                var curPos = this.path[i].getPosOnTheWayFrom(this.path[i - 1]);
                if (curPos == null) {
                    console.log("Невозможно двигаться из точки " + this.path[i - 1] + " в точку " + this.path[i]);
                    return;
                }
                this.unit.spritePos = curPos;
            }
        }
    }
    
    /**
     * Возвращает юнита.
     */
    this.getUnit = function() {
        return this.unit;
    }
    
    return this;
}
