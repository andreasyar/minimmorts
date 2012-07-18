package ru.azuchan.minimmorts.sprite {

    import flash.display.BitmapData;
    
	/**
     * Спрайт.
     * @author sorc
     */
    public class Sprite {
        
        private var bitmapData:BitmapData;
        
        public var w:int;
        
        public var h:int;
        
        private var baseX:int;
        
        private var baseY:int;
        
        public function Sprite(bitmapData:BitmapData, w:int, h:int, baseX:int, baseY:int) {
            this.bitmapData = bitmapData;
            this.w = w;
            this.h = h;
            this.baseX = baseX;
            this.baseY = baseY;
        }
        
        public function getBitmapData():BitmapData {
            return bitmapData;
        }
        
        public function getDrawCoordinates(unitJ:int, unitI:int, camera:Camera):Object {
            
            if (w < Config.CELL_W || h < Config.CELL_H) {
                trace("Sprite bounds lesser than map cell.");
                return null;
            }
            
            if (baseX < Config.CELL_HW || baseY < Config.CELL_HH || baseX + Config.CELL_HW > w || baseY + Config.CELL_HH > h) {
                trace("Sprite cannot cover all cell.");
                return null;
            }
            
            // Координаты на карте ячейки, где находится игрок.
            var unitMXY:Object = camera.translateAxis(Map.getCellXOnMap(unitJ), Map.getCellYOnMap(unitI));
            
            return {
                x: unitMXY.x - (this.baseX - Config.CELL_HW),
                y: unitMXY.y - (this.baseY - Config.CELL_HH)
            };
        }
        
        public function getDrawCoordinatesByXY(unitMX:int, unitMY:int, camera:Camera):Object {
            
            if (w < Config.CELL_W || h < Config.CELL_H) {
                trace("Sprite bounds lesser than map cell.");
                return null;
            }
            
            if (baseX < Config.CELL_HW || baseY < Config.CELL_HH || baseX + Config.CELL_HW > w || baseY + Config.CELL_HH > h) {
                trace("Sprite cannot cover all cell.");
                return null;
            }
            
            // Координаты на карте ячейки, где находится игрок.
            var unitMXY:Object = camera.translateAxis(unitMX, unitMY);
            
            return {
                x: unitMXY.x - (this.baseX - Config.CELL_HW),
                y: unitMXY.y - (this.baseY - Config.CELL_HH)
            };
        }
    }
}