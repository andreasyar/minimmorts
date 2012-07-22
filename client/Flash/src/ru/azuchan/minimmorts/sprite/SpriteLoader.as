package ru.azuchan.minimmorts.sprite {
    import flash.display.Loader;
    import flash.events.Event;
    import flash.net.URLRequest;

	/**
     * Загрузчик спрайтов.
     * @author sorc
     */
    public class SpriteLoader {
        
        /**
         * Массив спрайтов.
         */
        private var sprites:Array;
        
        /**
         * Обработчик события окончания загрузки спрайтов.
         */
        private var loadCompleteCallback:Function;
        
        /**
         * Создаёт загрузчик спрайтов.
         * @param loadCompleteCallback обработчик события окончания загрузки спрайтов
         */
        public function SpriteLoader(loadCompleteCallback:Function) {
            sprites = [];
            this.loadCompleteCallback = loadCompleteCallback;
            
            var prefix : String = "http://2.92.40.121/flash/sprites/";
            var i:int, o:String;
            
            sprites.push({
                name: "grass",
                src: prefix + "grass.png",
                baseX: 16,
                baseY: 16,
                w: 32,
                h: 32,
                sprite: null,
                isLoaded: false
            });
            sprites.push({
                name: "grass_border",
                src: prefix + "grass_border.png",
                baseX: 16,
                baseY: 16,
                w: 32,
                h: 32,
                sprite: null,
                isLoaded: false
            });
            sprites.push({
                name: "peasant_selection",
                src: prefix + "peasant_selection.png",
                baseX: 36,
                baseY: 33,
                w: 72,
                h: 72,
                sprite: null,
                isLoaded: false
            });
            sprites.push({
                name: "peasant_follow",
                src: prefix + "peasant_follow.png",
                baseX: 36,
                baseY: 33,
                w: 72,
                h: 72,
                sprite: null,
                isLoaded: false
            });
            
            for each (o in ["NE", "SE", "SW", "NW", "N", "E", "S", "W"]) {
                sprites.push({
                    name: "peasant_" + o,
                    src: prefix + "peasant_" + o + ".png",
                    baseX: 36,
                    baseY: 33,
                    w: 72,
                    h: 72,
                    sprite: null,
                    isLoaded: false
                });
            }
            
            for (i = 0; i < 5; i++) {
                for each (o in ["NE", "SE", "SW", "NW", "N", "E", "S", "W"]) {
                    sprites.push({
                        name: "peasant_" + o + "_walk_" + i,
                        src: prefix + "peasant_" + o + "_walk_" + i + ".png",
                        baseX: 36,
                        baseY: 33,
                        w: 72,
                        h: 72,
                        sprite: null,
                        isLoaded: false
                    });
                }
            }
            
            for (i = 0; i < sprites.length; i++) {
                var loader : Loader = new Loader();
                loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadComplete);
                loader.load(new URLRequest(sprites[i].src));
            }
        }
        
        /**
         * Проверяет все ли спрайты загружены.
         * @return все ли спрайты загружены
         */
        private function isAllSpritesLoaded():Boolean {
            for (var i:int = 0; i < sprites.length; i++) {
                if (!sprites[i].isLoaded) {
                    return false;
                }
            }
            
            return true;
        }
        
        /**
         * Обработчик окончания загрузки спрайта.
         * @param event событие окончания загрузки спрайта
         */
        private function loadComplete(event:Event):void {
            for (var i:int = 0; i < sprites.length; i++) {
                if (sprites[i].src == event.target.url) {
                    sprites[i].sprite = new Sprite(
                        event.target.content.bitmapData,
                        sprites[i].w,
                        sprites[i].h,
                        sprites[i].baseX,
                        sprites[i].baseY
                    );
                    sprites[i].isLoaded = true;
                    break;
                }
            }
            
            if (isAllSpritesLoaded()) {
                loadCompleteCallback();
            }
        }
        
        /**
         * Возвращает спрайт по его имени.
         * @param name Имя спрайта.
         * @return Возвращает спрайт или null, если спрайт не найден.
         */
        public function getSpriteByName(name:String):Sprite {
            for (var i:int = 0; i < sprites.length; i++) {
                if (sprites[i].name == name && sprites[i].sprite != null) {
                    return sprites[i].sprite;
                }
            }
            
            trace("Спрайт с именем name=" + name + " не найден.");
            return null;
        }
    }
}
