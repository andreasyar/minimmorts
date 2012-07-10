function Follow(object, target) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    if (!(object instanceof Unit)) {
        throw new Error("\"Объект\" не является объектом типа Unit.");
    }

    if (!(target instanceof Unit)) {
        throw new Error("\"Субъект\" не является объектом типа Unit.");
    }
    
    this.object = object;
    
    this.target = target;
    
    this.getObject = function() {
        return this.object;
    }
    
    this.getTarget = function() {
        return this.target;
    }
    
    this.setTarget = function(target) {
        this.target = target;
    }
    
    return this;
}
