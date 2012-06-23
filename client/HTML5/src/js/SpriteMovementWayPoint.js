
/**
 * Точка пути плавного дивжения юнита.
 */
function SpriteMovementWayPoint(j, i, arrivalTime, arrivalSpeed, movementOrientation) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    this.id = createId();
    
    /**
     * Индекс клетки на карте по ширине.
     */
    this.j = j;
    
    /**
     * Индекс клетки на карте по высоте.
     */
    this.i = i;
    
    /**
     * Юникс время прибытия в эту точку.
     */
    this.arrivalTime = arrivalTime;
    
    /**
     * Скорость движения из предыдущей точки пути в эту точку.
     */
    this.arrivalSpeed = arrivalSpeed;
    
    /**
     * Ориентация движения из предыдущей точки пути в эту точку.
     */
    this.movementOrientation = movementOrientation;
    
    /**
     * Возвращает текущую позицию при движении из точки wayPoint в эту точку.
     * @return возвращает текущую позицию или null, в случае какой-либо ошибки.
     */
    this.getPosOnTheWayFrom = function(wayPoint) {
        var curTime;    // Текущее юникс время в милисекундах.
        var begX;       // Положение по оси 0X точки на карте, из который мы идём.
        var begY;       // Положение по оси 0Y точки на карте, из который мы идём.
        var curX;       // Текущее положение по оси 0X на карте места, в котором мы сейчас находимся.
        var curY;       // Текущее положение по оси 0Y на карте места, в котором мы сейчас находимся.
        var endX;       // Положение по оси 0X точки на карте, в которую мы идём (в эту точку).
        var endY;       // Положение по оси 0Y точки на карте, в которую мы идём (в эту точку).
        
        /*
         * Время, которое прошло с момента прибытия в точку, из которой мы идём
         * в эту точку до текущего момента времени.
         */
        var diffTime;
        
        /*
         * Путь в пикселях, пройденных за время с момента прибытия в точку, из
         * которой мы идём в эту точку до текущего момента времени.
         */
        var diffS;
        
        /*
         * Если true, то при расчете текущего положения мы не можем пройти
         * по любой координате мимо точки, в которую идём (мимо этой точки).
         * Если false, то можем.
         */
        var fix = true;
        
        if (!(wayPoint instanceof SpriteMovementWayPoint)) {
            console.log("Идти можно только из одной точки пути в другую.");
            return null;
        }
        
        curTime = (new Date()).getTime();
        
        /*
         * Если текущее время больше или равно, чем время прибытия в эту точку,
         * то возвращаем положение этой точки на карте с флагом, что движение
         * завершено.
         */
        if (this.arrivalTime <= curTime) {
            return {
                x: Map.getCellXOnMap(this.j),
                y: Map.getCellYOnMap(this.i),
                arrived: true,
                orientation: this.movementOrientation
            };
        }
        
        /*
         * Если текущее время меньше, чем время прибытия в точку из которой
         * мы идём в эту точку, то выводим ошибку и возвращаем null.
         */
        if (wayPoint.arrivalTime > curTime) {
            console.log("Время прибытия в точку " + wayPoint + " ещё не наступило. Попытка идти оттуда в эту точку скорей всего ошибка.");
            return null;
        }
        
        begX = Map.getCellXOnMap(wayPoint.j);
        begY = Map.getCellYOnMap(wayPoint.i);
        endX = Map.getCellXOnMap(this.j);
        endY = Map.getCellYOnMap(this.i);
        diffTime = curTime - wayPoint.arrivalTime;
        diffS = this.arrivalSpeed * diffTime;
        
        switch (this.movementOrientation) {
            case "N":
                curX = endX;
                curY = begY - diffS;
                if (fix && curY < endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curY <= endY ? true : false,
                    orientation: this.movementOrientation
                };
            case "NE":
                curX = begX + diffS;
                curY = begY - diffS;
                if (fix && curX > endX) {
                    curX = endX;
                }
                if (fix && curY < endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX >= endX && curY <= endY ? true : false,
                    orientation: this.movementOrientation
                };
            case "E":
                curX = begX + diffS;
                curY = endY;
                if (fix && curX > endX) {
                    curX = endX;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX >= endX ? true : false,
                    orientation: this.movementOrientation
                };
            case "SE":
                curX = begX + diffS;
                curY = begY + diffS;
                if (fix && curX > endX) {
                    curX = endX;
                }
                if (fix && curY > endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX >= endX && curY >= endY ? true : false,
                    orientation: this.movementOrientation
                };
            case "S":
                curX = endX;
                curY = begY + diffS;
                if (fix && curY > endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curY >= endY ? true : false,
                    orientation: this.movementOrientation
                };
            case "SW":
                curX = begX - diffS;
                curY = begY + diffS;
                if (fix && curX < endX) {
                    curX = endX;
                }
                if (fix && curY > endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX <= endX && curY >= endY ? true : false,
                    orientation: this.movementOrientation
                };
            case "W":
                curX = begX - diffS;
                curY = endY;
                if (fix && curX < endX) {
                    curX = endX;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX <= endX ? true : false,
                    orientation: this.movementOrientation
                };
            case "NW":
                curX = begX - diffS;
                curY = begY - diffS;
                if (fix && curX < endX) {
                    curX = endX;
                }
                if (fix && curY < endY) {
                    curY = endY;
                }
                return {
                    x: curX,
                    y: curY,
                    arrived: curX <= endX && curY <= endY ? true : false,
                    orientation: this.movementOrientation
                };
            default:
                console.log("Неизвестная ориентация движения! Случилось что-то совсем плохое.");
                return null;
        }
    }
    
    /**
     * Преобразовывает точку пути в строку.
     */
    this.toString = function() {
        return "[id=" + this.id + ",j=" + this.j + ",i=" + this.i + ",t=" + this.arrivalTime + ",s=" + this.arrivalSpeed + ",o=" + this.movementOrientation + "]";
    }
    
    return this;
}
