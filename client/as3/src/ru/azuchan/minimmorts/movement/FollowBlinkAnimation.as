package ru.azuchan.minimmorts.movement {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import ru.azuchan.minimmorts.sprite.Sprite;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * ...
     * @author sorc
     */
    public class FollowBlinkAnimation {
        private var unit:Unit;
        
        private var sloader:SpriteLoader;
        
        private var begTime:Number;
        
        private var sprite:Bitmap;
        
        public function FollowBlinkAnimation(unit:Unit) {
            this.unit = unit;
            sloader = Main.spriteLoader;
            begTime = (new Date()).getTime();
            sprite = new Bitmap();
            sprite.bitmapData = sloader.getSpriteByName("peasant_follow").getBitmapData();
            var s:Sprite = sloader.getSpriteByName("peasant_follow");
            var drawCoordinates:Object = s.getDrawCoordinatesByXY(unit.spritePos.x, unit.spritePos.y, Main.camera);
            sprite.x = drawCoordinates.x;
            sprite.y = drawCoordinates.y;
            Main.stage.addChild(sprite);
        }
        
        public function update(camera:Camera):void {
            var curTime:Number = (new Date()).getTime();
            
            if (begTime + 3000 < curTime) {
                var i:int = 0;
                if ( (i = Main.freeAnimation.indexOf(this)) != -1) {
                    Main.freeAnimation.splice(i, 1);
                    if (Main.stage.contains(sprite)) {
                        Main.stage.removeChild(sprite);
                    }
                    return;
                }
            }
            
            var diff:Number = curTime - begTime;
            if (Math.ceil(diff / 500) % 2 == 0) {
                var s:Sprite = sloader.getSpriteByName("peasant_follow");
                var drawCoordinates:Object = s.getDrawCoordinatesByXY(unit.spritePos.x, unit.spritePos.y, camera);
                sprite.x = drawCoordinates.x;
                sprite.y = drawCoordinates.y;
                if (!Main.stage.contains(sprite)) {
                    Main.stage.addChild(sprite);
                }
            } else {
                if (Main.stage.contains(sprite)) {
                    Main.stage.removeChild(sprite);
                }
            }
        }
    }
}
