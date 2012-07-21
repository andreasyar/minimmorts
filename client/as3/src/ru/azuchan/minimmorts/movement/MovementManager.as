package ru.azuchan.minimmorts.movement {
    import ru.azuchan.minimmorts.units.Unit;
    import ru.azuchan.minimmorts.sprite.SpriteMovement;

	/**
     * Менеджер движений. Хранит и управляет движением.
     * @author sorc
     */
    public class MovementManager {
        
        /**
         * Движения юнитов.
         */
        private var movements:Array;
        
        /**
         * Движения спрайтов юнитов.
         */
        private var spriteMovements:Array;
        
        private var followings:Array;
        
        public function MovementManager() {
            movements = [];
            spriteMovements = [];
            followings = [];
        }
        
        /**
         * Проверяет, движется ли юнит.
         */
        private function isUnitMove(unit:Unit):Boolean {
            for (var i:int = 0; i < movements.length; i++) {
                if (movements[i].getUnit() == unit && !movements[i].isFinished()) {
                    return true;
                }
            }
            
            return false;
        }
        
        /**
         * Создаёт новое движение.
         */
        public function startNewMovement(begStep:int, unit:Unit, waypoints:Array):void {
            if (Config.DEBUG_MOVEMENTS) {
                trace(Main.step + ": Создаётся новое движение юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/);
            }
            
            var path:Array = generatePath(Main.map, waypoints, unit);
            if (path == null) {
                trace(Main.step + ": Путь движения юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/ + " не найден.");
                return;
            }
            
            var movement:Movement = new Movement(unit, Config.DEFAULT_SPEED, begStep, waypoints, path);
            movements.push(movement);
            
            spriteMovements.push(new SpriteMovement(unit, movement, removeFinishedSpriteMovements));
        }
        
        /**
         * Обновляет существующее движение.
         */
        public function updateMovement(begStep:int, unit:Unit, waypoints:Array):void {
            var existedMovement:Movement = null;
            for (var i:int = 0; i < movements.length; i++) {
                if (movements[i].getUnit() == unit) {
                    existedMovement = movements[i];
                    break;
                }
            }
            if (existedMovement == null) {
                trace(Main.step + ": Движение юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/ + ", которое следует обновить, не найдено.");
                return;
            }
            if (Config.DEBUG_MOVEMENTS) {
                trace(Main.step + ": Обновляется движение юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/);
            }
            existedMovement.setBegStep(begStep);
            
            var path:Array = generatePath(Main.map, waypoints, existedMovement.getUnit());
            if (path == null) {
                trace(Main.step + ": Путь движения юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/ + " не найден.");
                return;
            }
            
            existedMovement.setWaypoints(waypoints);
            existedMovement.setPath(path);
        }
        
        /**
         * Обновляет позиции юнитов.
         */
        public function updatePositions(step:int):void {
            removeFinishedMovements();  // TODO В том чтобы удалять движения именно тут есть глубинный смысл, надо от него избавится.
            for (var i:int = 0; i < movements.length; i++) {
                if (!(movements[i] is Movement)) {
                    throw new Error("Движение не является объектом типа Movement.");
                }
                movements[i].updatePosition(step);
            }
        }
        
        /**
         * Обновляет позиции спрайтов юнитов.
         */
        public function updateSpritePositions():void {
            for (var i:int = 0; i < spriteMovements.length; i++) {
                if (!(spriteMovements[i] is SpriteMovement)) {
                    throw new Error("Движение спрайта не является объектом типа SpriteMovement.");
                }
                spriteMovements[i].updatePosition();
            }
        }
        
        /**
         * Удаляет завершенные движения.
         */
        private function removeFinishedMovements():void {
            var movementsInProgress:Array = [];
            for (var i:int = 0; i < movements.length; i++) {
                if (!(movements[i] is Movement)) {
                    throw new Error("Движение не является объектом типа Movement.");
                }
                if (!movements[i].isFinished()) {
                    movementsInProgress.push(movements[i]);
                }
            }
            movements = movementsInProgress;
        }
        
        /**
         * Удаляет завершенные движения спрайтов.
         */
        public function removeFinishedSpriteMovements():void {
            var spriteMovementsInProgress:Array = [];
            for (var i:int = 0; i < spriteMovements.length; i++) {
                if (!(spriteMovements[i] is SpriteMovement)) {
                    throw new Error("Движение спрайта не является объектом типа SpriteMovement.");
                }
                if (!spriteMovements[i].isFinished()) {
                    spriteMovementsInProgress.push(spriteMovements[i]);
                }
            }
            spriteMovements = spriteMovementsInProgress;
        }
        
        public function delMovementByUnitId(id:int):void {
            var i:int;
            
            for (i = 0; i < movements.length; i++) {
                if (movements[i].getUnit().id == id) {
                    movements.splice(i, 1);
                    if (Config.DEBUG_MOVEMENTS) {
                        trace("Удалено движение юнита id=" + id);
                    }
                    break;
                }
            }
            for (i = 0; i < spriteMovements.length; i++) {
                if (spriteMovements[i].getUnit().id == id) {
                    spriteMovements.splice(i, 1);
                    if (Config.DEBUG_MOVEMENTS) {
                        trace("Удалено движение спрайта юнита id=" + id);
                    }
                    break;
                }
            }
        }
        
        private function cost(cur:CellA, i:int, j:int):int {
            var g:int = cur.g;
            if ( (cur.i - 1 == i && cur.j == j) || (cur.i + 1 == i && cur.j == j) || (cur.i == i && cur.j - 1 == j) || (cur.i == i && cur.j + 1 == j)) {
                g += 10
            } else {
                g += 14
            }
            return g;
        }
        
        private function heuristic(i:int, j:int, k:int, l:int):int {
            return 10 * (Math.abs(j - l) + Math.abs(i - k));
        }
        
        private function getByIndex(list:Array, i:int, j:int):CellA {
            for (var n:int = 0; n < list.length; n++) {
                if (list[n].equal(i, j)) {
                    return list[n];
                }
            }
            return null;
        }
        
        private function isInList(list:Array, i:int, j:int):Boolean {
            for (var n:int = 0; n < list.length; n++) {
                if (list[n].equal(i, j)) {
                    return true;
                }
            }
            
            return false;
        }
        
        private function getPath(beg:CellA, end:CellA):CellA {
            var NOTPASSABLE:int = 0;
            var PASSABLE:int = 1;
            var TRIGGERED:int = 2;
            var open:Array = [];
            var closed:Array = [];
            var i:int, j:int, g:int, h:int, cur:CellA, nei:CellA, n:int, rows:Array = new Array(3), cols:Array = new Array(3);
            var limit:int = 10000
            var stat:Object = {
                cur: 0,
                beg: 0,
                onmap: 0,
                geo: 0,
                close: 0,
                corner: 0
            }
            
            if (beg.j == end.j && beg.i == end.i) {
                trace("Нет смысла строить путь, когда клетка отправления и клетка назначения равны.");
                return null;
            }
            
            if (!Main.map.isOnMap(end.j, end.i) || /*accessMap[end.i][end.j] == NOTPASSABLE*/ false) {
                return null;
            }
            
            open.push(beg)
            
            while(open.length != 0 && limit-- > 0) {
                
                // Текущей будет клетка из списка открытых клеток с нимимальным f.
                cur = null;
                for (n = 0; n < open.length; n++) {
                    if (cur == null) {
                        cur = open[n];
                    } else if (open[n].f < cur.f) {
                        cur = open[n];
                    }
                }
                
                //console.log("Current cell " + cur)
                
                // Поместим текущую клтку в список закрытых и удалим из списка
                // открытых клеток.
                closed.push(cur)
                for (n = 0; n < open.length; n++) {
                    if (open[n].equal(cur.i, cur.j)) {
                        open.splice(n, 1);
                    }
                }
                
                // Обойдём всех соседей текущей клетки.
                rows[0] = cur.i - 1;
                rows[1] = cur.i;
                rows[2] = cur.i + 1;
                cols[0] = cur.j - 1;
                cols[1] = cur.j;
                cols[2] = cur.j + 1;
                for (var k:int = 0; k < rows.length; k++) {
                    for (var l:int = 0; l < cols.length; l++) {
                        i = rows[k];
                        j = cols[l];

                        // Текущую клетку не рассматриваем.
                        if (cur.equal(i, j)) {
                            stat.cur++;
                            continue;
                        }

                        // Начальную клетку не рассматриваем.
                        if (beg.equal(i, j)) {
                            stat.beg++;
                            continue;
                        }

                        // Клетку вне карты не рассматриваем.
                        if (!Main.map.isOnMap(j, i)) {
                            stat.onmap++;
                            continue;
                        }

                        // Клетку, в которую нельзя ходить, не рассматриваем.
                        if (/*accessMap[i][j] == NOTPASSABLE*/ false) {
                            stat.geo++;
                            continue;
                        }

                        // Клетку из списка закрытых не рассматриваем.
                        if (isInList(closed, i, j)) {
                            stat.close++;
                            continue;
                        }

                        // Клетку в которую из текущей можно попасть лишь срезав угол
                        // непроходимой клетки, также не рассматриваем.
                        /*if (throughCorner(cur, i, j)) {
                            stat.corner++
                            continue
                        }*/

                        if (!isInList(open, i, j)) {
                            g = cost(cur, i, j);
                            h = heuristic(i, j, end.i, end.j);
                            nei = new CellA(i, j, g, h, cur);

                            if (nei.equal(end.i, end.j)) {
                                //console.log(stat)
                                return nei;  // Путь найден!
                            } else {
                                open.push(nei);
                            }
                        } else {
                            nei = getByIndex(open, i, j);
                            g = cost(cur, nei.i, nei.j);
                            if (nei.g > g) {
                                nei.parent = cur;
                                nei.g = g;
                                nei.f = nei.g + nei.h;
                            }
                        }
                    }
                }
            }
            
            if (limit <= 0) {
                trace('Search limit reached.');
            }
            
            //console.log(stat)
            return null;    // Нет пути
        }
        
        /**
         * Создаёт путь на карте по вейпоинтам.
         */
        public function generatePath(map:Map, waypoints:Array, unit:Unit):Array {
            var generatedPath:Array = [];
            var path:CellA;
            var pathArray:Array;
            var cachedPathUsed:Boolean = false;
            
            for (var i:int = 1; i < waypoints.length; i++) {
                if (Config.usePathCache) {
                    if ( (pathArray = Main.pathCache.getCachedPath(waypoints[i - 1].j, waypoints[i - 1].i, waypoints[i].j, waypoints[i].i, unit)) == null) {
                        path = getPath(
                            new CellA(waypoints[i - 1].i, waypoints[i - 1].j, 0, 0, null),
                            new CellA(waypoints[i].i, waypoints[i].j, 0, 0, null)
                        );
                        if (path == false) {
                            trace(Main.step + ": Путь через точки "/* + waypointsToString([waypoints[i - 1], waypoints[i]])*/ + " не может быть построен.");
                            return null;
                        }
                        pathArray = convertLinkedPathToArray(path);
                        if (Config.DEBUG_MOVEMENTS) {
                            trace(Main.step + ": Для движения юнита " + unit + " по участку пути "/* + waypointsToString([waypoints[i - 1], waypoints[i]])*/ + " был создан путь "/* + pathToString(path)*/);
                        }
                        
                        Main.pathCache.cachePath(pathArray);
                    } else {
                        if (Config.DEBUG_MOVEMENTS) {
                            trace(Main.step + ": Для движения юнита " + unit + " по участку пути "/* + waypointsToString([waypoints[i - 1], waypoints[i]])*/ + " был взят из кеша путь "/* + pathToString(path)*/);
                        }
                        if (pathArray.length < 2) {
                            //trace(pathCache.toString());
                        }
                        
                        cachedPathUsed = true;
                    }
                } else {
                    path = getPath(
                        new CellA(waypoints[i - 1].i, waypoints[i - 1].j, 0, 0, null),
                        new CellA(waypoints[i].i, waypoints[i].j, 0, 0, null)
                    );
                    if (path == false) {
                        trace(Main.step + ": Путь через точки"/* + waypointsToString([waypoints[i - 1], waypoints[i]])*/ + " не может быть построен.");
                        return null;
                    }
                    pathArray = convertLinkedPathToArray(path);
                }
                if (generatedPath.length > 0) {
                    pathArray.splice(0, 1);
                }
                generatedPath = generatedPath.concat(pathArray);
            }
            
            if (cachedPathUsed) {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Для движения юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/ + " был сформирован путь частично или полностью взятый из кеша "/* + pathToString(generatedPath)*/);
                }
            } else {
                if (Config.DEBUG_MOVEMENTS) {
                    trace(Main.step + ": Для движения юнита " + unit + " по точкам "/* + waypointsToString(waypoints)*/ + " был создан путь "/* + pathToString(generatedPath)*/);
                }
            }
            
            return generatedPath;
        }
        
        /**
         * Преобразовывает путь-список, возвращаемый функцией getPath(), в путь массив
         * точек. Требует для работы класс Point и Cell.
         */
        private function convertLinkedPathToArray(path:CellA):Array {
            var arrayPath:Array = new Array();
            var curCell:CellA = path;
            
            do {
                arrayPath.push(new Point(curCell.j, curCell.i));
                curCell = curCell.parent;
            } while (curCell != null);
            
            return arrayPath.reverse();
        }
        
        public static function getMovementOrientation(begJ:int, begI:int, endJ:int, endI:int):String {
            var jDiff:int = endJ - begJ;
            var iDiff:int = endI - begI;
            
            if (jDiff == 0 && iDiff == -1) {
                return "N";
            } else if (jDiff == 1 && iDiff == -1) {
                return "NE";
            } else if (jDiff == 1 && iDiff == 0) {
                return "E";
            } else if (jDiff == 1 && iDiff == 1) {
                return "SE";
            } else if (jDiff == 0 && iDiff == 1) {
                return "S";
            } else if (jDiff == -1 && iDiff == 1) {
                return "SW";
            } else if (jDiff == -1 && iDiff == 0) {
                return "W";
            } else if (jDiff == -1 && iDiff == -1) {
                return "NW";
            } else {
                return null;
            }
        }
        
        public function removeFollowing(object:Unit):void {
            for (var i:int = 0; i < followings.length; i++) {
                if (followings[i].getObject() == object) {
                    followings.splice(i, 1);
                    break;
                }
            }
        }
        
        public function removeFollowingByTarget(target:Unit):void {
            var found:Boolean = false;
            do {
                for (var i:int = 0; i < followings.length; i++) {
                    if (followings[i].getTarget() == target) {
                        followings.splice(i, 1);
                        found = true;
                        break;
                    }
                }
                found = false;
            } while (found);
        }
        
        public function addFollowing(object:Unit, target:Unit):Boolean {
            for (var i:int = 0; i < followings.length; i++) {
                if (followings[i].getObject() == object) {
                    if (Config.DEBUG_FOLLOW) {
                        trace(Main.step + ": Юнит " + object + " изменил цель преследования c " + followings[i].getTarget() + " на " + target);
                    }
                    followings[i].setTarget(target);
                    Main.freeAnimation.push(new FollowBlinkAnimation(target));
                    return true;
                }
            }
            
            followings.push(new Follow(object, target));
            if (Config.DEBUG_FOLLOW) {
                trace(Main.step + ": Юнит " + object + " начал преследовать цель " + target);
            }
            Main.freeAnimation.push(new FollowBlinkAnimation(target));
            return true;
        }
        
        public function getFollowings():Array {
            return followings;
        }
    }
}
