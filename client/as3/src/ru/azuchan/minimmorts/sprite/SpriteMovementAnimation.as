package ru.azuchan.minimmorts.sprite {

	/**
     * Анимация движения.
     * @author sorc
     */
    public class SpriteMovementAnimation {
        
        /**
         * Количество спрайтов в анимации.
         */
        private var spritesCount:int;
        
        /**
         * Временной интервал между сменами спрайтов.
         */
        private var spriteInterval:int;
        
        /**
         * Время начала анимации движения.
         */
        private var creationTime:int;
        
        /**
         * Создаёт анимацию движения.
         */
        public function SpriteMovementAnimation() {
            spritesCount = 5;
            spriteInterval = 150;
            creationTime = (new Date()).getTime();
        }
        
        /**
         * Возвращает индекс спрайта из набора анимированных спрайтов движения. При
         * анимации движения следует при каждой перерисовке движущегося юнита
         * вызывать этот метод, чтобы получить нужный индекс спрайта движения.
         */
        public function next():int {
            return Math.floor(((new Date()).getTime() - creationTime) / spriteInterval) % spritesCount;
        }
        
        /**
         * Возвращает индекс спрайта неподвижно стоящего юнита.
         */
        public function getStand():int {
            return 0;
        }
    }
}
