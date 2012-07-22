package ru.azuchan.minimmorts.sprite {
    import ru.azuchan.minimmorts.map.Map;
    import flash.display.BitmapData;
    
	/**
     * Спрайт.
     * @author sorc
     */
    public class Sprite {
        
        /**
         * Данные изображения.
         */
        private var _bitmapData:BitmapData;
        
        /**
         * Ширина спрайта.
         */
        private var _width:int;
        
        /**
         * Высота спрайта.
         */
        private var _height:int;
        
        /**
         * x-координата опорной точки спрайта.
         */
        private var baseX:int;
        
        /**
         * y-координата опорной точки спрайта.
         */
        private var baseY:int;
        
        /**
         * Создаёт спрайт.
         * @param bitmapData данные изображения
         * @param width ширина спрайта
         * @param height высота спрайта
         * @param baseX x-координата опорной точки спрайта
         * @param baseY y-координата опорной точки спрайта
         */
        public function Sprite(bitmapData:BitmapData, width:int, height:int, baseX:int, baseY:int) {
            _bitmapData = bitmapData;
            _width = width;
            _height = height;
            this.baseX = baseX;
            this.baseY = baseY;
        }
        
        /**
         * Данные изображения.
         */
        public function get bitmapData():BitmapData {
            return _bitmapData;
        }
        
        /**
         * Ширина спрайта.
         */
        public function get width():int {
            return _width;
        }
        
        /**
         * Высота спрайта.
         */
        public function get height():int {
            return _height;
        }
        
        /**
         * Возвращает координаты спрайта юнита на экране по положению юнита на карте.
         * @param unitJ j-индекс клетки карты
         * @param unitI i-индекс клетки карты
         * @param camera камера
         * @return координаты спрайта юнита на экране
         */
        public function getDrawCoordinates(unitJ:int, unitI:int, camera:Camera):Object {
            if (_width < Config.CELL_W || _height < Config.CELL_H) {
                trace("Размеры спрайта меньше, чем размеры клетки карты.");
                return null;
            }
            
            if (baseX < Config.CELL_HW || baseY < Config.CELL_HH || baseX + Config.CELL_HW > _width || baseY + Config.CELL_HH > _height) {
                trace("Спрайт не покрывает клетку карты целиком.");
                return null;
            }
            
            // Координаты клетки на карте, где находится игрок.
            var unitMXY:Object = camera.translateAxis(Map.getCellXOnMap(unitJ), Map.getCellYOnMap(unitI));
            
            return {
                x: unitMXY.x - (baseX - Config.CELL_HW),
                y: unitMXY.y - (baseY - Config.CELL_HH)
            };
        }
        
        /**
         * Возвращает координаты спрайта юнита на экране по положению юнита на карте.
         * @param unitMX x-координата юнита на карте
         * @param unitMY y-координата юнита на карте
         * @param camera камера
         * @return координаты спрайта юнита на экране
         */
        public function getDrawCoordinatesByXY(unitMX:int, unitMY:int, camera:Camera):Object {
            if (_width < Config.CELL_W || _height < Config.CELL_H) {
                trace("Размеры спрайта меньше, чем размеры клетки карты.");
                return null;
            }
            
            if (baseX < Config.CELL_HW || baseY < Config.CELL_HH || baseX + Config.CELL_HW > _width || baseY + Config.CELL_HH > _height) {
                trace("Спрайт не покрывает клетку карты целиком.");
                return null;
            }
            
            // Координаты клетки на карте, где находится игрок.
            var unitMXY:Object = camera.translateAxis(unitMX, unitMY);
            
            return {
                x: unitMXY.x - (baseX - Config.CELL_HW),
                y: unitMXY.y - (baseY - Config.CELL_HH)
            };
        }
    }
}