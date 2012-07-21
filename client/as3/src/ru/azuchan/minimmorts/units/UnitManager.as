package ru.azuchan.minimmorts.units {
    import ru.azuchan.minimmorts.movement.MovementManager;

	/**
     * Менеджер юнитов.
     * @author sorc
     */
    public class UnitManager {
        
        private var movementManager:MovementManager;
        
        private var units:Array;
        
        private var selfUnits:Array;
        
        private var selectedUnits:Array;
        
        public function UnitManager(movementManager:MovementManager) {
            this.movementManager = movementManager;
            units = [];
            selfUnits = [];
            selectedUnits = [];
        }
        
        public function addUnit(ownerId:int, id:int, j:int, i:int, r:int, g:int, b:int):void {
            var unit : Unit = new Unit(ownerId, id, j, i, r, g, b);
            units.push(unit);
            if (ownerId == Main.id) {
                selfUnits.push(unit);
                Main.camera.follow(unit);
                Main.camera.unfollow();
            }
        }
        
        public function update(camera:Camera):void {
            for (var i:int; i < units.length; i++) {
                units[i].update(camera);
            }
        }
        
        public function getUnitById(id:int):Unit {
            for (var i:int = 0; i < units.length; i++) {
                if (units[i].id == id) {
                    return units[i];
                }
            }
            
            return null;
        }
        
        public function delUnitById(id:int):Boolean {
            for (var i:int = 0; i < units.length; i++) {
                if (units[i].id == id) {
                    units.splice(i, 1);
                    movementManager.delMovementByUnitId(id);
                    movementManager.removeFollowingByTarget(units[i]);
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
        
        public function selfUnitsToProtocol1():String {
            var out:String = "";
            if (selfUnits.length > 0) {
                out += selfUnits[0].toProtocol1();
                for (var i:int = 1; i < selfUnits.length; i++) {
                    out += "," + selfUnits[i].toProtocol1();
                }
            }
            return out;
        }
        
        /**
         * Возвращает копию массива выделенных юнитов.
         * @return копия массива выделенных юнитов
         */
        public function getSelectedUnits():Array {
            return selectedUnits.concat();
        }
        
        public function tryFollowing(x:Number, y:Number):Boolean {
            for (var i:int = 0; i < units.length; i++) {
                for (var j:int = 0; j < selectedUnits.length; j++) {
                    if (units[i] != selectedUnits[j] && units[i].isInSprite(x, y)) {
                        for (var k:int = 0; k < selectedUnits.length; k++) {
                            movementManager.addFollowing(selectedUnits[k], units[i]);
                        }
                        return true;
                    }
                }
            }
            
            return false;
        }
        
        public function getUnits():Array {
            return units;
        }
        
        public function getSelfUnits():Array {
            return selfUnits;
        }
    }
}
