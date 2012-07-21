package ru.azuchan.minimmorts.units {

    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import ru.azuchan.minimmorts.sprite.Sprite;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    
	/**
     * Юнит.
     * @author sorc
     */
    public class Unit {
        
        private var ownerId:int;
        
        /**
         * Идентификатор юнита.
         */
        private var _id:int;
        
        public var j:int;
        
        public var i:int;
        
        private var sloader:SpriteLoader;
        
        public var selected:Boolean;
        
        public var spritePos:Object;
        
        private var sprite:Bitmap;
        
        private var selectionSprite:Bitmap;
        
        private var colorMarkSprite:Bitmap;
        
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
            sprite.bitmapData = sloader.getSpriteByName(spritePos.spriteName).getBitmapData();
            Main.stage.addChild(sprite);
            selectionSprite = new Bitmap();
            selectionSprite.bitmapData = sloader.getSpriteByName("peasant_selection").getBitmapData();
        }
        
        public function getOwnerId():int {
            return ownerId;
        }
        
        /**
         * Идентификатор юнита.
         */
        public function get id():int {
            return _id;
        }
        
        public function update(camera:Camera):void {
            var s:Sprite;
            var drawCoordinates:Object;
            if (Config.smoothMovementEnabled) {
                s = sloader.getSpriteByName(spritePos.spriteName);
                if (s == null) {
                    trace("Спрайт " + spritePos.spriteName + " не найден.");
                    return;
                }
                sprite.bitmapData = s.getBitmapData();
                drawCoordinates = s.getDrawCoordinatesByXY(spritePos.x, spritePos.y, camera);
            } else {
                s = sloader.getSpriteByName("peasant_" + spritePos.orientation);
                if (s == null) {
                    trace("Спрайт " + "peasant_" + spritePos.orientation + " не найден.");
                    return;
                }
                sprite.bitmapData = s.getBitmapData();
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
        
        public function isInSprite(mapX:int, mapY:int):Boolean {
            var s:Sprite = sloader.getSpriteByName(spritePos.spriteName);
            return mapX >= spritePos.x && mapX < spritePos.x + s.w && mapY >= spritePos.y && mapY < spritePos.y + s.h;
        }
        
        public function toProtocol1():String {
            return ownerId + "," + _id + "," + j + "," + i;
        }
    }
}
