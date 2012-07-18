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
        
        private var id:int;
        
        public var j:int;
        
        public var i:int;
        
        private var sloader:SpriteLoader;
        
        public var selected:Boolean;
        
        public var spritePos:Object;
        
        private var sprite:Bitmap;
        
        private var selectionSprite:Bitmap;
        
        public function Unit(ownerId:int, id:int, j:int, i:int, r:int, g:int, b:int) {
            this.ownerId = ownerId;
            this.id = id;
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
            sprite = new Bitmap();
            sprite.bitmapData = sloader.getSpriteByName(spritePos.spriteName).getBitmapData();
            Main.stage.addChild(sprite);
            selectionSprite = new Bitmap();
            selectionSprite.bitmapData = sloader.getSpriteByName("peasant_selection").getBitmapData();
            // TODO: Цветной фон под юнитом.
        }
        
        public function getOwnerId():int {
            return ownerId;
        }
        
        public function getId():int {
            return id;
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
                drawCoordinates = s.getDrawCoordinatesByXY(spritePos.x, spritePos.y, camera);
            } else {
                s = sloader.getSpriteByName("peasant_" + spritePos.orientation);
                if (s == null) {
                    trace("Спрайт " + "peasant_" + spritePos.orientation + " не найден.");
                    return;
                }
                drawCoordinates = s.getDrawCoordinatesByXY(Map.getCellXOnMap(j), Map.getCellYOnMap(i), camera);
            }
            sprite.x = drawCoordinates.x;
            sprite.y = drawCoordinates.y;
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
            return ownerId + "," + id + "," + j + "," + i;
        }
    }
}
