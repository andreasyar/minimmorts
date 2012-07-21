package {

	/**
     * Константы и настройки.
     * @author sorc
     */
    public class Config {
        
        /**
         * Ширина основного холста в пикселях.
         */
        public static var SCR_W:int = 800;
        
        /**
         * Высота основного холста в пикселях.
         */
        public static var SCR_H:int = 600;
        
        /**
         * Полуширина основного холста в пикселях.
         */
        public static var SCR_HW:int = 400;
        
        /**
         * Полувысота основного холста в пикселях.
         */
        public static var SCR_HH:int = 300;
        
        /**
         * Ширина ячейки карты в пикселях.
         */
        public static var CELL_W:int = 32;
        
        /**
         * Высота ячейки карты в пикселях.
         */
        public static var CELL_H:int = 32;
        
        /**
         * Полуширина ячейки карты в пикселях.
         */
        public static var CELL_HW:int = 16;
        
        /**
         * Полувысота ячейки карты в пикселях.
         */
        public static var CELL_HH:int = 16;
        
        /**
         * Скорость юнитов по-умолчанию.
         */
        public static var DEFAULT_SPEED:Number = 0.3;
        
        /**
         * Плавное движение юнитов.
         */
        public static var smoothMovementEnabled:Boolean = true;
        
        public static const DEBUG_MOVEMENTS:Boolean = false;
        
        public static const DEBUG_UNITS:Boolean = false;
        
        public static const DEBUG_MODES:Boolean = false;
        
        public static const DEBUG_PROTOCOL:Boolean = false;
        
        public static const usePathCache:Boolean = true;
        
        public static const DEBUG_FOLLOW:Boolean = false;
        
        public function Config() {
            
        }
    }
}