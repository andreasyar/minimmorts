package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;

	/**
     * ...
     * @author sorc
     */
    public class PathCache {
        
        private const MAX_CACHE_SIZE:int = 100;
        
        private var pathCache:Array;
        
        private var pathMap:Array;
        
        public function PathCache() {
            pathCache = [];
            pathMap = [];
        }
        
        public function cachePath(path:Array):Boolean {
            if (!isPathCached(path[0].j, path[0].i, path[path.length - 1].j, path[path.length - 1].i)) {
                var newPair:Pair = new Pair(clonePath(path), 0);
                var i:int;  // Индекс сохранённого пути в кеше.
                var curJ:int, curI:int;
                
                /*
                 * Если нужно, освободим место в кеше под путь.
                 */
                if (pathCache.length == MAX_CACHE_SIZE) {
                    var oldPair:Pair = getRarestUsedPath();
                    i = indexOf(pathCache, oldPair);
                    if (Config.DEBUG_MOVEMENTS) {
                        trace("Кеш путей заполнился. Удаляем", i, oldPair, newPair);
                    }
                    cleanPathMap(oldPair.getKey(), i);
                    pathCache[i] = newPair;
                } else {
                    pathCache.push(newPair);
                    i = indexOf(pathCache, newPair);
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
        
        public function getCachedPath(begJ:int, begI:int, endJ:int, endI:int, unit:Unit):Array {
            if (pathMap[begI] != undefined && pathMap[begI][begJ] != undefined && pathMap[endI] != undefined && pathMap[endI][endJ] != undefined) {
                for (var i:int = 0; i < pathMap[begI][begJ].length; i++) {
                    for (var j:int = 0; j < pathMap[endI][endJ].length; j++) {
                        if (pathMap[begI][begJ][i] == pathMap[endI][endJ][j]) {
                            var p:Pair = pathCache[pathMap[begI][begJ][i]];
                            var path:Array = p.getKey();
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
                                p.setValue(p.getValue() + 1);
                            }
                            
                            return savedPath;
                        }
                    }
                }
            }
            
            return null;
        }
        
        private function indexOf(array:Array, value:Object):int {
            for (var i:int = 0; i < array.length; i++) {
                if (array[i] == value) {
                    return i;
                }
            }
            
            return -1;
        }
        
        private function clonePath(path:Array):Array {
            var clone:Array = new Array();
            for (var i:int = 0; i < path.length; i++) {
                clone.push(path[i]);
            }
            return clone;
        }
        
        private function getRarestUsedPath():Pair {
            var used:int = 0;
            var i:int;
            
            while (pathCache.length > 0 && used < 1000000) {
                for (i = 0; i < pathCache.length; i++) {
                    if (pathCache[i].getValue() == used) {
                        return pathCache[i];
                    }
                }
                used++;
            }
            
            return null;
        }
        
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
        
        private function cleanPathMap(path:Array, i:int):void {
            for (var j:int = 0; j < path.length; j++) {
                this.pathMap[path[j].i][path[j].j] = removeByValue(pathMap[path[j].i][path[j].j], i);
            }
        }
        
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
