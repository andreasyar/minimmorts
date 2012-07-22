package ru.azuchan.minimmorts.map {
    import flash.display.Stage;
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
        private var _width:int;
        
        /**
         * Высота карты в клетках.
         */
        private var _height:int;
        
        /**
         * Загрузчик спрайтов.
         */
        private var sloader:SpriteLoader;
        
        /**
         * Карта спрайтов.
         */
        private var spriteMap:Array;
        
        /**
         * Блоки клеток. Кеш для оптимизации рисования карты.
         */
        private var cellBlocks:Array;
        
        /**
         * Ширина блока клеток в клетках.
         */
        public static const CELLBLOCK_W:int = 20;
        
        /**
         * Высота блока клеток в клетках.
         */
        public static const CELLBLOCK_H:int = 20;
        
        /**
         * Создаёт новую карту.
         * @param width ширина в клетках
         * @param height высота в клетках
         */
        public function Map(width:int, height:int) {
            _width = width;
            _height = height;
            sloader = Main.spriteLoader;
            spriteMap = [];
            cellBlocks = [];
            
            var i:int, j:int, k:int, l:int, x:Number, y:Number;
            
            for (i = 0; i < _height; i++) {
                spriteMap[i] = [];
                for (j = 0; j < _width; j++) {
                    spriteMap[i][j] = sloader.getSpriteByName("grass").bitmapData;
                }
            }
            
            var cellBlocksW:int = Math.ceil(_width / Map.CELLBLOCK_W);
            var cellBlocksH:int = Math.ceil(_height / Map.CELLBLOCK_H);
            var dim:Object = { jFrom : 0, jTo : 0, iFrom : 0, iTo : 0 };
            for (i = 0; i < cellBlocksH; i++) {
                cellBlocks[i] = [];
                for (j = 0; j < cellBlocksW; j++) {
                    cellBlocks[i][j] = new CellsBlock(Map.CELLBLOCK_W, Map.CELLBLOCK_H);
                    
                    dim.jFrom = j * Map.CELLBLOCK_W;
                    dim.jTo = dim.jFrom + Map.CELLBLOCK_W;
                    dim.iFrom = i * Map.CELLBLOCK_H;
                    dim.iTo = dim.iFrom + Map.CELLBLOCK_H;
                    
                    x = 0;
                    y = 0;
                    for (k = dim.iFrom; k < dim.iTo; k++) {
                        for (l = dim.jFrom; l < dim.jTo; l++) {
                            cellBlocks[i][j].writeBitmapData(spriteMap[k][l], x, y);
                            x += Config.CELL_W;
                        }
                        x = 0;
                        y += Config.CELL_H;
                    }
                }
            }
        }
        
        /**
         * Ширина карты в клетках.
         */
        public function get width():int {
            return _width;
        }
        
        /**
         * Высота карты в клетках.
         */
        public function get height():int {
            return _height;
        }
        
        /**
         * Добавляет карту в список отображения главного холста.
         * @param stage главный холст
         */
        public function show(stage:Stage):void {
            for (var i:int = 0; i < cellBlocks.length; i++) {
                for (var j:int = 0; j < cellBlocks[i].length; j++) {
                    cellBlocks[i][j].show(stage);
                }
            }
        }
        
        /**
         * Обновляет положение карты на экране в зависимости от того где находится камера,
         * тем самым эмулируется движение камеры над картой.
         * @param camera камера
         */
        public function update(camera:Camera):void {
            var tAxis:Object;
            
            for (var i:int = 0; i < cellBlocks.length; i++) {
                for (var j:int = 0; j < cellBlocks[i].length; j++) {
                    tAxis = camera.translateAxis(Map.getCellBlockXOnMap(j), Map.getCellBlockYOnMap(i));
                    cellBlocks[i][j].update(tAxis.x, tAxis.y);
                }
            }
        }
        
        /**
         * Проверяет, принадлежит ли клетка карте.
         * @param j индекс клетки по ширине
         * @param i индекс клетки по высоте
         * @return принадлежит ли клетка карте
         */
        public function isOnMap(j:int, i:int):Boolean {
            return i >= 0 && j >= 0 && i < _height && j < _width;
        }
        
        /**
         * Возвращает клетки карты, соседние заданным клеткам вместе с заданными клетками,
         * но без повторений.
         * @param cells массив клеток карты
         * @return соседние клетки
         */
        public function getNeighboursCells(cells:Array):Array {
            // TODO: Доработать алгоритм.
            var neighbours:Array = [];
            var checkCell:Function = function (neighbours:Array, _j:int, _i:int):void {
                if ((neighbours[_i] == undefined || neighbours[_i][_j] == undefined) && isOnMap(_j, _i)) {
                    if (neighbours[_i] == undefined) {
                        neighbours[_i] = [];
                    }
                    neighbours[_i][_j] = new Cell(_j, _i);
                }
            }
            var i:int, j:String, k:String, result:Array = cells;
            for (i = 0; i < cells.length; i++) {
                checkCell(neighbours, cells[i].j - 1, cells[i].i - 1);
                checkCell(neighbours, cells[i].j    , cells[i].i - 1);
                checkCell(neighbours, cells[i].j + 1, cells[i].i - 1);
                checkCell(neighbours, cells[i].j - 1, cells[i].i);
                checkCell(neighbours, cells[i].j + 1, cells[i].i);
                checkCell(neighbours, cells[i].j - 1, cells[i].i + 1);
                checkCell(neighbours, cells[i].j    , cells[i].i + 1);
                checkCell(neighbours, cells[i].j + 1, cells[i].i + 1);
            }
            for (j in neighbours) {
                for (k in neighbours[j]) {
                    result.push(neighbours[j][k]);
                }
            }
            return result;
        }
        
        /**
         * Возвращает j-индекс верхней левой клетки блока клеток на карте по её экранной x-координате.
         * @param x координата верхней левой клетки блока клеток на экране
         * @return j-индекс верхней левой клетки блока клеток на карте
         */
        public static function getCellBlockJOnMap(x:Number):int {
            return Math.floor(x / (Map.CELLBLOCK_W * Config.CELL_W));
        }
        
        /**
         * Возвращает i-индекс верхней левой клетки блока клеток на карте по её экранной y-координате.
         * @param y координата верхней левой клетки блока клеток на экране
         * @return i-индекс верхней левой клетки блока клеток на карте
         */
        public static function getCellBlockIOnMap(y:Number):int {
            return Math.floor(y / (Map.CELLBLOCK_H * Config.CELL_H));
        }
        
        /**
         * Возвращает экранную x-координату верхней левой клетки блока клеток по её j-индексу на карте.
         * @param j индекс верхней левой клетки блока клеток на карте
         * @return x-координата верхней левой клетки блока клеток на экране
         */
        public static function getCellBlockXOnMap(j:int):Number {
            return j * (Map.CELLBLOCK_W * Config.CELL_W);
        }
        
        /**
         * Возвращает экранную y-координату верхней левой клетки блока клеток по её i-индексу на карте.
         * @param i индекс верхней левой клетки блока клеток на карте
         * @return y-координата верхней левой клетки блока клеток на экране
         */
        public static function getCellBlockYOnMap(i:int):Number {
            return i * (Map.CELLBLOCK_H * Config.CELL_H);
        }
        
        /**
         * Возвращает j-индекс клетки на карте по её экранной x-координате.
         * @param x координата клетки на экране
         * @return j-индекс клетки на карте
         */
        public static function getCellJOnMap(x:Number):int {
            return Math.floor(x / Config.CELL_W);
        }
        
        /**
         * Возвращает i-индекс клетки на карте по её экранной y-координате.
         * @param y координата клетки на экране
         * @return i-индекс клетки на карте
         */
        public static function getCellIOnMap(y:Number):int {
            return Math.floor(y / Config.CELL_H);
        }
        
        /**
         * Возвращает экранную x-координату клетки по её j-индексу на карте.
         * @param j индекс клетки на карте
         * @return x-координата клетки на экране
         */
        public static function getCellXOnMap(j:int):Number {
            return j * Config.CELL_W;
        }
        
        /**
         * Возвращает экранную y-координату клетки по её i-индексу на карте.
         * @param i индекс клетки на карте
         * @return y-координата клетки на экране
         */
        public static function getCellYOnMap(i:int):Number {
            return i * Config.CELL_H;
        }
    }
}