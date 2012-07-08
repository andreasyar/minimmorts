
// Камера.
// Require constants.js

/**
 * Камера. "Показывает" место, которое будет нарисовано на основном холсте.
 */
function Camera(target) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }
    
    /**
     * Положение камеры на карте по оси X.
     */
    this.x = 0;
    
    /**
     * Положение камеры на карте по оси Y.
     */
    this.y = 0;
    
    /**
     * Ссылка на объект, за которым следит камера (который всегда будет
     * в центре объектива). Объект должен иметь атрибуты x и y, содержащие его
     * координаты на карте.
     */
    this.target = target;
    
    /**
     * Привязывает камеру к цели, чтобы цель всегда находилась в центре
     * объектива.
     * @param target Цель
     */
    this.follow = function(target) {
        this.target = target;
    }
    
    /**
     * Отвязывает камеру от цели.
     */
    this.unfollow = function() {
        this.target = null;
    }
    
    this.isFollow = function() {
        return this.target != null;
    }
    
    /**
     * Обновляет координаты камеры на карте в зависимости от того, где
     * находится цель.
     */
    this.updatePos = function () {
        if (this.target != undefined && this.target != null) {
            this.x = this.target.spritePos.x - SCR_W / 2;
            this.y = this.target.spritePos.y - SCR_H / 2;
        }
    }

    /**
     * Проверяет, попадат ли объект с заданными границами в объектив.
     * @param x Координата объекта на карте по оси Х.
     * @param y Координата объекта на карте по оси Y.
     * @param w Ширина объекта.
     * @param h Высота объекта.
     */
    this.isOnView = function(x, y, w, h) {
        this.updatePos();

        // Объект находится целиком справа или слева за объективом.
        if (x > this.x + SCR_W || x + w < this.x) {
            return false;
        }

        // Объект находится целиком снизу или сверху за объективом.
        if (y > this.y + SCR_H || y + h < this.y) {
            return false;
        }

        return true;
    }

    /**
     * Возвращает координаты внутри объектива камеры по координатам на карте.
     * Этот метод будет вызывать любой объект чтобы узнать координаты где его
     * нужно нарисовать.
     */
    this.translateAxis = function(x, y) {
        this.updatePos();

        return {
            x: x - this.x,
            y: y - this.y
        };
    }

    /**
     * Возвращает координаты на карте по координатам внутри объектива камеры.
     */
    this.reTranslateAxis = function(x, y) {
        this.updatePos();

        return {
            x: this.x + x,
            y: this.y + y
        };
    }

    return this;
}
