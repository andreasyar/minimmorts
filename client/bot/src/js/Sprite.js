/**
 * Спрайт для рисования на экране.
 */
function Sprite(image, w, h, baseX, baseY) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.image = image;
    
    this.w = w;
    
    this.h = h;
    
    this.baseX = baseX;
    
    this.baseY = baseY;
    
    this.getImage = function() {
        return this.image;
    }
    
    this.getDrawCoordinates = function(unitJ, unitI, camera) {
        
        if (this.w < CELL_W || this.h < CELL_H) {
            console.log("Sprite bounds lesser than map cell.");
            return null;
        }
        
        if (this.baseX < CELL_HW || this.baseY < CELL_HH || this.baseX + CELL_HW > this.w || this.baseY + CELL_HH > this.h) {
            console.log("Sprite cannot cover all cell.");
            return null;
        }
        
        // Координаты на карте ячейки, где находится игрок.
        var unitMXY = camera.translateAxis(Map.getCellXOnMap(unitJ), Map.getCellYOnMap(unitI));
        
        return {
            x: unitMXY.x - (this.baseX - CELL_HW),
            y: unitMXY.y - (this.baseY - CELL_HH)
        };
    }
    
    this.getDrawCoordinatesByXY = function(unitMX, unitMY, camera) {
        
        if (this.w < CELL_W || this.h < CELL_H) {
            console.log("Sprite bounds lesser than map cell.");
            return null;
        }
        
        if (this.baseX < CELL_HW || this.baseY < CELL_HH || this.baseX + CELL_HW > this.w || this.baseY + CELL_HH > this.h) {
            console.log("Sprite cannot cover all cell.");
            return null;
        }
        
        // Координаты на карте ячейки, где находится игрок.
        var unitMXY = camera.translateAxis(unitMX, unitMY);
        
        return {
            x: unitMXY.x - (this.baseX - CELL_HW),
            y: unitMXY.y - (this.baseY - CELL_HH)
        };
    }
}
