package {
    import flash.display.Bitmap;
    import flash.display.BitmapData;
    import flash.display.Shape;
    import flash.display.Sprite;
    import flash.display.Stage;
    import flash.events.Event;
    import flash.events.KeyboardEvent;
    import flash.events.MouseEvent;
    import flash.events.ThrottleEvent;
    import flash.events.TimerEvent;
    import flash.text.TextField;
    import flash.system.Security;
    import flash.geom.ColorTransform;
    import flash.utils.Timer;
    import com.adobe.serialization.json.JSON;
    import ru.azuchan.minimmorts.follow.FollowLoop;
    import ru.azuchan.minimmorts.follow.FollowManager;
    import ru.azuchan.minimmorts.map.Cell;
    import ru.azuchan.minimmorts.map.Map;
    import ru.azuchan.minimmorts.sprite.SpriteLoader;
    import ru.azuchan.minimmorts.client.ClientMainLoop;
    import ru.azuchan.minimmorts.movement.PathCache;
    import ru.azuchan.minimmorts.movement.MovementManager;
    import ru.azuchan.minimmorts.units.UnitManager;
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
        private var map:Map;
        
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
        
        private var movementManager:MovementManager;
        
        private var actionManager:ActionManager;
        
        private var unitManager:UnitManager;
        
        private var followManager:FollowManager;
        
        private var serverInteraction:ServerInteraction;
        
        static public var tf:TextField;
        
        static public var freeAnimation:Array;
        
        static public var serverIp:String;
        
        private var selectionRect:Shape;
        
        private var selectionBeg:Object;
        
        private var selectionEnd:Object;
        
        private var scrollMapBars:Array;
        
        private var scrollMapBarTimers:Array;
        
        private var minimap:Bitmap;
        
        private var throttle:Boolean;
        
        private var clientMainLoop:ClientMainLoop;
        
        public function Main():void {
            if (stage) {
                init();
            } else {
                addEventListener(Event.ADDED_TO_STAGE, init);
            }
        }
        
        private function init(e:Event = null):void {
            removeEventListener(Event.ADDED_TO_STAGE, init);
            
            //Security.loadPolicyFile("http://2.92.40.121/flash/policy.php");
            //trace(Security.sandboxType);
            Main.stage = stage;
            Main.spriteLoader = new SpriteLoader(loadCompleteCallback);
            Main.camera = new Camera(null);
            Main.pathCache = new PathCache();
            Main.step = -1;
            Main.freeAnimation = [];
            throttle = false;
        }
        
        private function loadCompleteCallback():void {
            map = new Map(100, 100);
            map.show(stage);
            
            followManager = new FollowManager(Main.freeAnimation, Main.stage);
            movementManager = new MovementManager(map);
            unitManager = new UnitManager(movementManager, followManager);
            messageManager = new MessageManager(movementManager, unitManager);
            messageManager.addEventListener("WELCOME", welcomeReceivedCallback);
            messageManager.addEventListener("STEPTIME", stepTimeReceivedCallback);
            actionManager = new ActionManager(unitManager, followManager, map);
            serverInteraction = new ServerInteraction(messageManager);
            
            minimap = new Bitmap(new BitmapData(map.width, map.height));
            minimap.x = 25;
            minimap.y = 25;
            stage.addChild(minimap);
            
            if (loaderInfo.parameters.server != undefined) {
                Main.serverIp = loaderInfo.parameters.server as String;
            } else {
                Main.serverIp = "2.92.40.121";
            }
            
            tf = new TextField();
            tf.x = 200;
            tf.y = 200;
            tf.wordWrap = true;
            stage.addChild(tf);
            
            serverInteraction.connect("ws://" + Main.serverIp + ":8015/");
        }
        
        private function welcomeReceivedCallback():void {
            stage.addEventListener(Event.ENTER_FRAME, onEnterFrame);
            stage.addEventListener(KeyboardEvent.KEY_DOWN, keyDown);
            stage.addEventListener(MouseEvent.MOUSE_DOWN, onMouseDown);
            stage.addEventListener(MouseEvent.MOUSE_UP, onMouseUp);
            stage.addEventListener(MouseEvent.MOUSE_MOVE, onMouseMove);
            stage.addEventListener(MouseEvent.RIGHT_CLICK, onMouseRightClick);
            stage.addEventListener(ThrottleEvent.THROTTLE, onThrottle);
            
            clientMainLoop = new ClientMainLoop(messageManager, movementManager, actionManager, unitManager, serverInteraction, onStepComplete);
            clientMainLoop.start();
            
            var follower:FollowLoop = new FollowLoop(actionManager, followManager);
            follower.start();
            
            selectionRect = new Shape();
            selectionBeg = null;
            selectionEnd = null;
            
            scrollMapBars = [new Sprite(), new Sprite(), new Sprite(), new Sprite()];
            
            scrollMapBarTimers = [new Timer(60), new Timer(60), new Timer(60), new Timer(60)];
            
            scrollMapBars[0].graphics.beginFill(0xFF0000);
            scrollMapBars[0].graphics.moveTo(0, 0);
            scrollMapBars[0].graphics.lineTo(24, 24);
            scrollMapBars[0].graphics.lineTo(24, Config.SCR_H - 1 - 24);
            scrollMapBars[0].graphics.lineTo(0, Config.SCR_H - 1);
            scrollMapBars[0].graphics.lineTo(0, 0);
            scrollMapBars[0].addEventListener(MouseEvent.MOUSE_OVER, onScrollMapBarOver);
            scrollMapBars[0].addEventListener(MouseEvent.MOUSE_OUT, onScrollMapBarOut);
            scrollMapBars[0].name = "leftScrollBar";
            stage.addChild(scrollMapBars[0]);
            scrollMapBarTimers[0].addEventListener("timer", scrollLeft);
            
            scrollMapBars[1].graphics.beginFill(0xFF0000);
            scrollMapBars[1].graphics.moveTo(0, 0);
            scrollMapBars[1].graphics.lineTo(24, 24);
            scrollMapBars[1].graphics.lineTo(Config.SCR_W - 1 - 24, 24);
            scrollMapBars[1].graphics.lineTo(Config.SCR_W - 1, 0);
            scrollMapBars[1].graphics.lineTo(0, 0);
            scrollMapBars[1].addEventListener(MouseEvent.MOUSE_OVER, onScrollMapBarOver);
            scrollMapBars[1].addEventListener(MouseEvent.MOUSE_OUT, onScrollMapBarOut);
            scrollMapBars[1].name = "upScrollBar";
            stage.addChild(scrollMapBars[1]);
            scrollMapBarTimers[1].addEventListener("timer", scrollUp);
            
            scrollMapBars[2].graphics.beginFill(0xFF0000);
            scrollMapBars[2].graphics.moveTo(Config.SCR_W - 1, 0);
            scrollMapBars[2].graphics.lineTo(Config.SCR_W - 1, Config.SCR_H - 1);
            scrollMapBars[2].graphics.lineTo(Config.SCR_W - 1 - 24, Config.SCR_H - 1 - 24);
            scrollMapBars[2].graphics.lineTo(Config.SCR_W - 1 - 24, 24);
            scrollMapBars[2].graphics.lineTo(Config.SCR_W - 1, 0);
            scrollMapBars[2].addEventListener(MouseEvent.MOUSE_OVER, onScrollMapBarOver);
            scrollMapBars[2].addEventListener(MouseEvent.MOUSE_OUT, onScrollMapBarOut);
            scrollMapBars[2].name = "rightScrollBar";
            stage.addChild(scrollMapBars[2]);
            scrollMapBarTimers[2].addEventListener("timer", scrollRight);
            
            scrollMapBars[3].graphics.beginFill(0xFF0000);
            scrollMapBars[3].graphics.moveTo(Config.SCR_W - 1, Config.SCR_H - 1);
            scrollMapBars[3].graphics.lineTo(0, Config.SCR_H - 1);
            scrollMapBars[3].graphics.lineTo(24, Config.SCR_H - 1 - 24);
            scrollMapBars[3].graphics.lineTo(Config.SCR_W - 1 - 24, Config.SCR_H - 1 - 24);
            scrollMapBars[3].graphics.lineTo(Config.SCR_W - 1, Config.SCR_H - 1);
            scrollMapBars[3].addEventListener(MouseEvent.MOUSE_OVER, onScrollMapBarOver);
            scrollMapBars[3].addEventListener(MouseEvent.MOUSE_OUT, onScrollMapBarOut);
            scrollMapBars[3].name = "downScrollBar";
            stage.addChild(scrollMapBars[3]);
            scrollMapBarTimers[3].addEventListener("timer", scrollDown);
        }
        
        private function stepTimeReceivedCallback():void {
            clientMainLoop.stop();
            clientMainLoop.start();
        }
        
        private function onThrottle(event:ThrottleEvent):void {
            throttle = !throttle;
            tf.text = tf.text.concat(throttle ? ",off" : ",on");
            
            if (throttle) {
                // TODO: Сделать отключение, полную очистку и остановку. Кроме загрузки спрайтов.
            } else {
                // TODO: Запустить клиент заново.
            }
        }
        
        private function onScrollMapBarOver(event:MouseEvent):void {
			var ct:ColorTransform = new ColorTransform();
            var bar:Sprite = event.currentTarget as Sprite;
			ct.color = 0x880000;
			bar.transform.colorTransform = ct;
            
            switch (bar.name) {
                case "leftScrollBar":
                    scrollMapBarTimers[0].start();
                    break;
                case "upScrollBar":
                    scrollMapBarTimers[1].start();
                    break;
                case "rightScrollBar":
                    scrollMapBarTimers[2].start();
                    break;
                case "downScrollBar":
                    scrollMapBarTimers[3].start();
                    break;
            }
        }
        
        private function onScrollMapBarOut(event:MouseEvent):void {
			var ct:ColorTransform = new ColorTransform();
            var bar:Sprite = event.currentTarget as Sprite;
			ct.color = 0xFF0000;
			bar.transform.colorTransform = ct;
            
            switch (bar.name) {
                case "leftScrollBar":
                    scrollMapBarTimers[0].stop();
                    break;
                case "upScrollBar":
                    scrollMapBarTimers[1].stop();
                    break;
                case "rightScrollBar":
                    scrollMapBarTimers[2].stop();
                    break;
                case "downScrollBar":
                    scrollMapBarTimers[3].stop();
                    break;
            }
        }
        
        private function scrollLeft(event:TimerEvent):void {
            Main.camera.x -= 1 * Config.CELL_W;
            if (Main.camera.x < -Config.SCR_HW) {
                Main.camera.x = -Config.SCR_HW;
            }
        }
        
        private function scrollUp(event:TimerEvent):void {
            Main.camera.y -= 1 * Config.CELL_H;
            if (Main.camera.y < -Config.SCR_HH) {
                Main.camera.y = -Config.SCR_HH;
            }
        }
        
        private function scrollRight(event:TimerEvent):void {
            Main.camera.x += 1 * Config.CELL_W;
            if (Main.camera.x > map.width * Config.CELL_W - Config.SCR_HW) {
                Main.camera.x = map.width * Config.CELL_W - Config.SCR_HW;
            }
        }
        
        private function scrollDown(event:TimerEvent):void {
            Main.camera.y += 1 * Config.CELL_H;
            if (Main.camera.y > map.height * Config.CELL_H - Config.SCR_HH) {
                Main.camera.y = map.height * Config.CELL_H - Config.SCR_HH;
            }
        }
        
        private function onEnterFrame(event:Event):void {
            map.update(Main.camera);
            unitManager.update(Main.camera);
            movementManager.updateSpritePositions();
            for (var i:int = 0; i < Main.freeAnimation.length; i++) {
                Main.freeAnimation[i].update(Main.camera);
            }
            if (selectionBeg != null && selectionEnd != null && stage.contains(selectionRect)) {
                selectionRect.graphics.clear();
                selectionRect.graphics.lineStyle(2, 0x000000, 0.75);
                selectionRect.graphics.moveTo(selectionBeg.x, selectionBeg.y);
                selectionRect.graphics.lineTo(selectionEnd.x, selectionBeg.y);
                selectionRect.graphics.lineTo(selectionEnd.x, selectionEnd.y);
                selectionRect.graphics.lineTo(selectionBeg.x, selectionEnd.y);
                selectionRect.graphics.lineTo(selectionBeg.x, selectionBeg.y);
            }
        }
        
        private function keyDown(event:KeyboardEvent):void {
            if (event.keyCode == 37) {  // left arrow
                Main.camera.x -= 1 * Config.CELL_W;
                if (Main.camera.x < -Config.SCR_HW) {
                    Main.camera.x = -Config.SCR_HW;
                }
            } else if (event.keyCode == 39) {   // right arrow
                Main.camera.x += 1 * Config.CELL_W;
                if (Main.camera.x > map.width * Config.CELL_W - Config.SCR_HW) {
                    Main.camera.x = map.width * Config.CELL_W - Config.SCR_HW;
                }
            } else if (event.keyCode == 38) {   // up arrow
                Main.camera.y -= 1 * Config.CELL_H;
                if (Main.camera.y < -Config.SCR_HH) {
                    Main.camera.y = -Config.SCR_HH;
                }
            } else if (event.keyCode == 40) {   // down arrow
                Main.camera.y += 1 * Config.CELL_H;
                if (Main.camera.y > map.height * Config.CELL_H - Config.SCR_HH) {
                    Main.camera.y = map.height * Config.CELL_H - Config.SCR_HH;
                }
            }
        }
        
        private function onMouseDown(event:MouseEvent):void {
            selectionBeg = { x: event.stageX, y: event.stageY };
        }
        
        private function onMouseUp(event:MouseEvent):void {
            if (selectionBeg != null && selectionEnd != null) {
                var tAxisBeg:Object = camera.reTranslateAxis(selectionBeg.x, selectionBeg.y);
                var tAxisEnd:Object = camera.reTranslateAxis(selectionEnd.x, selectionEnd.y);
                var dim:Object = {
                    begJ: Map.getCellJOnMap(tAxisBeg.x),
                    begI: Map.getCellIOnMap(tAxisBeg.y),
                    endJ: Map.getCellJOnMap(tAxisEnd.x),
                    endI: Map.getCellIOnMap(tAxisEnd.y),
                    toString: function():String {
                        return this.begJ + "," + this.begI + ":" + this.endJ + "," + this.endI;
                    }
                }
                unitManager.selectUnitsRect(dim);
            } else {
                //var tAxis:Object = camera.reTranslateAxis(event.stageX, event.stageY);
                unitManager.selectUnits(event.stageX, event.stageY);
            }
            
            selectionBeg = null;
            selectionEnd = null;
            if (stage.contains(selectionRect)) {
                stage.removeChild(selectionRect);
            }
        }
        
        private function onMouseMove(event:MouseEvent):void {
            if (selectionBeg != null) {
                selectionEnd = { x: event.stageX, y: event.stageY };
                
                if (!stage.contains(selectionRect)) {
                    stage.addChild(selectionRect);
                }
            }
        }
        
        private function onMouseRightClick(event:MouseEvent):void {
            var tAxis:Object = camera.reTranslateAxis(event.stageX, event.stageY);
            if (!unitManager.tryFollowing(event.stageX, event.stageY)) {
                actionManager.moveSelectedUnits(new Cell(Map.getCellJOnMap(tAxis.x), Map.getCellIOnMap(tAxis.y)));
            }
        }
        
        /**
         * Перерисовывает миникарту.
         */
        private function updateMinimap():void {
            var i:int, j:int, k:int;
            for (i = 0; i < map.height; i++) {
                for (j = 0; j < map.width; j++) {
                    if (i == 0 || j == 0 || i == map.height - 1 || j == map.width - 1) {
                        // Черная рамка по краям.
                        minimap.bitmapData.setPixel(j, i, 0x000000);
                    } else {
                        // Остальное зелёное.
                        minimap.bitmapData.setPixel(j, i, 0x00FF00);
                    }
                }
            }
            unitManager.markUnitsOnMinimap(minimap);
            var camJ:int = Math.max(0, Map.getCellJOnMap(Main.camera.x));
            var camI:int = Math.max(0, Map.getCellIOnMap(Main.camera.y));
            var camW:int = Math.ceil(Config.SCR_W / Config.CELL_W);
            var camH:int = Math.ceil(Config.SCR_H / Config.CELL_H);
            for (i = camI; i < map.height; i++) {
                for (j = camJ; j < map.width; j++) {
                    if ( ((i == camI || i == camI + camH - 1) && j <= camJ + camW - 1) || (j == camJ || j == camJ + camW - 1) && i <= camI + camH - 1) {
                        // Серая рамка камеры.
                        minimap.bitmapData.setPixel(j, i, 0x808080);
                    }
                }
            }
        }
        
        private function onStepComplete():void {
            updateMinimap();
        }
    }
}
