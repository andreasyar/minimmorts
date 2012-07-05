/**
 * Анимация движения.
 */
function SpriteMovementAnimation() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    this.spritesCount = 5;
    
    /**
     * Временной интервал между сменами спрайтов.
     */
    this.spriteInterval = 150;
    
    this.creationTime = (new Date()).getTime();

    /**
     * Возвращает индекс спрайта из набора анимированных спрайтов движения. При
     * анимации движения следует при каждой перерисовке движущегося юнита
     * вызывать этот метод, чтобы получить нужный индекс спрайта движения.
     */
    this.next = function() {
        return Math.floor(((new Date()).getTime() - this.creationTime) / this.spriteInterval) % this.spritesCount;
    }

    /**
     * Возвращает спрайт юнита, стоящего неподвижно.
     */
    this.getStand = function() {
        return 0;
    }
}
