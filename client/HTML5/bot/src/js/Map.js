
// Карта.
// Require: resources.js, SpriteLoader.js, constants.js

// Карта игрового мира.
function Map(width, height) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    Map.DEBUG_DRAW = false;

    /**
     * Ширина карты в клетках.
     */
    this.w = width;

    /**
     * Высота карты в клетках.
     */
    this.h = height;

    /**
     * Загрузчик спрайтов.
     */
    this.sloader = new SpriteLoader(SPRITES);

    /**
     * Карта спрайтов.
     */
    this.spriteMap = new Array(this.h);
    for (var i = 0; i < this.h; i++) {
        this.spriteMap[i] = new Array(this.w)
        for (var j = 0; j < this.w; j++) {
            this.spriteMap[i][j] = null;
        }
    }

    var tmpCanvas = document.createElement('canvas');
    tmpCanvas.width = width;
    tmpCanvas.height = height;
    this.miniMap = (tmpCanvas.getContext("2d")).createImageData(tmpCanvas.width, tmpCanvas.height);
    
    var MACROBLOCK_W = 20;
    var MACROBLOCK_H = 20;
    var macromap_h = Math.ceil(this.h / MACROBLOCK_H);
    var macromap_w = Math.ceil(this.w / MACROBLOCK_W);
    this.macromap = [];
    var macromap = this.macromap;
    var macroblock_context;
    var dim = {j_from: null, j_to: null, i_from: null, i_to: null};
    for (i = 0; i < macromap_h; i++) {
        macromap[i] = [];
        for (j = 0; j < macromap_w; j++) {
            macromap[i][j] = document.createElement('canvas');
            macromap[i][j].width = MACROBLOCK_W * CELL_W;
            macromap[i][j].height = MACROBLOCK_H * CELL_H;
            macroblock_context = macromap[i][j].getContext("2d");
            
            dim.j_from = j * MACROBLOCK_W;
            dim.j_to = dim.j_from + MACROBLOCK_W;
            dim.i_from = i * MACROBLOCK_H;
            dim.i_to = dim.i_from + MACROBLOCK_H;
            
            var x = 0;
            var y = 0;
            for (var k = dim.i_from; k < dim.i_to; k++) {
                for (var l = dim.j_from; l < dim.j_to; l++) {
                    if (this.spriteMap[k][l] != undefined) {
                        macroblock_context.drawImage(this.spriteMap[k][l].ground, x, y);
                        if (Map.DEBUG_DRAW) {
                            macroblock_context.drawImage(this.sloader.getSpriteByName("grass_border").getImage(), x, y);
                            macroblock_context.fillText(k + "," + l, x + 3, y + 15);
                        }
                        //x += this.sloader.getSpriteByName("grass").w;
                    }
                }
                x = 0;
                //y += this.sloader.getSpriteByName("grass").h;
            }
            
            if (Map.DEBUG_DRAW) {
                macroblock_context.beginPath();
                macroblock_context.moveTo(0, 0);
                macroblock_context.lineTo(MACROBLOCK_W * CELL_W - 1, 0);
                macroblock_context.lineTo(MACROBLOCK_W * CELL_W - 1, MACROBLOCK_H * CELL_H - 1);
                macroblock_context.lineTo(0, MACROBLOCK_H * CELL_H - 1);
                macroblock_context.lineTo(0, 0);
                macroblock_context.closePath();
                macroblock_context.lineWidth = 1;
                macroblock_context.strokeStyle = "blue";
                macroblock_context.stroke();
            }
        }
    }

    /**
     * Метод рисования карты.
     */
    this.draw = function(context, camera) {
        if (camera == undefined || camera == null) {
            console.error("У карты не установлена камера! Карта не может быть отображена.")
            return
        }

        // Вычислим, какие клетки карты помещаются на экран при текущем
        // положении нашего игрока. Например если наш игрок находится в клетке
        // 1, 1 то нам нужно нарисовать все клетки которые помещаются на экран
        // с клеткой 1, 1 в центре экрана.

        var MACROBLOCK_W = 20;
        var MACROBLOCK_H = 20;
        var macromap_h = Math.ceil(this.h / MACROBLOCK_H);
        var macromap_w = Math.ceil(this.w / MACROBLOCK_W);

        var j_from
        if (camera.x < 0) {
            j_from = 0;
        } else {
            j_from = Map.getMacroBlockJOnMap(camera.x);
        }

        var j_to
        if (camera.x + SCR_W >= this.w * CELL_W) {
            j_to = macromap_w - 1;
        } else {
            j_to = Map.getMacroBlockJOnMap(camera.x + SCR_W);
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellJOnMap можем случайно вылететь за края карты.
        if (j_to >= macromap_w) {
            j_to = macromap_w - 1;
        }

        var i_from = Map.getMacroBlockIOnMap(camera.y)
        if (camera.y < 0) {
            i_from = 0
        } else {
            i_from = Map.getMacroBlockIOnMap(camera.y)
        }

        var i_to = Map.getMacroBlockIOnMap(camera.y + SCR_H)
        if (camera.y + SCR_H >= this.h * CELL_H) {
            i_to = macromap_h - 1
        } else {
            i_to = Map.getMacroBlockIOnMap(camera.y + SCR_H)
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellIOnMap можем случайно вылететь за края карты.
        if (i_to >= macromap_h) {
            i_to = macromap_h - 1;
        }

        var i, j,
            tAxis   // Translated axis. Map -> Camera.

        // Нарисуем клетки вычисленного региона карты.
        for (i = i_from; i <= i_to; i++) {
            for (j = j_from; j <= j_to; j++) {
                tAxis = camera.translateAxis(Map.getMacroBlockXOnMap(j), Map.getMacroBlockYOnMap(i));
                context.drawImage(this.macromap[i][j], tAxis.x, tAxis.y);
            }
        }
    }

    /**
     * Проверяет, принадлежит ли клетка карте.
     * @param j j-индекс клетки.
     * @param i i-индекс клетки.
     */
    this.isOnMap = function(j, i) {
        return i >= 0 && j >= 0 && i < this.h && j < this.w
    }

    /**
     * Перерисовывает миникарту.
     */
    this.updateMinimap = function() {
        var i, j, k;
        for (i = 0; i < this.h; i++) {
            for (j = 0; j < this.w; j++) {
                if (i == 0 || j == 0 || i == this.h - 1 || j == this.w - 1) {
                    // Черная рамка по краям.
                    setPixel(this.miniMap, j, i, 0, 0, 0, 255);
                } else {
                    // Остальное зелёное.
                    setPixel(this.miniMap, j, i, 0, 255, 0, 255);
                }
            }
        }
        for (k = 0; k < self_units_list.length; k++) {
            setPixel(this.miniMap, self_units_list[k].j, self_units_list[k].i, 0, 0, 255, 255);
        }
        for (k = 0; k < units_list.length; k++) {
            setPixel(this.miniMap, units_list[k].j, units_list[k].i, 255, 0, 0, 255);
        }
        var camJ = Math.max(0, Map.getCellJOnMap(camera.x));
        var camI = Math.max(0, Map.getCellIOnMap(camera.y));
        //var camW = Math.min(map.w, Math.ceil(SCR_W / CELL_W));
        //var camH = Math.min(map.h, Math.ceil(SCR_H / CELL_H));
        var camW = Math.ceil(SCR_W / CELL_W);
        var camH = Math.ceil(SCR_H / CELL_H);
        for (i = camI; i < map.h; i++) {
            for (j = camJ; j < map.w; j++) {
                if ( ((i == camI || i == camI + camH - 1) && j <= camJ + camW - 1) || (j == camJ || j == camJ + camW - 1) && i <= camI + camH - 1) {
                    // Серая рамка камеры.
                    setPixel(this.miniMap, j, i, 128, 128, 128, 255);
                }
            }
        }
    }

    return this
}

/**
 * Возвращает координату x карты, которую имеет клетка с индексом j.
 */
Map.getCellXOnMap = function(j) {
    return j * CELL_W
};

/**
 * Возвращает координату y карты, которую имеет клетка с индексом i.
 */
Map.getCellYOnMap = function(i) {
    return i * CELL_H
};

/**
 * Возвращает координату x карты, которую имеет макроблок с индексом j.
 */
Map.getMacroBlockXOnMap = function(j) {
    var MACROBLOCK_W = 20;
    return j * (MACROBLOCK_W * CELL_W);
};

/**
 * Возвращает координату y карты, которую имеет макроблок с индексом i.
 */
Map.getMacroBlockYOnMap = function(i) {
    var MACROBLOCK_H = 20;
    return i * (MACROBLOCK_H * CELL_H);
};

/**
 * Возвращает j индекс клетки по x координате карты.
 */
Map.getCellJOnMap = function(x) {
    return Math.floor(x / CELL_W)
};

/**
 * Возвращает i индекс клетки по y координате карты.
 */
Map.getCellIOnMap = function(y) {
    return Math.floor(y / CELL_H)
};

/**
 * Возвращает j индекс макроблока по x координате карты.
 */
Map.getMacroBlockJOnMap = function(x) {
    var MACROBLOCK_W = 20;
    return Math.floor(x / (MACROBLOCK_W * CELL_W));
};

/**
 * Возвращает i индекс макроблока по y координате карты.
 */
Map.getMacroBlockIOnMap = function(y) {
    var MACROBLOCK_H = 20;
    return Math.floor(y / (MACROBLOCK_H * CELL_H));
};

/**
 * Проверяет, лежат ли клетки на диагонали.
 * @param i1 i индекс первой клетки.
 * @param j1 j индекс первой клетки.
 * @param i2 i индекс второй клетки.
 * @param j2 j индекс второй клетки.
 * @return Возвращает true, если клетки лежат на диагонали и false в
 * противном случае.
 */
Map.isDiagonalCells = function(i1, j1, i2, j2) {
    if (Math.abs(i1 - i2) == Math.abs(j1 - j2)) {
        return true
    }

    return false
}
