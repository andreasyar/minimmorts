package ru.azuchan.minimmorts.sprite {
    
    import flash.display.Loader;
    import flash.events.Event;
    import flash.net.URLRequest;

	/**
     * Загрузчик спрайтов.
     * @author sorc
     */
    public class SpriteLoader {
        
        private var sprites:Array;
        
        private var loadCompleteCallback:Function;
        
        public function SpriteLoader(loadCompleteCallback:Function) {
            sprites = [];
            this.loadCompleteCallback = loadCompleteCallback;
            var prefix : String = "http://2.92.40.121/flash/sprites/";
            
            sprites.push( { name: "grass", src: prefix + "grass.png", baseX: 16, baseY: 16, w: 32, h: 32, sprite: null, isLoaded: false } );
            sprites.push( { name: "grass_border", src: prefix + "grass_border.png", baseX: 16, baseY: 16, w: 32, h: 32, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_selection", src: prefix + "peasant_selection.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_follow", src: prefix + "peasant_follow.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            
            sprites.push( { name: "peasant_NE", src: prefix + "peasant_NE.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_SE", src: prefix + "peasant_SE.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_SW", src: prefix + "peasant_SW.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_NW", src: prefix + "peasant_NW.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            
            sprites.push( { name: "peasant_N", src: prefix + "peasant_N.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_E", src: prefix + "peasant_E.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_S", src: prefix + "peasant_S.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            sprites.push( { name: "peasant_W", src: prefix + "peasant_W.png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            
            var i:int;
            for (i = 0; i < 5; i++) {
                sprites.push( { name: "peasant_NE_walk_" + i, src: prefix + "peasant_NE_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_SE_walk_" + i, src: prefix + "peasant_SE_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_SW_walk_" + i, src: prefix + "peasant_SW_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_NW_walk_" + i, src: prefix + "peasant_NW_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_N_walk_" + i, src: prefix + "peasant_N_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_E_walk_" + i, src: prefix + "peasant_E_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_S_walk_" + i, src: prefix + "peasant_S_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
                sprites.push( { name: "peasant_W_walk_" + i, src: prefix + "peasant_W_walk_" + i + ".png", baseX: 36, baseY: 33, w: 72, h: 72, sprite: null, isLoaded: false } );
            }
            
            for (i = 0; i < sprites.length; i++) {
                var loader : Loader = new Loader();
                loader.contentLoaderInfo.addEventListener(Event.COMPLETE, loadComplete);
                loader.load(new URLRequest(sprites[i].src));
            }
        }
        
        private function isAllSpritesLoaded():Boolean {
            for (var i:int = 0; i < sprites.length; i++) {
                if (!sprites[i].isLoaded) {
                    return false;
                }
            }
            
            return true;
        }
        
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
            
            trace("Sprite name=" + name + " not found.");
            return null;
        }
    }
}
