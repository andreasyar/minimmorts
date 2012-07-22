package ru.azuchan.minimmorts.follow {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Stage;
    import ru.azuchan.minimmorts.sprite.Sprite;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * Анимация мигающей рамки вокруг преследуемого юнита.
     * @author sorc
     */
    public class FollowBlinkAnimation {
        
        /**
         * Преследуемый юнит.
         */
        private var unit:Unit;
        
        /**
         * Загрузчик спрайтов.
         */
        private var sloader:SpriteLoader;
        
        /**
         * Время начала проигрывания анимации.
         */
        private var begTime:Number;
        
        /**
         * Спрайт с рамкой.
         */
        private var sprite:Bitmap;
        
        /**
         * Холст, на котором был нарисован спрайт.
         */
        private var stage:Stage;
        
        /**
         * Создаёт новую анимацию мигающей рамки вокруг преследуемого юнита.
         * @param unit преследуемый юнит
         */
        public function FollowBlinkAnimation(unit:Unit, stage:Stage) {
            this.unit = unit;
            sloader = Main.spriteLoader;
            begTime = (new Date()).getTime();
            sprite = new Bitmap();
            sprite.bitmapData = sloader.getSpriteByName("peasant_follow").bitmapData;
            var s:Sprite = sloader.getSpriteByName("peasant_follow");
            var drawCoordinates:Object = s.getDrawCoordinatesByXY(unit.spritePos.x, unit.spritePos.y, Main.camera);
            sprite.x = drawCoordinates.x;
            sprite.y = drawCoordinates.y;
            this.stage = stage;
        }
        
        /**
         * Добавляет анимацию мигающей рамки вокруг преследуемого юнита в список отображения главного холста.
         */
        private function show():void {
            if (!stage.contains(sprite)) {
                stage.addChild(sprite);
            }
        }
        
        /**
         * Удаляет анимацию мигающей рамки вокруг преследуемого юнита с холста.
         */
        private function hide():void {
            if (stage.contains(sprite)) {
                stage.removeChild(sprite);
            }
        }
        
        /**
         * Обновляет положение мигающей рамки вокруг преследуемого юнита на экране.
         * @param stage главный холст
         * @param camera камера
         */
        public function update(camera:Camera):void {
            var curTime:Number = (new Date()).getTime();
            
            if (begTime + 3000 < curTime) {
                var i:int = 0;
                if ( (i = Main.freeAnimation.indexOf(this)) != -1) {
                    Main.freeAnimation.splice(i, 1);
                    hide();
                    return;
                }
            }
            
            var diff:Number = curTime - begTime;
            if (Math.ceil(diff / 500) % 2 == 0) {
                var s:Sprite = sloader.getSpriteByName("peasant_follow");
                var drawCoordinates:Object = s.getDrawCoordinatesByXY(unit.spritePos.x, unit.spritePos.y, camera);
                sprite.x = drawCoordinates.x;
                sprite.y = drawCoordinates.y;
                show();
            } else {
                hide();
            }
        }
    }
}
