package ru.azuchan.minimmorts.units {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import ru.azuchan.minimmorts.follow.FollowManager;
    import ru.azuchan.minimmorts.movement.MovementManager;

	/**
     * Менеджер юнитов.
     * @author sorc
     */
    public class UnitManager {
        
        /**
         * Менеджер движений.
         */
        private var movementManager:MovementManager;
        
        /**
         * Менеджер преследований.
         */
        private var followManager:FollowManager;
        
        /**
         * Массив юнитов.
         */
        private var units:Array;
        
        /**
         * Массив юнитов, принадлежащих игроку.
         */
        private var selfUnits:Array;
        
        /**
         * Массив выделенных юнитов.
         */
        private var selectedUnits:Array;
        
        /**
         * Создаёт новый менеджер юнитов.
         * @param movementManager менеджер движений
         * @param followManager менеджер преследований
         */
        public function UnitManager(movementManager:MovementManager, followManager:FollowManager) {
            this.movementManager = movementManager;
            this.followManager = followManager;
            units = [];
            selfUnits = [];
            selectedUnits = [];
        }
        
        /**
         * Возвращает копию массива выделенных юнитов.
         * @return копия массива выделенных юнитов
         */
        public function getSelectedUnits():Array {
            return selectedUnits.concat();
        }
        
        /**
         * Добавляет юнита.
         * @param ownerId идентификатор владельца юнита
         * @param id идентификатор юнита
         * @param j j-координата клетки карты, где находится юнит
         * @param i i-координата клетки карты, где находится юнит
         * @param r красная компонента цвета юнита
         * @param g зеленая компонента цвета юнита
         * @param b синяя компонента цвета юнита
         */
        public function addUnit(ownerId:int, id:int, j:int, i:int, r:int, g:int, b:int):void {
            var unit : Unit = new Unit(ownerId, id, j, i, r, g, b);
            units.push(unit);
            if (ownerId == Main.id) {
                selfUnits.push(unit);
                Main.camera.follow(unit);
                Main.camera.unfollow();
            }
        }
        
        /**
         * Обновляет положение всех юнитов.
         * @param camera камера
         */
        public function update(camera:Camera):void {
            for (var i:int; i < units.length; i++) {
                units[i].update(camera);
            }
        }
        
        /**
         * Возвращает юнита по идентификатору.
         * @param id идентификатор юнита
         * @return юнит или null, если юнит не был найден
         */
        public function getUnitById(id:int):Unit {
            for (var i:int = 0; i < units.length; i++) {
                if (units[i].id == id) {
                    return units[i];
                }
            }
            
            return null;
        }
        
        /**
         * Удаляет юнита по идентификатору.
         * @param id идентификатор юнита
         * @return вовзвращает true, если юнит был успешно удалён и
         * false, если юнит не был найден
         */
        public function delUnitById(id:int):Boolean {
            for (var i:int = 0; i < units.length; i++) {
                if (units[i].id == id) {
                    units[i].hide();
                    units.splice(i, 1);
                    movementManager.delMovementByUnitId(id);
                    followManager.removeFollowingByObject(units[i]);
                    return true;
                }
            }
            for (i = 0; i < selfUnits.length; i++) {
                if (selfUnits[i].id == id) {
                    selfUnits.splice(i, 1);
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Возвращает хеш юнитов.
         * @return хеш юнитов
         */
        public function getUnitsHash():Object {
            var hash:String = "";
            
            for (var i:int = 0; i < units.length - 1; i++) {
                hash += units[i].toProtocol1() + ",";
            }
            hash += units[units.length - 1].toProtocol1();
            
            return {
                unitsCount: units.length,
                unitsString: hash
            };
        }
        
        /**
         * Выделяет юнитов в заданной точке.
         * @param x x-координата точки на экране
         * @param y y-координата точки на экране
         */
        public function selectUnits(x:Number, y:Number):void {
            selectedUnits = [];
            for (var i:int = 0; i < selfUnits.length; i++) {
                if (selfUnits[i].isInSprite(x, y)) {
                    selfUnits[i].selected = true;
                    selectedUnits.push(selfUnits[i]);
                } else {
                    selfUnits[i].selected = false;
                }
            }
        }
        
        /**
         * Выделяет юнитов в заданной прямоугольной области.
         * @param dim прямоугольная область
         */
        public function selectUnitsRect(dim:Object):int {
            var n:int = 0;
            selectedUnits = [];
            var k:int;
            
            for (k = 0; k < selfUnits.length; k++) {
                selfUnits[k].selected = false;
            }
            for (var i:int = dim.begI; i < dim.endI; i++) {
                for (var j:int = dim.begJ; j < dim.endJ; j++) {
                    for (k = 0; k < selfUnits.length; k++) {
                        if (selfUnits[k].j == j && selfUnits[k].i == i) {
                            selfUnits[k].selected = true;
                            selectedUnits.push(selfUnits[k]);
                            n++;
                        }
                    }
                }
            }
            
            return n;
        }
        
        /**
         * Пытается запустить преследование. Возвращает true, если преследование
         * было запущено и flase в противном случае.
         * @param x x-координата нажатия мыши на экране
         * @param y y-координата нажатия мыши на экране
         */
        public function tryFollowing(x:Number, y:Number):Boolean {
            for (var i:int = 0; i < units.length; i++) {
                for (var j:int = 0; j < selectedUnits.length; j++) {
                    if (units[i] != selectedUnits[j] && units[i].isInSprite(x, y)) {
                        for (var k:int = 0; k < selectedUnits.length; k++) {
                            followManager.addFollowing(selectedUnits[k], units[i]);
                        }
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        /**
         * Отмечает положение юнитов на миникарте.
         * @param minimap миникарта
         */
        public function markUnitsOnMinimap(minimap:Bitmap):void {
            var i:int;
            for (i = 0; i < units.length; i++) {
                minimap.bitmapData.setPixel(units[i].j, units[i].i, 0xFF0000);
            }
            for (i = 0; i < selfUnits.length; i++) {
                minimap.bitmapData.setPixel(units[i].j, units[i].i, 0x0000FF);
            }
        }
    }
}
