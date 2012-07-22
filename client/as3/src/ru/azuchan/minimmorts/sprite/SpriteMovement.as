package ru.azuchan.minimmorts.sprite {
    import ru.azuchan.minimmorts.map.Cell;
    import ru.azuchan.minimmorts.movement.Movement;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.client.ClientMainLoop;
    import ru.azuchan.minimmorts.map.Map;

	/**
     * Плавное движение спрайта юнита по клеткам карты.
     * @author sorc
     */
    public class SpriteMovement {
        
        /**
         * Юнит.
         */
        private var unit:Unit;
        
        /**
         * Движение юнита.
         */
        private var movement:Movement;
        
        /**
         * Обработчик события завершения движения.
         */
        private var onMovementComplete:Function;
        
        /**
         * Флаг окончания движения спрайта.
         */
        private var finished:Boolean;
        
        /**
         * Анимация движения спрайта юнита.
         */
        private var spriteMovementAnimation:SpriteMovementAnimation;
        
        /**
         * Создаёт новое движение спрайта.
         * @param unit юнит
         * @param movement движение юнита
         * @param onMovementComplete обработчик события завершения движения
         */
        public function SpriteMovement(unit:Unit, movement:Movement, onMovementComplete:Function) {
            this.unit = unit;
            this.movement = movement;
            this.onMovementComplete = onMovementComplete;
            finished = false;
            spriteMovementAnimation = new SpriteMovementAnimation();
        }
        
        /**
         * Возвращает юнита.
         */
        public function getUnit():Unit {
            return unit;
        }
        
        /**
         * Проверяет, не завершилось ли движение спрайта.
         */
        public function isFinished():Boolean {
            return finished;
        }
        
        /**
         * Обновляет положение спрайта юнита.
         * @return обновилось положение спрайта или нет
         */
        public function updatePosition():Boolean {
            if (ClientMainLoop.stepBegTime == -1 || ClientMainLoop.prevStepTime == -1) {
                trace("Время начала текущего хода и время предыдущего хода не заданы.");
                return false;
            }
            
            if ((new Date()).getTime() - ClientMainLoop.stepBegTime < 0) {
                trace("Время начала текущего хода ещё не пришло.");
                return false;
            }
            
            if (Main.step == -1) {
                trace("Номер текущнго шага не задан.");
                return false;
            }
            
            // Стоим на месте.
            if (Main.step == movement.begStep) {
                return true;
            }
            
            // Стоим на месте.
            if ((new Date()).getTime() - ClientMainLoop.stepBegTime > ClientMainLoop.prevStepTime) {
                return true;
            }
            
            var prevStepBegTime:int = ClientMainLoop.stepBegTime - ClientMainLoop.prevStepTime;
            
            var curTime:int = (new Date()).getTime() - ClientMainLoop.prevStepTime;
            var diffTime:int = curTime - prevStepBegTime;
            
            var m:Number = ((Main.step - 1) - movement.begStep) * movement.speed;
            var prevPos:Cell = null;
            var n:int = Math.floor(m);
            if (n > movement.path.length - 1) {
                n = movement.path.length - 1;
            }
            prevPos = movement.path[n];
            
            var movementOrientation:String;
            if (n == movement.path.length - 1) {
                movementOrientation = MovementManager.getMovementOrientation(
                    movement.path[n - 1].j,
                    movement.path[n - 1].i,
                    movement.path[n].j,
                    movement.path[n].i
                );
            } else {    // n < this.movement.path.length - 1
                movementOrientation = MovementManager.getMovementOrientation(
                    movement.path[n].j,
                    movement.path[n].i,
                    movement.path[n + 1].j,
                    movement.path[n + 1].i
                );
            }
            
            if (movementOrientation == null) {
                trace("Не удалось вычислить ориентацию движения.");
                return false;
            }
            
            var prevPosX:Number = Map.getCellXOnMap(prevPos.j);
            var prevPosY:Number = Map.getCellYOnMap(prevPos.i);
            
            var v:Number = (movement.speed * Config.CELL_W) / ClientMainLoop.prevStepTime;
            
            var curPos:Object = null;
            var endX:Number = Map.getCellXOnMap(movement.path[movement.path.length - 1].j);
            var endY:Number = Map.getCellYOnMap(movement.path[movement.path.length - 1].i);
            switch (movementOrientation) {
                case "N":
                    prevPosY -= (m - n) * Config.CELL_H;
                    prevPosY -= v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosY <= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "NE":
                    prevPosX += (m - n) * Config.CELL_W;
                    prevPosX += v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    prevPosY -= (m - n) * Config.CELL_H;
                    prevPosY -= v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosX >= endX && prevPosY <= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "E":
                    prevPosX += (m - n) * Config.CELL_W;
                    prevPosX += v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    
                    if (prevPosX >= endX && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "SE":
                    prevPosX += (m - n) * Config.CELL_W;
                    prevPosX += v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    prevPosY += (m - n) * Config.CELL_H;
                    prevPosY += v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosX >= endX && prevPosY >= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "S":
                    prevPosY += (m - n) * Config.CELL_H;
                    prevPosY += v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosY >= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "SW":
                    prevPosX -= (m - n) * Config.CELL_W;
                    prevPosX -= v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    prevPosY += (m - n) * Config.CELL_H;
                    prevPosY += v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosX <= endX && prevPosY >= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "W":
                    prevPosX -= (m - n) * Config.CELL_W;
                    prevPosX -= v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    
                    if (prevPosX <= endX && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
                case "NW":
                    prevPosX -= (m - n) * Config.CELL_W;
                    prevPosX -= v * diffTime;
                    prevPosX = Math.floor(prevPosX);
                    prevPosY -= (m - n) * Config.CELL_H;
                    prevPosY -= v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosX <= endX && prevPosY <= endY && n == movement.path.length - 1) {
                        finished = true;
                        prevPosX = endX;
                        prevPosY = endY;
                    }
                    
                    curPos = {
                        x: prevPosX,
                        y: prevPosY,
                        arrived: finished,
                        orientation: movementOrientation
                    };
                    break;
            }
            
            if (finished) {
                curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + spriteMovementAnimation.getStand();
                onMovementComplete();
            } else {
                curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + spriteMovementAnimation.next();
            }
            
            unit.spritePos = curPos;
            
            return true;
        }
    }
}
