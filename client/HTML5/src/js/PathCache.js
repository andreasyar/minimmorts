/**
 * Кеш путей.
 * @author Sorc
 */
function PathCache() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    PathCache.MAX_CACHE_SIZE = 100;
    
    this.pathCache = [];
    
    this.pathMap = [];
    
    this.cachePath = function(path) {
        var pathCache = this.pathCache;
        var pathMap = this.pathMap;
        
        if (!this.isPathCached(path[0].getJ(), path[0].getI(), path[path.length - 1].getJ(), path[path.length - 1].getI())) {
            var newPair = new Pair(clonePath(path), 0);
            var i;  // Индекс сохранённого пути в кеше.
            var curJ, curI;
            
            /*
             * Если нужно, освободим место в кеше под путь.
             */
            if (pathCache.length == PathCache.MAX_CACHE_SIZE) {
                var oldPair = this.getRarestUsedPath();
                i = indexOf(pathCache, oldPair);
                if (DEBUG_MOVEMENTS) {
                    console.log("Кеш путей заполнился. Удаляем", i, oldPair, newPair);
                }
                this.cleanPathMap(oldPair.getKey(), i);
                pathCache[i] = newPair;
            } else {
                pathCache.push(newPair);
                i = indexOf(pathCache, newPair);
            }
            
            /*
             * Отметим на карте путей клетки, через которые проходит добавленный путь.
             */
            for (var j = 0; j < path.length; j++) {
                curJ = path[j].getJ();
                curI = path[j].getI();
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
    
    this.getCachedPath = function(begJ, begI, endJ, endI, unit) {
        var pathMap = this.pathMap;
        var pathCache = this.pathCache;
        
        if (pathMap[begI] != undefined && pathMap[begI][begJ] != undefined && pathMap[endI] != undefined && pathMap[endI][endJ] != undefined) {
            for (var i = 0; i < pathMap[begI][begJ].length; i++) {
                for (var j = 0; j < pathMap[endI][endJ].length; j++) {
                    if (pathMap[begI][begJ][i] == pathMap[endI][endJ][j]) {
                        var p = pathCache[pathMap[begI][begJ][i]];
                        var path = p.getKey();
                        var savedPath = [];
                        var isPathReverted = false;
                        var doSave = false;
                        var k;
                        for (k = 0; k < path.length; k++) {
                            if (path[k].getJ() == begJ && path[k].getI() == begI) {
                                isPathReverted = false;
                                break;
                            }
                            if (path[k].getJ() == endJ && path[k].getI() == endI) {
                                isPathReverted = true;
                                break;
                            }
                        }
                        if (isPathReverted) {
                            /*TODO: translate to JS
                            for (k = path.size() - 1; k >= 0; k--) {
                                if (path.get(k).getJ() == begJ && path.get(k).getI() == begI) {
                                    doSave = true;
                                }
                                if (path.get(k).getJ() == endJ && path.get(k).getI() == endI) {
                                    savedPath.add(path.get(k));
                                    break;
                                }
                                if (doSave) {
                                    savedPath.add(path.get(k));
                                }
                            }*/
                            return null;
                        } else {
                            if (DEBUG_MOVEMENTS) {
                                console.log(step + ": В кеше найден нужный путь для юнита " + unit + " " + k + ":" + pathToString(path) + " проходящий через точки " + waypointsToString([new Point(begJ, begI), new Point(endJ, endI)]));
                            }
                            doSave = false;
                            for (k = 0; k < path.length; k++) {
                                if (path[k].getJ() == begJ && path[k].getI() == begI) {
                                    doSave = true;
                                }
                                if (path[k].getJ() == endJ && path[k].getI() == endI) {
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
    
    this.getRarestUsedPath = function() {
        var used = 0;
        var i;
        var pathCache = this.pathCache;
        
        while (pathCache.length > 0 && used < 1000000) {
            for (i = 0; i < pathCache.length; i++) {
                if (pathCache[i].getValue() == used) {
                    console.log("i=" + i);
                    return pathCache[i];
                }
            }
            used++;
        }
        
        return null;
    }
    
    this.isPathCached = function(begJ, begI, endJ, endI) {
        var pathMap = this.pathMap;
        
        if (pathMap[begI] != undefined && pathMap[begI][begJ] != undefined && pathMap[endI] != undefined && pathMap[endI][endJ] != undefined) {
            for (var i = 0; i < pathMap[begI][begJ].length; i++) {
                for (var j = 0; j < pathMap[endI][endJ].length; j++) {
                    if (pathMap[begI][begJ][i] == pathMap[endI][endJ][j]) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    this.cleanPathMap = function(path, i) {
        for (var j = 0; j < path.length; j++) {
            this.pathMap[path[j].getI()][path[j].getJ()] = removeByValue(this.pathMap[path[j].getI()][path[j].getJ()], i);
        }
    }
    
    this.toString = function() {
        var pathMap = this.pathMap;
        var out = "";
        for (var i = 0; i < pathMap.length; i++) {
            if (pathMap[i] != undefined) {
                out += "\n" + i + "[";
                for (var j = 0; j < pathMap[i].length; j++) {
                    if (pathMap[i][j] != undefined) {
                        out += j + "[";
                        for (var k = 0; k < pathMap[i][j].length; k++) {
                            out += pathMap[i][j][k] + ",";
                        }
                        out += "],";
                    }
                }
                out += "]";
            }
        }
        return out;
    }
}

function Pair(key, value) {
    
    this.key = key;
    
    this.value = value;
    
    this.getKey = function() {
        return this.key;
    }
    
    this.getValue = function() {
        return this.value;
    }
    
    this.setValue = function(value) {
        var oldValue = this.value;
        this.value = value;
        return oldValue;
    }
}
