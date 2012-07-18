package ru.azuchan.minimmorts.sprite {
    import ru.azuchan.minimmorts.movement.Movement;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.movement.Point;
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.client.ClientMainLoop;

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
         * Флаг окончания движения спрайта.
         */
        private var finished:Boolean;
        
        /**
         * Анимация движения спрайта юнита.
         */
        private var spriteMovementAnimation:SpriteMovementAnimation;
        
        public function SpriteMovement(unit:Unit, movement:Movement) {
            this.unit = unit;
            this.movement = movement;
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
            if (Main.step == movement.getBegStep()) {
                return true;
            }
            
            // Стоим на месте.
            if ((new Date()).getTime() - ClientMainLoop.stepBegTime > ClientMainLoop.prevStepTime) {
                return true;
            }
            
            var prevStepBegTime:int = ClientMainLoop.stepBegTime - ClientMainLoop.prevStepTime;
            
            var curTime:int = (new Date()).getTime() - ClientMainLoop.prevStepTime;
            var diffTime:int = curTime - prevStepBegTime;
            
            var m:int = ((Main.step - 1) - movement.getBegStep()) * movement.getSpeed();
            var prevPos:Point = null;
            var n:int = Math.floor(m);
            if (n > movement.getPath().length - 1) {
                n = movement.getPath().length - 1;
            }
            prevPos = movement.getPath()[n];
            
            var movementOrientation:String;
            if (n == movement.getPath().length - 1) {
                movementOrientation = MovementManager.getMovementOrientation(
                    movement.getPath()[n - 1].j,
                    movement.getPath()[n - 1].i,
                    movement.getPath()[n].j,
                    movement.getPath()[n].i
                );
            } else {    // n < this.movement.path.length - 1
                movementOrientation = MovementManager.getMovementOrientation(
                    movement.getPath()[n].j,
                    movement.getPath()[n].i,
                    movement.getPath()[n + 1].j,
                    movement.getPath()[n + 1].i
                );
            }
            
            if (movementOrientation == null) {
                trace("Не удалось вычислить ориентацию движения.");
                return false;
            }
            
            var prevPosX:Number = Map.getCellXOnMap(prevPos.j);
            var prevPosY:Number = Map.getCellYOnMap(prevPos.i);
            
            var v:Number = (movement.getSpeed() * Config.CELL_W) / ClientMainLoop.prevStepTime;
            
            var curPos:Object = null;
            var endX:Number = Map.getCellXOnMap(movement.getPath()[movement.getPath().length - 1].j);
            var endY:Number = Map.getCellYOnMap(movement.getPath()[movement.getPath().length - 1].i);
            switch (movementOrientation) {
                case "N":
                    prevPosY -= (m - n) * Config.CELL_H;
                    prevPosY -= v * diffTime;
                    prevPosY = Math.floor(prevPosY);
                    
                    if (prevPosY <= endY && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX >= endX && prevPosY <= endY && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX >= endX && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX >= endX && prevPosY >= endY && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosY >= endY && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX <= endX && prevPosY >= endY && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX <= endX && n == movement.getPath().length - 1) {
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
                    
                    if (prevPosX <= endX && prevPosY <= endY && n == movement.getPath().length - 1) {
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
                Main.movementManager.removeFinishedSpriteMovements();
            } else {
                curPos.spriteName = "peasant_" + curPos.orientation + "_walk_" + spriteMovementAnimation.next();
            }
            
            unit.spritePos = curPos;
            
            return true;
        }
    }
}
