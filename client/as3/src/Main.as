package {
    import flash.display.Sprite;
    import flash.display.Stage;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.events.MouseEvent;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import flash.text.TextField;
    import com.adobe.serialization.json.JSON;
    import ru.azuchan.minimmorts.client.ClientMainLoop;
    import ru.azuchan.minimmorts.movement.PathCache;
    import ru.azuchan.minimmorts.movement.Point;
    import ru.azuchan.minimmorts.units.UnitManager;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.messages.MessageManager;
    import ru.azuchan.minimmorts.net.ServerInteraction;
    import ru.azuchan.minimmorts.actions.ActionManager;

    /**
     * ...
     * @author sorc
     */
    public class Main extends Sprite {
        
        /**
         * Карта.
         */
        static public var map:Map;
        
        /**
         * Камера.
         */
        static public var camera:Camera;
        
        /**
         * Загрузчик спрайтов.
         */
        static public var spriteLoader:SpriteLoader;
        
        /**
         * Главный холст.
         */
        static public var stage:Stage;
        
        /**
         * Идентификатор клиента.
         */
        static public var id:int;
        
        /**
         * Текущий шаг.
         */
        static public var step:int;
        
        /**
         * Продолжительность хода в милисекундах.
         */
        static public var stepTime:int;
        
        static public var pathCache:PathCache;
        
        private var messageManager:MessageManager;
        
        static public var movementManager:MovementManager;
        
        private var actionManager:ActionManager;
        
        static public var unitManager:UnitManager;
        
        private var serverInteraction:ServerInteraction;
        
        public var tf:TextField;
        
        public function Main():void {
            if (stage) {
                init();
            } else {
                addEventListener(Event.ADDED_TO_STAGE, init);
            }
        }
        
        private function init(e:Event = null):void {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
            Main.stage = stage;
            Main.spriteLoader = new SpriteLoader(loadCompleteCallback);
            Main.camera = new Camera(null);
            Main.pathCache = new PathCache();
            Main.step = -1;
        }
        
        private function loadCompleteCallback():void {
            Main.map = new Map(100, 100);
            
            movementManager = new MovementManager();
            unitManager = new UnitManager();
            messageManager = new MessageManager(movementManager, unitManager, welcomeReceivedCallback);
            actionManager = new ActionManager();
            serverInteraction = new ServerInteraction(messageManager);
            
            serverInteraction.connect("ws://localhost:8015/");
        }
        
        private function welcomeReceivedCallback():void {
            stage.addEventListener(Event.ENTER_FRAME, onEnterFrame);
            stage.addEventListener(KeyboardEvent.KEY_DOWN, keyDown);
            stage.addEventListener(MouseEvent.CLICK, onMouseClick);
            stage.addEventListener(MouseEvent.RIGHT_CLICK, onMouseRightClick);
            
            var client:ClientMainLoop = new ClientMainLoop(messageManager, movementManager, actionManager, unitManager, serverInteraction);
            client.start();
            /*tf = new TextField();
            tf.text = "Derp";
            stage.addChild(tf);*/
        }
        
        private function onEnterFrame(event:Event):void {
            map.update(Main.camera);
            unitManager.update(Main.camera);
            movementManager.updateSpritePositions();
        }
        
        private function keyDown(event:KeyboardEvent):void {
            if (event.keyCode == 37) {  // left arrow
                Main.camera.x -= 1 * Config.CELL_W;
                if (Main.camera.x < -Config.SCR_HW) {
                    Main.camera.x = -Config.SCR_HW;
                }
            } else if (event.keyCode == 39) {   // right arrow
                Main.camera.x += 1 * Config.CELL_W;
                if (Main.camera.x > map.getWidth() * Config.CELL_W - Config.SCR_HW) {
                    Main.camera.x = map.getWidth() * Config.CELL_W - Config.SCR_HW;
                }
            } else if (event.keyCode == 38) {   // up arrow
                Main.camera.y -= 1 * Config.CELL_H;
                if (Main.camera.y < -Config.SCR_HH) {
                    Main.camera.y = -Config.SCR_HH;
                }
            } else if (event.keyCode == 40) {   // down arrow
                Main.camera.y += 1 * Config.CELL_H;
                if (Main.camera.y > map.getHeight() * Config.CELL_H - Config.SCR_HH) {
                    Main.camera.y = map.getHeight() * Config.CELL_H - Config.SCR_HH;
                }
            }
        }
        
        private function onMouseClick(event:MouseEvent):void {
            var tAxis:Object = camera.reTranslateAxis(event.stageX, event.stageY);
            unitManager.selectUnits(tAxis.x, tAxis.y);
        }
        
        private function onMouseRightClick(event:MouseEvent):void {
            trace("Right click.");
            var tAxis:Object = camera.reTranslateAxis(event.stageX, event.stageY);
            actionManager.moveSelectedUnits(new Point(Map.getCellJOnMap(tAxis.x), Map.getCellIOnMap(tAxis.y)));
        }
    }
}
