package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * Кеш путей на карте.
     * @author sorc
     */
    public class PathCache {
        
        /**
         * Максимальный размер кеша.
         */
        private static const MAX_CACHE_SIZE:int = 100;
        
        /**
         * Кеш путей (массив пар (Pair)).
         */
        private var pathCache:Array;
        
        /**
         * Карта путей. Каждая ячейка содержит индекс пути из кеша, который через неё проходит.
         */
        private var pathMap:Array;
        
        /**
         * Создаёт новый кеш путей.
         */
        public function PathCache() {
            pathCache = [];
            pathMap = [];
        }
        
        /**
         * Добавляет путь в кеш.
         * @param path путь
         */
        public function cachePath(path:Array):Boolean {
            if (!isPathCached(path[0].j, path[0].i, path[path.length - 1].j, path[path.length - 1].i)) {
                var newPair:Pair = new Pair(path.concat(), 0);
                var i:int;  // Индекс сохранённого пути в кеше.
                var curJ:int, curI:int;
                
                /*
                 * Если нужно, освободим место в кеше под путь.
                 */
                if (pathCache.length == PathCache.MAX_CACHE_SIZE) {
                    var oldPair:Pair = getRarestUsedPath();
                    i = pathCache.indexOf(oldPair);
                    if (Config.DEBUG_MOVEMENTS) {
                        trace("Кеш путей заполнился. Удаляем", i, oldPair, newPair);
                    }
                    cleanPathMap(oldPair.key, i);
                    pathCache[i] = newPair;
                } else {
                    pathCache.push(newPair);
                    i = pathCache.indexOf(newPair);
                }
                
                /*
                 * Отметим на карте путей клетки, через которые проходит добавленный путь.
                 */
                for (var j:int = 0; j < path.length; j++) {
                    curJ = path[j].j;
                    curI = path[j].i;
                    if (pathMap[curI] == undefined) {
                        pathMap[curI] = [];
                    }
                    if (pathMap[curI][curJ] == undefined) {
                        pathMap[curI][curJ] = [];
                    }
                    pathMap[curI][curJ].push(i);
                }
                
                return true;
            }
            
            return false;
        }
        
        /**
         * Возваращает путь из кеша, проходящий через заданные кетки. Или null, если в кеше нет
         * подходящего пути.
         * @param begJ j-индекс на карте клетки отправления
         * @param begI i-индекс на карте клетки отправления
         * @param endJ j-индекс на карте клетки назначения
         * @param endI i-индекс на карте клетки назначения
         * @return путь или null
         */
        public function getCachedPath(begJ:int, begI:int, endJ:int, endI:int, unit:Unit):Array {
            if (pathMap[begI] != undefined && pathMap[begI][begJ] != undefined && pathMap[endI] != undefined && pathMap[endI][endJ] != undefined) {
                for (var i:int = 0; i < pathMap[begI][begJ].length; i++) {
                    for (var j:int = 0; j < pathMap[endI][endJ].length; j++) {
                        if (pathMap[begI][begJ][i] == pathMap[endI][endJ][j]) {
                            var p:Pair = pathCache[pathMap[begI][begJ][i]];
                            var path:Array = p.key;
                            var savedPath:Array = [];
                            var isPathReverted:Boolean = false;
                            var doSave:Boolean = false;
                            var k:int;
                            for (k = 0; k < path.length; k++) {
                                if (path[k].j == begJ && path[k].i == begI) {
                                    isPathReverted = false;
                                    break;
                                }
                                if (path[k].j == endJ && path[k].i == endI) {
                                    isPathReverted = true;
                                    break;
                                }
                            }
                            if (isPathReverted) {
                                return null;
                            } else {
                                if (Config.DEBUG_MOVEMENTS) {
                                    trace(Main.step + ": В кеше найден нужный путь для юнита " + unit + " " + k + ":"/* + pathToString(path)*/ + " проходящий через точки "/* + waypointsToString([new Point(begJ, begI), new Point(endJ, endI)])*/);
                                }
                                doSave = false;
                                for (k = 0; k < path.length; k++) {
                                    if (path[k].j == begJ && path[k].i == begI) {
                                        doSave = true;
                                    }
                                    if (path[k].j == endJ && path[k].i == endI) {
                                        savedPath.push(path[k]);
                                        break;
                                    }
                                    if (doSave) {
                                        savedPath.push(path[k]);
                                    }
                                }
                                
                                /*
                                 * Увеличим счетчик использований этого пути.
                                 */
                                p.value = p.value + 1;
                            }
                            
                            return savedPath;
                        }
                    }
                }
            }
            
            return null;
        }
        
        /**
         * Возвращает самый редко используемый путь (Pair).
         * @return запись из кеша путей (Pair).
         */
        private function getRarestUsedPath():Pair {
            var used:int = 0;
            var i:int;
            
            while (pathCache.length > 0 && used < 1000000) {
                for (i = 0; i < pathCache.length; i++) {
                    if (pathCache[i].value == used) {
                        return pathCache[i];
                    }
                }
                used++;
            }
            
            return null;
        }
        
        /**
         * Проверяет, есть ли в кеше путь, проходящий через заданные клетки.
         * @param begJ j-индекс на карте клетки отправления
         * @param begI i-индекс на карте клетки отправления
         * @param endJ j-индекс на карте клетки назначения
         * @param endI i-индекс на карте клетки назначения
         * @return есть ли в кеше подходящий путь
         */
        private function isPathCached(begJ:int, begI:int, endJ:int, endI:int):Boolean {
            if (pathMap[begI] != undefined && pathMap[begI][begJ] != undefined && pathMap[endI] != undefined && pathMap[endI][endJ] != undefined) {
                for (var i:int = 0; i < pathMap[begI][begJ].length; i++) {
                    for (var j:int = 0; j < pathMap[endI][endJ].length; j++) {
                        if (pathMap[begI][begJ][i] == pathMap[endI][endJ][j]) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        }
        
        /**
         * Очищает клетки карты путей от заданного пути.
         * @param path удаляемый путь
         * @param i индекс удаляемого пути
         */
        private function cleanPathMap(path:Array, i:int):void {
            for (var j:int = 0; j < path.length; j++) {
                this.pathMap[path[j].i][path[j].j] = removeByValue(pathMap[path[j].i][path[j].j], i);
            }
        }
        
        /**
         * Удаляет из массива заданное значение и возвращает этот же массив, но уже без удалённого значения.
         * @param array массив
         * @param value значение
         * @return результирующий массив
         */
        private function removeByValue(array:Array, value:Object):Array {
            for (var i:int = 0; i < array.length; i++) {
                if (array[i] == value) {
                    array.splice(i, 1)
                    return array;
                }
            }
            
            return array;
        }
    }
}
