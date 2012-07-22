package ru.azuchan.minimmorts.units {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import ru.azuchan.minimmorts.sprite.Sprite;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import ru.azuchan.minimmorts.map.Map;
    
	/**
     * Юнит.
     * @author sorc
     */
    public class Unit {
        
        /**
         * Идентификатор владельца юнита.
         */
        private var ownerId:int;
        
        /**
         * Идентификатор юнита.
         */
        private var _id:int;
        
        /**
         * j-координата клетки карты, где находится юнит.
         */
        public var j:int;
        
        /**
         * i-координата клетки карты, где находится юнит.
         */
        public var i:int;
        
        /**
         * Загрузчик спрайтов.
         */
        private var sloader:SpriteLoader;
        
        /**
         * Флаг выделения юнита.
         */
        public var selected:Boolean;
        
        /**
         * Положение спрайта юнита.
         */
        public var spritePos:Object;
        
        /**
         * Спрайт юнита.
         */
        private var sprite:Bitmap;
        
        /**
         * Рамка выделения юнита.
         */
        private var selectionSprite:Bitmap;
        
        /**
         * Цветной квадратик под юнитом.
         */
        private var colorMarkSprite:Bitmap;
        
        /**
         * Создаёт нового юнита.
         * @param ownerId идентификатор владельца юнита
         * @param id идентификатор юнита
         * @param j j-координата клетки карты, где находится юнит
         * @param i i-координата клетки карты, где находится юнит
         * @param r красная компонента цвета юнита
         * @param g зеленая компонента цвета юнита
         * @param b синяя компонента цвета юнита
         */
        public function Unit(ownerId:int, id:int, j:int, i:int, r:int, g:int, b:int) {
            this.ownerId = ownerId;
            _id = id;
            this.j = j;
            this.i = i;
            
            sloader = Main.spriteLoader;
            selected = false;
            spritePos = {
                x: Map.getCellXOnMap(this.j),
                y: Map.getCellYOnMap(this.i),
                arrived: true,
                orientation: "S",
                spriteName: "peasant_S_walk_0"
            }
            var rgba:Number = 0x00000000;
            rgba |= (r << 24);
            rgba |= (g << 16);
            rgba |= (b << 8);
            rgba |= 128;
            colorMarkSprite = new Bitmap(new BitmapData(Config.CELL_W, Config.CELL_H, true, rgba));
            Main.stage.addChild(colorMarkSprite);
            sprite = new Bitmap();
            sprite.bitmapData = sloader.getSpriteByName(spritePos.spriteName).bitmapData;
            Main.stage.addChild(sprite);
            selectionSprite = new Bitmap();
            selectionSprite.bitmapData = sloader.getSpriteByName("peasant_selection").bitmapData;
        }
        
        /**
         * Идентификатор юнита.
         */
        public function get id():int {
            return _id;
        }
        
        /**
         * Удаляет с главного холста все спрайты связанные с юнитом.
         */
        internal function hide():void {
            if (Main.stage.contains(sprite)) {
                Main.stage.removeChild(sprite);
            }
            if (Main.stage.contains(selectionSprite)) {
                Main.stage.removeChild(selectionSprite);
            }
            if (Main.stage.contains(colorMarkSprite)) {
                Main.stage.removeChild(colorMarkSprite);
            }
        }
        
        /**
         * Обновляет позицию спрайтов юнита на экране.
         * @param camera камера
         */
        public function update(camera:Camera):void {
            var s:Sprite;
            var drawCoordinates:Object;
            if (Config.smoothMovementEnabled) {
                s = sloader.getSpriteByName(spritePos.spriteName);
                if (s == null) {
                    trace("Спрайт " + spritePos.spriteName + " не найден.");
                    return;
                }
                sprite.bitmapData = s.bitmapData;
                drawCoordinates = s.getDrawCoordinatesByXY(spritePos.x, spritePos.y, camera);
            } else {
                s = sloader.getSpriteByName("peasant_" + spritePos.orientation);
                if (s == null) {
                    trace("Спрайт " + "peasant_" + spritePos.orientation + " не найден.");
                    return;
                }
                sprite.bitmapData = s.bitmapData;
                drawCoordinates = s.getDrawCoordinatesByXY(Map.getCellXOnMap(j), Map.getCellYOnMap(i), camera);
            }
            sprite.x = drawCoordinates.x;
            sprite.y = drawCoordinates.y;
            colorMarkSprite.x = drawCoordinates.x + 21;
            colorMarkSprite.y = drawCoordinates.y + 17;
            if (selected) {
                if (!Main.stage.contains(selectionSprite)) {
                    Main.stage.addChild(selectionSprite);
                }
                selectionSprite.x = drawCoordinates.x;
                selectionSprite.y = drawCoordinates.y;
            } else if (Main.stage.contains(selectionSprite)) {
                Main.stage.removeChild(selectionSprite);
            }
            return;
        }
        
        /**
         * Проверяет, принадлежат ли заданные координаты спрайту юнита.
         * @param x-координата на экране
         * @param y-координата на экране
         * @return принадлежат ли заданные координаты спрайту юнита
         */
        public function isInSprite(x:Number, y:Number):Boolean {
            var s:Sprite = sloader.getSpriteByName(spritePos.spriteName);
            var drawCoordinates:Object = s.getDrawCoordinatesByXY(spritePos.x, spritePos.y, Main.camera);
            return x >= drawCoordinates.x && x < drawCoordinates.x + s.width && y >= drawCoordinates.y && y < drawCoordinates.y + s.height;
        }
        
        /**
         * Возвращает юнита в формате Protocol-1.
         * @return юнит в формате Protocol-1
         */
        public function toProtocol1():String {
            return ownerId + "," + _id + "," + j + "," + i;
        }
    }
}
