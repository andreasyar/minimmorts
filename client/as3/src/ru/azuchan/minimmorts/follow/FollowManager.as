package ru.azuchan.minimmorts.follow {
    import flash.display.Stage;
    import ru.azuchan.minimmorts.actions.ActionManager;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.Unit;
    
	/**
     * Менеджер преследований.
     * @author sorc
     */
    public class FollowManager {
        
        /**
         * Массив преследований.
         */
        private var followings:Array;
        
        /**
         * Массив анимаций мигающей рамки вокруг преследуемого юнита.
         */
        private var animations:Array;
        
        /**
         * Холст для анимаций мигающей рамки вокруг преследуемого юнита.
         */
        private var stage:Stage;
        
        /**
         * Создаёт новый менеджер преследований.
         */
        public function FollowManager(animations:Array, stage:Stage) {
            followings = [];
            this.animations = animations;
            this.stage = stage;
        }
        
        /**
         * Удаляет преследование по субъекту.
         * @param subject субъект
         */
        public function removeFollowing(subject:Unit):void {
            for (var i:int = 0; i < followings.length; i++) {
                if (followings[i].subject == subject) {
                    followings.splice(i, 1);
                    break;
                }
            }
        }
        
        /**
         * Удаляет все преследования объекта.
         * @param object объект
         */
        public function removeFollowingByObject(object:Unit):void {
            var found:Boolean;
            do {
                found = false;
                for (var i:int = 0; i < followings.length; i++) {
                    if (followings[i].object == object) {
                        followings.splice(i, 1);
                        found = true;
                        break;
                    }
                }
            } while (found);
        }
        
        /**
         * Добавляет новое преследование субъектом объекта.
         * @param subject субъект
         * @param object объект
         */
        public function addFollowing(subject:Unit, object:Unit):void {
            for (var i:int = 0; i < followings.length; i++) {
                if (followings[i].subject == subject) {
                    if (Config.DEBUG_FOLLOW) {
                        trace(Main.step + ": Юнит " + subject + " изменил цель преследования c " + followings[i].object + " на " + object);
                    }
                    followings[i].object = object;
                    var animation:FollowBlinkAnimation = new FollowBlinkAnimation(object, stage);
                    animations.push(animation);
                    return;
                }
            }
            
            followings.push(new Follow(subject, object));
            if (Config.DEBUG_FOLLOW) {
                trace(Main.step + ": Юнит " + subject + " начал преследовать цель " + object);
            }
            var animation:FollowBlinkAnimation = new FollowBlinkAnimation(object, stage);
            animations.push(animation);
            return;
        }
        
        /**
         * Возвращает все преследования.
         * @return преследования
         */
        internal function getFollowings():Array {
            return followings;
        }
    }
}
