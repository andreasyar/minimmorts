function Unit(ownerId, id, j, i) {

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
        var tAxis = camera.translateAxis(Map.getCellXOnMap(this.j), Map.getCellYOnMap(this.i));
        var sprite;
        if (this.selected) {
            sprite = this.sloader.getSpriteByName("peasant_selected");
        } else {
            sprite = this.sloader.getSpriteByName("peasant");
        }
        context.drawImage(sprite, tAxis.x, tAxis.y);
    }
}
