/**
 * Точка.
 * @param j - индекс точки по ширине
 * @param i - индекс точки по высоте
 */
function Point(j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.j = j;
    
    this.i = i;
    
    this.getJ = function() {
        return this.j;
    }
    
    this.getI = function() {
        return this.i;
    }
    
    this.toString = function() {
        return "[" + this.j + "," + this.i + "]";
    }
    
    this.equals = function(obj) {
        if (obj instanceof Point) {
            return obj.getJ() == this.j && obj.getI() == this.i;
        }
        return false;
    }
    
    return this;
}
