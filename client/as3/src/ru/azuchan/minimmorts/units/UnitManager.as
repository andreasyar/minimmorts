package ru.azuchan.minimmorts.units {

	/**
     * Менеджер юнитов.
     * @author sorc
     */
    public class UnitManager {
        
        private var units:Array;
        
        private var selfUnits:Array;
        
        private var selectedUnits:Array;
        
        public function UnitManager() {
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
                if (units[i].getId() == id) {
                    return units[i];
                }
            }
            
            return null;
        }
        
        public function delUnitById(id:int):Boolean {
            for (var i:int = 0; i < units.length; i++) {
                if (units[i].getId() == id) {
                    units.splice(i, 1);
                    Main.movementManager.delMovementByUnitId(id);
                    //removeFollowingByTarget(units[i]);
                    return true;
                }
            }
            for (i = 0; i < selfUnits.length; i++) {
                if (selfUnits[i].getId() == id) {
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
        
        public function getSelectedUnits():Array {
            return selectedUnits;
        }
    }
}
