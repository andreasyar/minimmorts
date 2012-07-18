package {

    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.geom.Point;
    import flash.geom.Rectangle;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * Карта игрового мира.
     * @author sorc
     */
    public class Map {
        
        /**
         * Ширина карты в клетках.
         */
        private var w:int;
        
        /**
         * Высота карты в клетках.
         */
        private var h:int;
        
        /**
         * Загрузчик спрайтов.
         */
        private var sloader:SpriteLoader;
        
        /**
         * Карта спрайтов.
         */
        private var spriteMap:Array;
        
        /**
         * Карта макроблоков карты.
         */
        private var macromap:Array;
        
        public function Map(w:int, h:int) {
            this.w = w;
            this.h = h;
            sloader = Main.spriteLoader;
            spriteMap = [];
            macromap = [];
            
            var i:int, j:int, k:int, l:int;
            
            for (i = 0; i < this.h; i++) {
                spriteMap[i] = [];
                for (j = 0; j < this.w; j++) {
                    spriteMap[i][j] = sloader.getSpriteByName("grass").getBitmapData();
                }
            }
            
            var MACROBLOCK_W:int = 20;
            var MACROBLOCK_H:int = 20;
            var macromap_h:int = Math.ceil(this.h / MACROBLOCK_H);
            var macromap_w:int = Math.ceil(this.w / MACROBLOCK_W);
            var dim:Object = { j_from : 0, j_to : 0, i_from : 0, i_to : 0 };
            for (i = 0; i < macromap_h; i++) {
                macromap[i] = [];
                for (j = 0; j < macromap_w; j++) {
                    macromap[i][j] = new Bitmap();
                    macromap[i][j].bitmapData = new BitmapData(
                        MACROBLOCK_W * Config.CELL_W,
                        MACROBLOCK_H * Config.CELL_H
                    );
                    
                    dim.j_from = j * MACROBLOCK_W;
                    dim.j_to = dim.j_from + MACROBLOCK_W;
                    dim.i_from = i * MACROBLOCK_H;
                    dim.i_to = dim.i_from + MACROBLOCK_H;
                    
                    var x:Number = 0;
                    var y:Number = 0;
                    for (k = dim.i_from; k < dim.i_to; k++) {
                        for (l = dim.j_from; l < dim.j_to; l++) {
                            macromap[i][j].bitmapData.copyPixels(
                                spriteMap[k][l],
                                new Rectangle(0, 0, Config.CELL_W, Config.CELL_H),
                                new Point(x, y)
                            );
                            x += Config.CELL_W;
                        }
                        x = 0;
                        y += Config.CELL_H;
                    }
                }
            }
            
            for (i = 0; i < macromap.length; i++) {
                for (j = 0; j < macromap[i].length; j++) {
                    Main.stage.addChild(macromap[i][j]);
                }
            }
        }
        
        public function getWidth():int {
            return w;
        }
        
        public function getHeight():int {
            return h;
        }
        
        public function update(camera:Camera):void {
            var tAxis:Object;
            
            for (var i:int = 0; i < macromap.length; i++) {
                for (var j:int = 0; j < macromap[i].length; j++) {
                    tAxis = camera.translateAxis(Map.getMacroBlockXOnMap(j), Map.getMacroBlockYOnMap(i));
                    macromap[i][j].x = tAxis.x;
                    macromap[i][j].y = tAxis.y;
                }
            }
        }
        
        /**
         * Проверяет, принадлежит ли клетка карте.
         * @param j j-индекс клетки.
         * @param i i-индекс клетки.
         */
        public function isOnMap(j:int, i:int):Boolean {
            return i >= 0 && j >= 0 && i < h && j < w;
        }
        
        public static function getMacroBlockJOnMap(x:Number):int {
            var MACROBLOCK_W:int = 20;
            return Math.floor(x / (MACROBLOCK_W * Config.CELL_W));
        }
        
        public static function getMacroBlockIOnMap(y:Number):int {
            var MACROBLOCK_H:int = 20;
            return Math.floor(y / (MACROBLOCK_H * Config.CELL_H));
        }   
        
        public static function getMacroBlockXOnMap(j:int):Number {
            var MACROBLOCK_W:int = 20;
            return j * (MACROBLOCK_W * Config.CELL_W);
        }
        
        public static function getMacroBlockYOnMap(i:int):Number {
            var MACROBLOCK_H:int = 20;
            return i * (MACROBLOCK_H * Config.CELL_H);
        }
        
        public static function getCellXOnMap(j:int):Number {
            return j * Config.CELL_W;
        }
        
        public static function getCellYOnMap(i:int):Number {
            return i * Config.CELL_H;
        }
        
        public static function getCellJOnMap(x:Number):int {
            return Math.floor(x / Config.CELL_W);
        }
        
        public static function getCellIOnMap(y:Number):int {
            return Math.floor(y / Config.CELL_H);
        }
    }
}