
// Различные функции.
// requied Constants.js

// http://stackoverflow.com/questions/2936112/text-wrap-in-a-canvas-element
// Разбивает фразу на отдельные слова и возвращает массив подфраз фразы phrase,
// влезающих в ширину maxPxLength при выводе на экран.
function getLines(ctx, phrase, maxPxLength, textFont) {
    var wa = phrase.split(" "),
        phraseArray = [],
        lastPhrase = "",
        l = maxPxLength,
        measure = 0;   // Length of word tail in percent.
    ctx.font = textFont;
    for (var i = 0; i < wa.length; i++) {
        var w = wa[i];
        measure = ctx.measureText(lastPhrase + w).width;
        if (measure < l) {
            lastPhrase += (w + " ");
        } else {
            phraseArray.push(lastPhrase);
            lastPhrase = w;
        }
        if (i === wa.length - 1) {
            phraseArray.push(lastPhrase);
            break;
        }
    }
    return phraseArray;
}

// Скрывает форму ввода логина и пароля для входа в игру.
function hideLoginForm() {
    document.getElementById('login_form').style.visibility = 'hidden';
}
// Отображает форму ввода логина и пароля для входа в игру.
function showLoginForm() {
    var login_form = document.getElementById('login_form');

    // Сделаем форму видимой и отцентируем форму логина по середине конвы.
    login_form.style.visibility = 'visible';
    login_form.style.left = (SCR_W / 2 - login_form.offsetWidth / 2) + "px";
    login_form.style.top = (SCR_H / 2  - login_form.offsetHeight / 2) + "px";
}
/**
 * Возвращает логин, введённый в форме ввода логина пароля.
 */
function getLogin() {
    return document.getElementById('login').value
}
/**
 * Возвращает пароль, введённый в форме ввода логина пароля.
 */
function getPassword() {
    return document.getElementById('password').value
}

// Поиск пути на карте.
function Cell(i, j, g, h, parent) {
    this.i = i
    this.j = j
    this.f = g + h
    this.g = g
    this.h = h
    this.parent = parent
    this.equal = function(i, j) {
        return this.i == i && this.j == j
    }
    this.toString = function() {
        return "[" + this.j + ", " + this.i + "]"
    }
    return this
}
function cost(cur, i, j) {
    var g = cur.g
    if ( (cur.i - 1 == i && cur.j == j)
            || (cur.i + 1 == i && cur.j == j)
            || (cur.i == i && cur.j - 1 == j)
            || (cur.i == i && cur.j + 1 == j)) {

        g += 10
    } else {
        g += 14
    }
    return g
}
function heuristic(i, j, k, l) {
    return 10 * (Math.abs(j - l) + Math.abs(i - k));
}
function getByIndex(list, i, j) {
    for (var n = 0; n < list.length; n++)
        if (list[n].i == i && list[n].j == j)
            return list[n]
    return null
}
function isInList(list, i, j) {
    for (var n = 0; n < list.length; n++)
        if (list[n].i == i && list[n].j == j)
            return true
    return false
}
function throughCorner(cur, i, j) {
    var NOTPASSABLE = 0;
    var PASSABLE = 1;
    var TRIGGERED = 2;

    // Клетки не лежат на диагонали
    if (cur.i - i == 0 || cur.j - j == 0) {
        return false
    }
    
    return false

    //[c][ ]
    //[ ][n]
    if (i > cur.i && j > cur.j) {
        return accessMap[i - 1][j] == NOTPASSABLE
                || accessMap[i][j - 1] == NOTPASSABLE;
    }

    //[n][ ]
    //[ ][c]
    if (i < cur.i && j < cur.j) {
        return accessMap[i + 1][j] == NOTPASSABLE
                || accessMap[i][j + 1] == NOTPASSABLE;
    }

    //[ ][n]
    //[c][ ]
    if (i < cur.i && j > cur.j) {
        return accessMap[i + 1][j] == NOTPASSABLE
                || accessMap[i][j - 1] == NOTPASSABLE;
    }

    //[ ][c]
    //[n][ ]
    if (i > cur.i && j < cur.j) {
        return accessMap[i - 1][j] == NOTPASSABLE
                || accessMap[i][j + 1] == NOTPASSABLE;
    }

    return false
}
function getPath(beg, end) {
    var NOTPASSABLE = 0;
    var PASSABLE = 1;
    var TRIGGERED = 2;
    var open = new Array()
    var closed = new Array()
    var i, j, g, h, cur, nei, n, rows = new Array(3), cols = new Array(3)
    var limit = 10000
    var stat = {
        cur: 0,
        beg: 0,
        onmap: 0,
        geo: 0,
        close: 0,
        corner: 0
    }
    
    if (beg.j == end.j && beg.i == end.i) {
        console.log("Нет смысла строить путь, когда клетка отправления и клетка назначения равны.");
        return false;
    }

    if (!map.isOnMap(end.j, end.i) || /*accessMap[end.i][end.j] == NOTPASSABLE*/ false) {
        return false
    }

    open.push(beg)

    while(open.length != 0 && limit-- > 0) {

        // Текущей будет клетка из списка открытых клеток с нимимальным f.
        cur = null
        for (n = 0; n < open.length; n++) {
            if (cur == null) {
                cur = open[n]
            } else if (open[n].f < cur.f) {
                cur = open[n]
            }
        }

        //console.log("Current cell " + cur)

        // Поместим текущую клтку в список закрытых и удалим из списка
        // открытых клеток.
        closed.push(cur)
        for (n = 0; n < open.length; n++) {
            if (open[n].equal(cur.i, cur.j)) {
                open.splice(n, 1)
            }
        }

        // Обойдём всех соседей текущей клетки.
        rows[0] = cur.i - 1
        rows[1] = cur.i
        rows[2] = cur.i + 1
        cols[0] = cur.j - 1
        cols[1] = cur.j
        cols[2] = cur.j + 1
        for (var k in rows) {
            for (var l in cols) {
                i = rows[k]
                j = cols[l]

                // Текущую клетку не рассматриваем.
                if (cur.equal(i, j)) {
                    stat.cur++
                    continue
                }

                // Начальную клетку не рассматриваем.
                if (beg.equal(i, j)) {
                    stat.beg++
                    continue
                }

                // Клетку вне карты не рассматриваем.
                if (!map.isOnMap(j, i)) {
                    stat.onmap++
                    continue
                }

                // Клетку, в которую нельзя ходить, не рассматриваем.
                if (/*accessMap[i][j] == NOTPASSABLE*/ false) {
                    stat.geo++
                    continue
                }

                // Клетку из списка закрытых не рассматриваем.
                if (isInList(closed, i, j)) {
                    stat.close++
                    continue
                }

                // Клетку в которую из текущей можно попасть лишь срезав угол
                // непроходимой клетки, также не рассматриваем.
                if (throughCorner(cur, i, j)) {
                    stat.corner++
                    continue
                }

                if (!isInList(open, i, j)) {
                    g = cost(cur, i, j)
                    h = heuristic(i, j, end.i, end.j)
                    nei = new Cell(i, j, g, h, cur)

                    if (nei.equal(end.i, end.j)) {
                        //console.log(stat)
                        return nei  // Путь найден!
                    } else {
                        open.push(nei)
                    }
                } else {
                    nei = getByIndex(open, i, j)
                    g = cost(cur, nei.i, nei.j)
                    if (nei.g > g) {
                        nei.parent = cur
                        nei.g = g
                        nei.f = nei.g + nei.h
                    }
                }
            }
        }
    }

    if (limit <= 0) {
        alert('Search limit reached.')
    }

    //console.log(stat)
    return false    // Нет пути
}

// Отображает форму чата.
function showChat() {
    var chat = document.getElementById('chat');

    // Сделаем форму видимой и отцентируем форму логина по середине конвы.
    chat.style.visibility = 'visible';
    chat.style.left = (SCR_W + 20) + "px";
    chat.style.top = (20) + "px";
    document.getElementById("chat_bar_common").style.color = "blue"

    chatToggle(cur_chat_type)
}
// Выводит содержимое чата.
function chatToggle(type) {
    var c, e,
        t = document.getElementById("chat_field"),
        input = document.getElementById("chat_input")

    switch (type) {
        case 0:
            e = document.getElementById("chat_bar_private")
            c = private_chat
            cur_chat_type = 0

            e.style.color = "blue"
            document.getElementById("chat_bar_common").style.color = "black"
            break
        case 1:
            e = document.getElementById("chat_bar_common")
            c = common_chat
            cur_chat_type = 1

            e.style.color = "blue"
            document.getElementById("chat_bar_private").style.color = "black"
            break
    }

    var tmp = ""
    for (var i in c) {
        tmp += c[i].name + ": " + c[i].message + "\n"
    }
    t.value = tmp
    setCaretToPos(t, t.value.length)
    setCaretToPos(input, input.value.length)
}
// Отправляет сообщение на сервер.
function chatSend() {
    var input = document.getElementById("chat_input")
        
    if (input.value != "") {
        server.sendMessage(new NewchatMessage(input.value))
        setCaretToPos(input, input.value.length)
        input.value = ""
    }
}
// http://stackoverflow.com/questions/499126/jquery-set-cursor-position-in-text-area
function setSelectionRange(input, selectionStart, selectionEnd) {
  if (input.setSelectionRange) {
    input.focus();
    input.setSelectionRange(selectionStart, selectionEnd);
  }
  else if (input.createTextRange) {
    var range = input.createTextRange();
    range.collapse(true);
    range.moveEnd('character', selectionEnd);
    range.moveStart('character', selectionStart);
    range.select();
  }
}
function setCaretToPos(input, pos) {
    setSelectionRange(input, pos, pos);
}

// Возвращает id спрайта по его серверному имени.
function getSpriteIdByServerName(sname) {
    for (var id in SPRITES) {
        if (SPRITES[id].sname == sname) {
            return id
        }
    }

    throw new Error("Спрайт с именем " + sname + " не найден.")
}

/**
 * Обработчик события загрузки объектов карты.
 */
function onLoadMapObjects(event) {
    var tmp, tiles

    if (MAPOBJECTS == undefined) {
        throw new Error("Объекты карты не были загружены.")
    }

    tmp = {}
    for (var obj in MAPOBJECTS) {
        tiles = MAPOBJECTS[obj].tiles
        tmp[obj] = {}
        tmp[obj].tiles = new Array(tiles.length)
        for (var i = 0; i < tiles.length; i++) {
            tmp[obj].tiles[i] = new Array()
            for (var j = 0; j < tiles[i].length; j++) {
                if (tiles[i][j] == null) {
                    tmp[obj].tiles[i].push(null)
                } else {
                    tmp[obj].tiles[i].push({
                        sprId: getSpriteIdByServerName(tiles[i][j].src),
                        acc: tiles[i][j].acc
                    })
                }
            }
        }
    }
    console.log("Загрузка объектов карты завершена.")

    MAPOBJECTS = tmp
}

/**
 * Обработчик события загрузки тайлов земли.
 */
function onLoadGroundTiles(event) {
    var tmp

    if (GROUNDS == undefined) {
        throw new Error("Тайлы земли не были загружены.")
    }

    tmp = {}
    for (var id in GROUNDS) {
        tmp[id] = {
            sprId: getSpriteIdByServerName(GROUNDS[id].src),
            acc: GROUNDS[id].acc
        }
    }
    console.log("Загрузка тайлов земли завершена.")

    GROUNDS = tmp
}

/**
 * Обработчик события загрузки карты.
 * @param mapId Идентификатор карты.
 */
function onLoadMap(mapId) {
    if (maps[mapId] != undefined) {
        console.warn("Попытка повторной загрузки карты " + mapId + ".")
        return
    }
    if (MAPS[mapId] == undefined) {
        throw new Error("Карта " + mapId + " не была загружена.")
    }

    var i, j, k, obj, tiles

    var mapH = MAPS[mapId].tiles.length
    var mapW = MAPS[mapId].tiles[0].length
    var grnd = MAPS[mapId].tiles
    var objs = MAPS[mapId].objects
    var sloader = new SpriteLoader(SPRITES)
    var spriteMap = new Array(mapH)
    var accessMap = new Array(mapH)
    var eventMap = new Array(mapH)

    for (i = 0; i < mapH; i++) {
        spriteMap[i] = new Array(mapW)
        accessMap[i] = new Array(mapW)
        eventMap[i] = new Array(mapW)
        for (j = 0; j < mapW; j++) {
            spriteMap[i][j] = {
                ground: sloader.getSprite(GROUNDS[grnd[i][j]].sprId),
                objs: new Array()
            }
            accessMap[i][j] = parseInt(GROUNDS[grnd[i][j]].acc)
        }
    }
    for (k in objs) {
        obj = objs[k]
        tiles = MAPOBJECTS[obj.type].tiles
        for (i in tiles) {
            i = parseInt(i)
            for (j in tiles[i]) {
                j = parseInt(j)
                if (tiles[i][j] != null) {
                    spriteMap[obj.y + i][obj.x + j].objs.push(sloader.getSprite(tiles[i][j].sprId))
                    accessMap[obj.y + i][obj.x + j] = parseInt(tiles[i][j].acc)
                    if (tiles[i][j].acc == 2) {
                        var _id = obj.objid
                        eventMap[obj.y + i][obj.x + j] = function() {return function(id) {console.log("Entering map object " + id + ".")}(_id)}
                    }
                }
            }
        }
    }

    maps[mapId] = new Map(mapId, mapW, mapH, sloader, spriteMap, accessMap,
                          eventMap)
    console.log("Загрузка карты %s (%sx%s) завершена.", mapId, mapW, mapH)
}

function freeCameraToggle() {
    if (is_camera_free) {
        camera.target = self_player
        is_camera_free = false
    } else {
        delete camera.target
        is_camera_free = true
    }
}

function createSelectionRect(x1, y1, x2, y2) {
    var rect = {
        beg: {x: Math.min(x1, x2), y: Math.min(y1, y2), j: null, i: null},
        end: {x: Math.max(x1, x2), y: Math.max(y1, y2), j: null, i: null},
        draw: function(context, camera) {
            context.beginPath();
            
            context.moveTo(this.beg.x, this.beg.y);
            context.lineTo(this.end.x, this.beg.y);
            context.lineTo(this.end.x, this.end.y);
            context.lineTo(this.beg.x, this.end.y);
            context.lineTo(this.beg.x, this.beg.y);
            
            context.closePath();
            context.stroke();
        }
    };
    var tAxis1 = camera.reTranslateAxis(rect.beg.x, rect.beg.y);
    var tAxis2 = camera.reTranslateAxis(rect.end.x, rect.end.y);
    rect.beg.j = Map.getCellJOnMap(tAxis1.x);
    rect.beg.i = Map.getCellJOnMap(tAxis1.y);
    rect.end.j = Map.getCellJOnMap(tAxis2.x);
    rect.end.i = Map.getCellJOnMap(tAxis2.y);
    
    return rect;
}

function getNeighboursCells(cells) {
    var neighbours = {};
    var checkCell = function (neighbours, _j, _i) {
        if ((neighbours[_i] == undefined || neighbours[_i][_j] == undefined) && map.isOnMap(_j, _i)) {
            if (neighbours[_i] == undefined) {
                neighbours[_i] = {};
            }
            neighbours[_i][_j] = new Point(_j, _i);
        }
    }
    var i, j, result = cells;
    for (i in cells) {
        checkCell(neighbours, cells[i].getJ() - 1, cells[i].getI() - 1);
        checkCell(neighbours, cells[i].getJ()    , cells[i].getI() - 1);
        checkCell(neighbours, cells[i].getJ() + 1, cells[i].getI() - 1);
        checkCell(neighbours, cells[i].getJ() - 1, cells[i].getI());
        checkCell(neighbours, cells[i].getJ() + 1, cells[i].getI());
        checkCell(neighbours, cells[i].getJ() - 1, cells[i].getI() + 1);
        checkCell(neighbours, cells[i].getJ()    , cells[i].getI() + 1);
        checkCell(neighbours, cells[i].getJ() + 1, cells[i].getI() + 1);
    }
    for (i in neighbours) {
        for (j in neighbours[i]) {
            result.push(neighbours[i][j]);
        }
    }
    return result;
}

function getUnitById(id) {
    for (var i in self_units_list) {
        if (self_units_list[i].getId() == id) {
            return self_units_list[i];
        }
    }
    for (i in units_list) {
        if (units_list[i].getId() == id) {
            return units_list[i];
        }
    }
    
    return null;
}

function delUnitById(id) {
    for (var i in self_units_list) {
        if (self_units_list[i].getId() == id) {
            self_units_list.splice(i, 1);
            movementManager.delMovementByUnitId(id);
            removeFollowingByTarget(self_units_list[i]);
            return true;
        }
    }
    for (i in units_list) {
        if (units_list[i].getId() == id) {
            units_list.splice(i, 1);
            movementManager.delMovementByUnitId(id);
            removeFollowingByTarget(units_list[i]);
            return true;
        }
    }
    
    return false;
}

function toggleSmoothMovement() {
    smoothMovementEnabled = smoothMovementEnabled ? false : true;
    document.getElementById('toggleSmoothMovement').checked = smoothMovementEnabled;
}

function toggleCamFollow() {
    camFollowEnabled = camFollowEnabled ? false : true;
    document.getElementById('toggleCamFollow').checked = camFollowEnabled;
            
    if (camFollowEnabled && selected_units.length > 0 && camera != undefined && !camera.isFollow()) {
        camera.follow(selected_units[0]);
    } else if (camera != undefined && camera.isFollow()) {
        camera.unfollow();
    }
}

/**
 * Возвращает случайное число в диапазоне [0, n]
 */
function getRandomInt(n) {
    return Math.floor(Math.random() * (n + 1));
}

function setPixel(imageData, x, y, r, g, b, a) {
    var index = (x + y * imageData.width) * 4;
    imageData.data[index+0] = r;
    imageData.data[index+1] = g;
    imageData.data[index+2] = b;
    imageData.data[index+3] = a;
}

function clearTimeouts() {
    clearTimeout(aiTimer);
    clearTimeout(reconnectTimer);
    clearTimeout(connectTimer);
    clearTimeout(followingsTimer);
}

/**
 * Преобразовывает путь-список, возвращаемый функцией getPath(), в путь массив
 * точек. Требует для работы класс Point и Cell.
 */
function convertLinkedPathToArray(path) {
    if (!(path instanceof Cell)) {
        throw new Error("Путь не является объектом типа Cell.");
    }
    
    var arrayPath = new Array();
    var curCell = path;

    do {
        arrayPath.push(new Point(curCell.j, curCell.i));
        curCell = curCell.parent;
    } while (curCell != null);

    return arrayPath.reverse();
}

/**
 * Проверяет, является ли объект массивом.
 */
function isArray(obj) {
    return obj.constructor == Array;
}

/**
 * Преобразует путь массив точек в строку. Требует для работы класс Point.
 */
function pathToString(path) {
    if (!(path instanceof Array)) {
        throw new Error("Путь не является массивом.");
    }

    var out = "";

    for (var i = 0; i < path.length; i++) {
        if (!(path[i] instanceof Point)) {
            throw new Error("Точка пути не является объектом типа Point.");
        }
        out += path[i];
    }

    return out;
}

/**
 * Создаёт путь на карте по вейпоинтам.
 */
function generatePath(map, waypoints, unit) {
    if (!(map instanceof Map)) {
        throw new Error("Карта не является объектом типа Map.");
    }
    if (!(waypoints instanceof Array)) {
        throw new Error("Вейпоинты не является массивом.");
    }
    
    var generatedPath = new Array();
    var path;
    var cachedPathUsed = false;

    for (var i = 1; i < waypoints.length; i++) {
        if (usePathCache) {
            if ( (path = pathCache.getCachedPath(waypoints[i - 1].j, waypoints[i - 1].i, waypoints[i].j, waypoints[i].i, unit)) == null) {
        path = getPath(
            new Cell(waypoints[i - 1].i, waypoints[i - 1].j, 0, 0, null),
            new Cell(waypoints[i].i, waypoints[i].j, 0, 0, null)
        );
        if (path == false) {
                    console.log(step + ": Путь через точки " + waypointsToString([waypoints[i - 1], waypoints[i]]) + " не может быть построен.");
            return null;
        }
        path = convertLinkedPathToArray(path);
                if (DEBUG_MOVEMENTS) {
                    console.log(step + ": Для движения юнита " + unit + " по участку пути " + waypointsToString([waypoints[i - 1], waypoints[i]]) + " был создан путь " + pathToString(path));
                }
                
                pathCache.cachePath(path);
            } else {
                if (DEBUG_MOVEMENTS) {
                    console.log(step + ": Для движения юнита " + unit + " по участку пути " + waypointsToString([waypoints[i - 1], waypoints[i]]) + " был взят из кеша путь " + pathToString(path));
                }
                if (path.length < 2) {
                    console.log(pathCache.toString());
                }
                
                cachedPathUsed = true;
            }
        } else {
            path = getPath(
                new Cell(waypoints[i - 1].i, waypoints[i - 1].j, 0, 0, null),
                new Cell(waypoints[i].i, waypoints[i].j, 0, 0, null)
            );
            if (path == false) {
                console.log(step + ": Путь через точки" + waypointsToString([waypoints[i - 1], waypoints[i]]) + " не может быть построен.");
                return null;
            }
            path = convertLinkedPathToArray(path);
        }
        if (generatedPath.length > 0) {
            path.splice(0, 1);
        }
        generatedPath = generatedPath.concat(path);
    }

    if (cachedPathUsed) {
        if (DEBUG_MOVEMENTS) {
            console.log(step + ": Для движения юнита " + unit + " по точкам " + waypointsToString(waypoints) + " был сформирован путь частично или полностью взятый из кеша " + pathToString(generatedPath));
        }
    } else {
        if (DEBUG_MOVEMENTS) {
            console.log(step + ": Для движения юнита " + unit + " по точкам " + waypointsToString(waypoints) + " был создан путь " + pathToString(generatedPath));
        }
    }

    return generatedPath;
}

function connect() {
    server = new ServerInteraction();
    server.connect("ws://localhost:8015/");
}

function disconnect() {
    if (server != undefined && server.socket != null) {
        server.socket.close();
    }
}

function syncLoop() {
    var i;
    
    if (stepBegTime != null) {
        prevStepTime = (new Date()).getTime() - stepBegTime;
    }
    stepBegTime = (new Date()).getTime();
    
    step++;
    
    movementManager.updatePositions(step);

    var unitsCount = self_units_list.length + units_list.length;
    var unitsString = "";
    for (i = 0; i < self_units_list.length; i++) {
        unitsString += self_units_list[i].toProtocol1() + ",";
    }
    for (i = 0; i < units_list.length; i++) {
        unitsString += units_list[i].toProtocol1() + ",";
    }
    if (self_units_list.length > 0 || units_list.length > 0) {
        unitsString = unitsString.substring(0, unitsString.length - 1);
    }
    var actions = actionManager.takeMovementRequests();
    var actionsCount = actions.length;
    var actionsString = "";
    for (i = 0; i < actions.length; i++) {
        actionsString += actions[i].toProtocol1() + ",";
    }
    if (actions.length > 0) {
        actionsString = actionsString.substring(0, actionsString.length - 1);
    }
    server.sendMessage(new StateReportMessage(step, unitsCount, unitsString, actionsCount, actionsString).toProtocol1());
}

function pingLoop() {
    server.sendMessage(new PingMessage((new Date()).getTime()).toProtocol1());
    pingTimer = setTimeout(pingLoop, 100);
}

function createId() {
    return createId.id = createId.id == undefined ? 0 : createId.id + 1;
}

function clonePath(path) {
    if (!(path instanceof Array)) {
        throw new Error("Путь не является массивом.");
    }

    var clone = new Array();
    for (var i = 0; i < path.length; i++) {
        if (!(path[i] instanceof Point)) {
            throw new Error("Точка пути не является объектом типа Point.");
        }

        clone.push(path[i]);
    }
    return clone;
}

function waypointsToString(waypoints) {
    var out = "";

    for (var i = 0; i < waypoints.length - 1; i++) {
        out += "[" + waypoints[i].j + "," + waypoints[i].i + "]->";
    }
    if (waypoints.length > 0) {
        out += "[" + waypoints[waypoints.length - 1].j + "," + waypoints[waypoints.length - 1].i + "]";
    }

    return out;
}

function cloneArray(array) {
    var clone = [];
    for (var i = 0; i < array.length; i++) {
        clone.push(array[i]);
    }
    return clone;
}

function removeByValue(array, value) {
    for (var i = 0; i < array.length; i++) {
        if (array[i] == value) {
            //var old = cloneArray(array);
            array.splice(i, 1)
            //var _new = cloneArray(array);
            //console.log(old, value, i, _new)
            //return _new;
            return array;
        }
    }
    
    return array;
}

function indexOf(array, value) {
    for (var i = 0; i < array.length; i++) {
        if (array[i] == value) {
            return i;
        }
    }
    
    return -1;
}

function followLoop() {
    var object;
    var target;
    var objectDest;
    var targetPos;
    
    for (var i = 0; i < followings.length; i++) {
        object = followings[i].getObject();
        target = followings[i].getTarget();
        if ( (objectDest = followings[i].getObjectDest()) == null) {
            objectDest = new Point(object.getJ(), object.getI());
        }
        targetPos = new Point(target.getJ(), target.getI());
        
        if (!targetPos.equals(objectDest)) {
            actionManager.moveUnit(object, targetPos);
            followings[i].setObjectDest(targetPos);
        }
    }
    
    followingsTimer = setTimeout(followLoop, 500);
}

function removeFollowing(object) {
    for (var i = 0; i < followings.length; i++) {
        if (followings[i].getObject() == object) {
            followings.splice(i, 1);
            break;
        }
    }
    followingEnabled = false;
}

function removeFollowingByTarget(target) {
    var found = false;
    do {
        for (var i = 0; i < followings.length; i++) {
            if (followings[i].getTarget() == target) {
                followings.splice(i, 1);
                found = true;
                break;
            }
        }
        found = false;
    } while (found);
    followingEnabled = false;
}

function addFollowing(object, target) {
    for (var i = 0; i < followings.length; i++) {
        if (followings[i].getObject() == object) {
            if (DEBUG_FOLLOW) {
                console.log(step + ": Юнит " + object + " изменил цель преследования c " + followings[i].getTarget() + " на " + target);
            }
            followings[i].setTarget(target);
            return true;
        }
    }
    
    followings.push(new Follow(object, target));
    if (DEBUG_FOLLOW) {
        console.log(step + ": Юнит " + object + " начал преследовать цель " + target);
    }
    return true;
}

function reconnect() {
    if (server != undefined && server.socket != null) {
        server.socket.close()
    } else {
        console.log("Отключение от сервера не удалось: север не создан или сокет не инициализирован.")
    }
    
    connectTimer = setTimeout(connect, 2000);
}

function aiLoop() {
    if (followProbability != null && getRandomInt(followProbability) == 0 && units_list.length > 0) {
        for (var i = 0; i < self_units_list.length; i++) {
            addFollowing(self_units_list[i], units_list[getRandomInt(units_list.length - 1)]);
        }
        followingEnabled = true;
    }
    if (!followingEnabled) {
        actionManager.createRandomMovementRequests();
    }
    if (reconnectProbability != null && getRandomInt(reconnectProbability) == 0) {
        reconnectTimer = setTimeout(reconnect, 2000);
        console.log("Переподключение.");
    } else {
        aiTimer = setTimeout(aiLoop, 2000);
    }
}
