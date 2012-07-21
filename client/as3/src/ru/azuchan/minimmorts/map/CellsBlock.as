package ru.azuchan.minimmorts.map {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.geom.Rectangle;
    import flash.geom.Point;
    
	/**
     * Блок клеток карты.
     * @author sorc
     */
    public class CellsBlock {
        
        /**
         * Ширина блока клеток в клетках.
         */
        private var _width:int;
        
        /**
         * Высота блока клеток в клетках.
         */
        private var _height:int;
        
        /**
         * Спрайт блока клеток.
         */
        private var sprite:Bitmap;
        
        public function CellsBlock(width:int, height:int) {
            _width = w;
            _height = h;
            sprite = new Bitmap();
            sprite.bitmapData = new BitmapData(_width * Config.CELL_W, _height * Config.CELL_H);
        }
        
        /**
         * Ширина блока клеток в клетках.
         */
        public function get width():int {
            return _width;
        }
        
        /**
         * Высота блока клеток в клетках.
         */
        public function get height():int {
            return _height;
        }
        
        /**
         * Записывает изображение в блок клеток.
         * @param stage главный холст
         */
        public function writeBitmapData(src:BitmapData, x:Number, y:Number/*, width:Number = Config.CELL_W, height:Number = Config.CELL_H*/):void {
            sprite.bitmapData.copyPixels(src, new Rectangle(0, 0, src.width, src.height), new Point(x, y));
        }
        
        /**
         * Добавляет блок клеток карты в список отображения главного холста.
         * @param stage главный холст
         */
        public function show(stage:Stage):void {
            if (!stage.contains(sprite)) {
                stage.addChild(sprite);
            }
        }
        
        /**
         * Обновляет положение блока карты на экране.
         * @param x координата верхней левой клетки блока клеток на экране
         * @param y координата верхней левой клетки блока клеток на экране
         */
        public function update(x:Number, y:Number):void {
            sprite.x = x;
            sprite.y = y;
        }
    }
}
