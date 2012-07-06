function Unit(ownerId, id, j, i, color) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.ownerId = ownerId;
    this.id = id;
    this.j = j;
    this.i = i;
    this.sloader = new SpriteLoader(SPRITES);
    this.selected = false;
    this.spritePos = {
        x: Map.getCellXOnMap(this.j),
        y: Map.getCellYOnMap(this.i),
        arrived: true,
        orientation: "S",
        spriteName: "peasant_S_walk_0"
    };

    var tmpCanvas = document.createElement('canvas');
    tmpCanvas.width = CELL_W;
    tmpCanvas.height = CELL_H;
    this.unitMarkBackground = (tmpCanvas.getContext("2d")).createImageData(CELL_W, CELL_H);
    for (var k = 0; k < CELL_H; k++) {
        for (var l = 0; l < CELL_W; l++) {
            setPixel(this.unitMarkBackground, l, k, color.r, color.g, color.b, 128);
        }
    }
    
    this.history = new Array();
    
    this.getOwnerId = function() {
        return this.ownerId;
    }
    this.getId = function() {
        return this.id;
    }
    this.getJ = function() {
        return this.j;
    }
    this.getI = function() {
        return this.i;
    }
    this.draw = function(context, camera) {
        var sprite;
        var drawCoordinates;
        if (smoothMovementEnabled) {
            sprite = this.sloader.getSpriteByName(this.spritePos.spriteName);
            if (sprite == null) {
                return;
            }
            drawCoordinates = sprite.getDrawCoordinatesByXY(this.spritePos.x, this.spritePos.y, camera);
        } else {
            sprite = this.sloader.getSpriteByName("peasant_" + this.spritePos.orientation);
            drawCoordinates = sprite.getDrawCoordinatesByXY(Map.getCellXOnMap(this.j), Map.getCellYOnMap(this.i), camera);
        }
        context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        if (DEBUG_UNITS) {
            context.fillText(this.ownerId, drawCoordinates.x, drawCoordinates.y);
        }
        if (this.selected) {
            sprite = this.sloader.getSpriteByName("peasant_selection");
            context.drawImage(sprite.getImage(), drawCoordinates.x, drawCoordinates.y);
        }
        return;
    }
    this.drawColor = function(context, camera) {
        var sprite;
        var drawCoordinates;
        if (smoothMovementEnabled) {
            sprite = this.sloader.getSpriteByName(this.spritePos.spriteName);
            if (sprite == null) {
                return;
            }
            drawCoordinates = sprite.getDrawCoordinatesByXY(this.spritePos.x, this.spritePos.y, camera);
        } else {
            sprite = this.sloader.getSpriteByName("peasant_" + this.spritePos.orientation);
            drawCoordinates = sprite.getDrawCoordinatesByXY(Map.getCellXOnMap(this.j), Map.getCellYOnMap(this.i), camera);
        }
        context.putImageData(this.unitMarkBackground, drawCoordinates.x + 21, drawCoordinates.y + 17);
        return;
    }

    this.toString = function() {
        return "{ownerId=" + this.ownerId + ",id=" + this.id + ",j=" + this.j + ",i=" + this.i + "}";
    }

    return this;
}
