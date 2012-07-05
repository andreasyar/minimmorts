/**
 * Плавное движение спрайта юнита по клеткам.
 */
function SpriteMovement(unit, movement) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    if (!(unit instanceof Unit)) {
        throw new Error("Юнит не является объектом типа Unit.");
    }

    /**
     * Юнит.
     */
    this.unit = unit;

    /**
     * Движение юнита.
     */
    this.movement = movement;

    /**
     * Флаг окончания движения спрайта.
     */
    this.finished = false;
    
    /**
     * Анимация движения спрайта юнита.
     */
    this.spriteMovementAnimation = new SpriteMovementAnimation();

    /**
     * Возвращает юнита.
     */
    this.getUnit = function() {
        return this.unit;
    }
    
    /**
     * Проверяет, не завершилось ли движение спрайта.
     */
    this.isFinished = function() {
        return this.finished;
    }

    /**
     * Обновляет положение спрайта юнита.
     */
    this.updatePosition = function() {

        if (stepBegTime == undefined || stepBegTime == null || prevStepTime == undefined || prevStepTime == null) {
            console.log("Время начала текущего хода и время предыдущего хода не заданы.");
            return false;
        }
        
        if ((new Date()).getTime() - stepBegTime < 0) {
            console.log("Время начала текущего хода ещё не пришло.");
            return false;
        }
        
        if (step == undefined || step == null) {
            console.log("Номер текущнго шага не задан.");
            return false;
        }

        // Стоим на месте.
        if (step == this.movement.beg_step) {
            return true;
        }

        // Стоим на месте.
        if ((new Date()).getTime() - stepBegTime > prevStepTime) {
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
        
        if (movementOrientation == null) {
            console.log("Не удалось вычислить ориентацию движения.");
            return false;
        }

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
        }

        if (this.finished) {
            curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + this.spriteMovementAnimation.getStand();
            movementManager.removeFinishedSpriteMovements();
        } else {
            curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + this.spriteMovementAnimation.next();
        }

        this.unit.spritePos = curPos;

        return true;
    }

    return this;
}
