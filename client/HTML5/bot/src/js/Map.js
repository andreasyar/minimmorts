
// Карта.
// Require: resources.js, SpriteLoader.js, constants.js

// Карта игрового мира.
function Map(width, height) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

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

        var j_from
        if (camera.x < 0) {
            j_from = 0
        } else {
            j_from = Map.getCellJOnMap(camera.x)
        }

        var j_to
        if (camera.x + SCR_W >= this.w * CELL_W) {
            j_to = this.w - 1
        } else {
            j_to = Map.getCellJOnMap(camera.x + SCR_W)
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellJOnMap можем случайно вылететь за края карты.
        if (j_to >= this.w) {
            j_to = this.w - 1;
        }

        var i_from = Map.getCellIOnMap(camera.y)
        if (camera.y < 0) {
            i_from = 0
        } else {
            i_from = Map.getCellIOnMap(camera.y)
        }

        var i_to = Map.getCellIOnMap(camera.y + SCR_H)
        if (camera.y + SCR_H >= this.h * CELL_H) {
            i_to = this.h - 1
        } else {
            i_to = Map.getCellIOnMap(camera.y + SCR_H)
        }
        // Разрешение экрана может быть разное, поэтому при подсчете
        // Map.getCellIOnMap можем случайно вылететь за края карты.
        if (i_to >= this.h) {
            i_to = this.h - 1;
        }

        var i, j,
            tAxis   // Translated axis. Map -> Camera.

        // Нарисуем клетки вычисленного региона карты.
        for (i = i_from; i <= i_to; i++) {
            for (j = j_from; j <= j_to; j++) {
                tAxis = camera.translateAxis(Map.getCellXOnMap(j), Map.getCellYOnMap(i));
                context.drawImage(this.spriteMap[i][j].ground, tAxis.x, tAxis.y);
                //context.drawImage(this.sloader.getSpriteByName("grass_border").getImage(), tAxis.x, tAxis.y);
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
