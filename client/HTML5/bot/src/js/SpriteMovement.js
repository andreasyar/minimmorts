
/**
 * Плавное движение спрайта юнита по клеткам.
 */
function SpriteMovement(unit, path, curUnitPos, movement) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    if (!(unit instanceof Unit)) {
        console.log(unit, "не является юнитом.");
        return null;
    }
    
    console.log("Создано новое движение спрайта")
    
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
                wayPointPath[wayPointPath.length - 1].arrivalTime + ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
                32 / ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
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
            wayPointPath[wayPointPath.length - 1].arrivalTime + ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
            32 / ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
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
    console.log("original", (new Date()).getTime(), pathToString(this.path));
    
    this.begTime = wayPointPath[0].arrivalTime;
    
    this.endTime = wayPointPath[wayPointPath.length - 1].arrivalTime;
    
    this.spriteInterval = 150;
    
    this.finished = false;
    
    this.movement = movement;
    
    /**
     * Обновляет координаты спрайта юнита.
     */
    this.updatePosition = function() {
        var curTime = (new Date()).getTime();
        var curPos;
        var derp;
        
        for (var i = 0; i < this.path.length; i++) {
            if (this.path[i].arrivalTime > curTime) {
                if (i == 0) {
                    console.log("Попытка движения спрайта юнита по пути, с начальной точкой в будущем. Тут явно что-то не так.");
                    return;
                }
                
                curPos = this.path[i].getPosOnTheWayFrom(this.path[i - 1]);
                if (curPos == null) {
                    console.log("Невозможно двигаться из точки " + this.path[i - 1] + " в точку " + this.path[i]);
                    return;
                }
                this.finished = curPos.arrived;
                if (!this.finished) {
                    derp = Math.ceil((curTime - this.begTime) / this.spriteInterval);
                    curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + (derp % 5);
                } else {
                    curPos.spriteName = "peasant_" + curPos.orientation + "_walk_0";
                    movementManager.removeFinishedSpriteMovements();
                }
                this.unit.spritePos = curPos;
                return;
            }
        }
        
        if (!this.finished) {
            curPos = this.path[this.path.length - 1].getPosOnTheWayFrom(this.path[this.path.length - 2]);
            if (curPos == null) {
                console.log("Невозможно двигаться из точки " + this.path[this.path.length - 2] + " в точку " + this.path[this.path.length - 1]);
                return;
            }
            if (!curPos.arrived) {
                derp = Math.ceil((curTime - this.begTime) / this.spriteInterval);
                curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + (derp % 5);
            } else {
                curPos.spriteName = "peasant_" + curPos.orientation + "_walk_0";
            }
            this.unit.spritePos = curPos;
        }
    }
    
    /**
     * Возвращает юнита.
     */
    this.getUnit = function() {
        return this.unit;
    }
    
    this.update = function(path, curUnitPos) {
        
        /*
         * Найдём точку пути, в которой находится спрайт игрока.
         */
        var curTime = (new Date()).getTime();
        
        var newPath = new Array();
        
        var found = false;
        
        for (var i = 0; i < this.path.length; i++) {
            if (this.path[i].arrivalTime > curTime) {
                newPath.push(this.path[i]);
                break;
            }
        }
        if (!found) {
            newPath.push(this.path[this.path.length - 1]);
        }
        
        console.log("pre splice", (new Date()).getTime(), pathToString(this.path));
        console.log("post splice", pathToString(newPath));
    
        var path_array = convertLinkedPathToArray(path);

        l = 0;
        if (newPath.lenght > 0 && newPath[newPath.length - 1].j == path_array[0].j && newPath[newPath.length - 1].i == path_array[0].i) {
            l = 1;
        }
        for (var k = l; k < path_array.length - 1; k++) {
            newPath.push(
                new SpriteMovementWayPoint(
                    path_array[k].j,
                    path_array[k].i,
                    newPath[newPath.length - 1].arrivalTime + ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
                    32 / ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
                    movementManager.getMovementOrientation(
                        newPath[newPath.length - 1].j,
                        newPath[newPath.length - 1].i,
                        path_array[k].j,
                        path_array[k].i
                    )
                )
            );
        }
        newPath.push(
            new SpriteMovementWayPoint(
                path_array[path_array.length - 1].j,
                path_array[path_array.length - 1].i,
                newPath[newPath.length - 1].arrivalTime + ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
                32 / ((1 / DEFAULT_SPEED) * (prevStepTime == null ? 100 : prevStepTime)),
                movementManager.getMovementOrientation(
                    newPath[newPath.length - 1].j,
                    newPath[newPath.length - 1].i,
                    path_array[path_array.length - 1].j,
                    path_array[path_array.length - 1].i
                )
            )
        );
            
        this.path = newPath;
        console.log("updated", pathToString(this.path));
        
    return this;
}
    
    this.updatePosition2 = function() {
        
        if (stepBegTime == null || prevStepTime == null) {
            console.log("Невозможно обновить координаты спрайта юнита, когда время начала текущего хода и время предыдущего хода - null");
            return false;
        }
        
        // Стоим на месте.
        if (step == this.movement.beg_step) {
            return true;
        }
        
        var prevStepBegTime = stepBegTime - prevStepTime;
        var curTime = (new Date()).getTime() - prevStepTime;
        var diffTime = curTime - prevStepBegTime;
        
        var m = ((step - 1) - this.movement.beg_step) * this.movement.speed;
        var prevPos = null;
        var n = Math.floor(m);
        if (n > this.movement.path.length - 1) {
            n = this.movement.path.length - 1;
        }
        prevPos = this.movement.path[n];
        
        var movementOrientation = null;
        if (n == this.movement.path.length - 1) {
            movementOrientation = MovementManager.getMovementOrientation(
                this.movement.path[n - 1].j,
                this.movement.path[n - 1].i,
                this.movement.path[n].j,
                this.movement.path[n].i
            );
        } else {    // n < this.movement.path.length - 1
            movementOrientation = MovementManager.getMovementOrientation(
                this.movement.path[n].j,
                this.movement.path[n].i,
                this.movement.path[n + 1].j,
                this.movement.path[n + 1].i
            );
        }
        //console.log(prevPos + "," + movementOrientation);
        
        var prevPosX = Map.getCellXOnMap(prevPos.j);
        var prevPosY = Map.getCellYOnMap(prevPos.i);
        
        var v = (this.movement.speed * CELL_W) / prevStepTime;
        
        var curPos = null;
        var endX = Map.getCellXOnMap(this.movement.path[this.movement.path.length - 1].j);
        var endY = Map.getCellYOnMap(this.movement.path[this.movement.path.length - 1].i);
        switch (movementOrientation) {
            case "N":
                prevPosY -= (m - n) * CELL_H;
                prevPosY -= v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                if (prevPosY <= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "NE":
                prevPosX += (m - n) * CELL_W;
                prevPosX += v * diffTime;
                prevPosX = Math.floor(prevPosX);
                prevPosY -= (m - n) * CELL_H;
                prevPosY -= v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                /*if (prevPosX >= endX) {
                    prevPosX = endX;
                }
                if (prevPosY <= endY) {
                    prevPosY = endY;
                }*/
                if (prevPosX >= endX && prevPosY <= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "E":
                prevPosX += (m - n) * CELL_W;
                prevPosX += v * diffTime;
                prevPosX = Math.floor(prevPosX);
                
                if (prevPosX >= endX && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "SE":
                prevPosX += (m - n) * CELL_W;
                prevPosX += v * diffTime;
                prevPosX = Math.floor(prevPosX);
                prevPosY += (m - n) * CELL_H;
                prevPosY += v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                /*if (prevPosX >= endX) {
                    prevPosX = endX;
                }
                if (prevPosY >= endY) {
                    prevPosY = endY;
                }*/
                if (prevPosX >= endX && prevPosY >= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "S":
                prevPosY += (m - n) * CELL_H;
                prevPosY += v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                if (prevPosY >= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "SW":
                prevPosX -= (m - n) * CELL_W;
                prevPosX -= v * diffTime;
                prevPosX = Math.floor(prevPosX);
                prevPosY += (m - n) * CELL_H;
                prevPosY += v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                /*if (prevPosX <= endX) {
                    prevPosX = endX;
                }
                if (prevPosY >= endY) {
                    prevPosY = endY;
                }*/
                if (prevPosX <= endX && prevPosY >= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "W":
                prevPosX -= (m - n) * CELL_W;
                prevPosX -= v * diffTime;
                prevPosX = Math.floor(prevPosX);
                
                if (prevPosX <= endX && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            case "NW":
                prevPosX -= (m - n) * CELL_W;
                prevPosX -= v * diffTime;
                prevPosX = Math.floor(prevPosX);
                prevPosY -= (m - n) * CELL_H;
                prevPosY -= v * diffTime;
                prevPosY = Math.floor(prevPosY);
                
                /*if (prevPosX <= endX) {
                    prevPosX = endX;
                }
                if (prevPosY <= endY) {
                    prevPosY = endY;
                }*/
                if (prevPosX <= endX && prevPosY <= endY && n == this.movement.path.length - 1) {
                    this.finished = true;
                    prevPosX = endX;
                    prevPosY = endY;
                }
                
                curPos = {
                    x: prevPosX,
                    y: prevPosY,
                    arrived: this.finished,
                    orientation: movementOrientation
                };
                break;
            default:
                console.log("Неизвестная ориентация движения:", movementOrientation);
                return false;
        }
        
        if (!this.finished) {
            var derp = Math.ceil((curTime - this.begTime) / this.spriteInterval);
            curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + (derp % 5);
        } else {
            curPos.spriteName = "peasant_" + curPos.orientation + "_walk_0";
            movementManager.removeFinishedSpriteMovements();
        }        
        
        this.unit.spritePos = curPos;
        console.log(curPos.x + "," + curPos.y + "," + curPos.orientation);
        
        return true;
    }
    
    return this;
}

function pathToString(path) {
    var out = "";
    
    for (var i = 0; i < path.length; i++) {
        out += path[i];
    }
    
    return out;
}

function convertLinkedPathToArray(path) {
    var array = new Array();
    var cur = path;
    
    do {
        array.push(new Point(cur.j, cur.i));
        cur = cur.parent;
    } while (cur != null);
    
    return array.reverse();
}
