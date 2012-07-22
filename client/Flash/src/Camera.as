package {

	/**
     * Камера. "Показывает" место, которое будет показано пользователю.
     * @author sorc
     */
    public class Camera {
        
        /**
         * Положение камеры на карте по оси X.
         */
        public var x:Number;
        
        /**
         * Положение камеры на карте по оси Y.
         */
        public var y:Number;
        
        /**
         * Ссылка на объект, за которым следит камера (который всегда будет
         * в центре объектива). Объект должен иметь атрибуты x и y, содержащие его
         * координаты на карте.
         */
        private var target:Object;
        
        public function Camera(target:Object) {
            x = 0;
            y = 0;
            this.target = target;
        }
        
        /**
         * Привязывает камеру к цели, чтобы цель всегда находилась в центре
         * объектива.
         * @param target Цель
         */
        public function follow(target:Object):void {
            this.target = target;
            updatePos();
        }
        
        /**
         * Отвязывает камеру от цели.
         */
        public function unfollow():void {
            target = null;
        }
        
        /**
         * Проверяет, следит ли камера за какой-либо целью.
         */
        public function isFollow():Boolean {
            return target != null;
        }
        
        /**
         * Обновляет координаты камеры на карте в зависимости от того, где
         * находится цель.
         */
        public function updatePos():void {
            if (target != null) {
                x = target.spritePos.x - Config.SCR_W / 2;
                y = target.spritePos.y - Config.SCR_H / 2;
            }
        }
        
        /**
         * Проверяет, попадат ли объект с заданными границами в объектив.
         * @param x Координата объекта на карте по оси Х.
         * @param y Координата объекта на карте по оси Y.
         * @param w Ширина объекта.
         * @param h Высота объекта.
         */
        public function isOnView(x:Number, y:Number, w:int, h:int):Boolean {
            updatePos();
            
            // Объект находится целиком справа или слева за объективом.
            if (x > this.x + Config.SCR_W || x + w < this.x) {
                return false;
            }
            
            // Объект находится целиком снизу или сверху за объективом.
            if (y > this.y + Config.SCR_H || y + h < this.y) {
                return false;
            }
            
            return true;
        }
        
        /**
         * Возвращает координаты внутри объектива камеры по координатам на карте.
         * Этот метод будет вызывать любой объект чтобы узнать координаты где его
         * нужно нарисовать.
         */
        public function translateAxis(x:Number, y:Number):Object {
            updatePos();
            
            return {
                x: x - this.x,
                y: y - this.y
            };
        }
        
        /**
         * Возвращает координаты на карте по координатам внутри объектива камеры.
         */
        public function reTranslateAxis(x:Number, y:Number):Object {
            updatePos();
            
            return {
                x: this.x + x,
                y: this.y + y
            };
        }
    }
}