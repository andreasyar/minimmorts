/* *****************************************************************************
 * Сообщения для обмена данными с сервером и обработчики сообщений от сервера. *
 *******************************************************************************/

/**
 * Запрос входа в игру.
 * @param login Логин.
 * @param password Пароль.
 */
function LoginMessage(login, password) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Логин.
     */
    this.login = login;

    /**
     * Пароль.
     */
    this.password = password;

    /**
     * Преобразовывает сообщение в формат Protocol-1.
     */
    this.toProtocol1 = function() {
        return "LOGIN," + this.login + "," + this.password;
    }

    return this;
}

/**
 * Требование хода.
 * @param unitId Идентификатор движущегося юнита.
 * @param j j-индекс клетки назначения.
 * @param i i-индекс клетки назначения.
 */
function MoveRequestMessage(unitId, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Идентификатор движущегося юнита.
     */
    this.unitId = unitId;

    /**
     * j-индекс клетки назначения.
     */
    this.j = j;

    /**
     * i-индекс клетки назначения.
     */
    this.i = i;

    /**
     * Преобразовывает сообщение в формат Protocol-1.
     */
    this.toProtocol1 = function() {
        return "MOVEREQ," + this.unitId + "," + this.j + "," + this.i;
    }

    return this;
}

/**
 * Отчёт о состоянии клиента.
 * @param step Номер шага.
 * @param unitsCount Количество юнитов в строке юнитов.
 * @param unitsString Строка юнитов.
 * @param actionsCount Количество действий в строке действий.
 * @param actionsString Строка действий.
 */
function StateReportMessage(step, unitsCount, unitsString, actionsCount, actionsString) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Номер шага.
     */
    this.step = step;

    /**
     * Количество юнитов в строке юнитов.
     */
    this.unitsCount = unitsCount;

    /**
     * Строка юнитов.
     */
    this.unitsString = unitsString;

    /**
     * Количество действий в строке действий.
     */
    this.actionsCount = actionsCount;

    /**
     * Строка действий.
     */
    this.actionsString = actionsString;
    
    /**
     * Преобразовывает сообщение в формат Protocol-1.
     */
    this.toProtocol1 = function() {
        var out = "STATE," + this.step + "," + this.unitsCount;
        if (this.unitsCount > 0) {
            out += "," + this.unitsString;
        }
        out += "," + this.actionsCount;
        if (this.actionsCount > 0) {
            out += "," + this.actionsString;
        }
        return out;
    }
}

/**
 * Диагностическое сообщение.
 * @param time Время.
 */
function PingMessage(time) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Время.
     */
    this.time = time;
    
    /**
     * Преобразовывает сообщение в формат Protocol-1.
     */
    this.toProtocol1 = function() {
        return "PING," + this.time;
    }

    return this;
}

/**
 * Обработчики сообщений.
 */
var MESSAGE_HANDLERS = {
    "ERROR": function (message) {
        // Format:
        // ["ERROR", "error text"]
        
        clearTimeouts();
        disconnect();
        alert(message[1]);
    },
    "SYNCERROR": function (message) {
        // ["SYNCERROR", {"unitId":123}]
        
        var syncerrorMessage = message[1];
        var unit = getUnitById(syncerrorMessage.unitId);
            
        if (unit == null) {
            console.log("Юнит id=" + syncerrorMessage.unitId + " не найден.");
        }
        
        clearTimeouts()
        disconnect();
        alert("Ошибка синхронизации. Юнит id=" + syncerrorMessage.unitId);
    },
    "WELCOME": function (msg) {
        // Format:
        // ["WELCOME",clientId,step,[units],[actions]]
        
        var i;
        
        clientId = msg[1];
        step = msg[2];
        camera = new Camera(null);
        map = new Map(100, 100);
        
        self_units_list = new Array();
        if (DEBUG_UNITS) {
            console.log("Все свои юниты удалены.");
        }
        
        units_list = new Array();
        if (DEBUG_UNITS) {
            console.log("Все чужие юниты удалены.");
        }
        
        selected_units = new Array();
        movementManager = new MovementManager();
        actionManager = new ActionManager();
        followings = [];
        
        /*
         * Создание юнитов.
         */
        for (i in msg[3]) {
            MESSAGE_HANDLERS[msg[3][i][0]](msg[3][i]);
        }
        
        /*
         * Выполнение действий.
         */
        for (i in msg[4]) {
            MESSAGE_HANDLERS[msg[4][i][0]](msg[4][i]);
        }
        
        clientLoopFreq = msg[5];
        console.log(clientLoopFreq);
        clearTimeouts();
        drawTimer = setTimeout(drawLoop, 1000 / FPS);
        syncTimer = setInterval(syncLoop, clientLoopFreq);
        pingTimer = setTimeout(pingLoop, 100);
        followingsTimer = setTimeout(followLoop, 500);
        scr.addEventListener("mousedown", scrOnMousedown, false);
        scr.addEventListener("mouseup", scrOnMouseup, false);
        scr.addEventListener("mousemove", scrOnMousemove, false);
        hideLoginForm();
    },
    "CONFIRM": function (msg) {
        if (idleMode) {
            if (step == msg[1]) {
                for (var i in msg[2]) {
                    MESSAGE_HANDLERS[msg[2][i][0]](msg[2][i]);
                }

                syncLoop();
            } else {
                console.log("Cant wake up. Step", msg[1], "confirmed but we are waiting for confirmation of step", step);
            }
        } else {
            addConfirm(msg);
        }
    },
    "NEWUNIT": function (msg) {
        // Example:
        // ["NEWUNIT",{"ownerId":10,"id":11,"j":4,"i":4,"r":128,"g":0,"b":0}]

        var unit = msg[1];
        var cur_units_list;

        if (clientId == unit.ownerId) {
            cur_units_list = self_units_list;
            
            /*
             * Камера будет смотреть на последнего добавленного юнита игрока.
             */
            camera.x = Map.getCellXOnMap(unit.j - (SCR_W / CELL_W) / 2);
            camera.y = Map.getCellYOnMap(unit.i - (SCR_H / CELL_H) / 2);
        } else {
            cur_units_list = units_list;
        }
        cur_units_list.push(new Unit(unit.ownerId, unit.id, unit.j, unit.i, {r:unit.r,g:unit.g,b:unit.b}));
        if (DEBUG_UNITS) {
            console.log("Создан юнит id=" + unit.id);
        }
    },
    "DELUNIT": function (msg) {
        // Example:
        // ["DELUNIT",11]

        if (!delUnitById(msg[1]) && DEBUG_UNITS) {
            console.log("Юнит не может быть удалён. Юнит id=" + msg[1] + " не найден.");
        } else if (DEBUG_UNITS) {
            console.log("Удалён юнит id=" + msg[1]);
        }
    },
    "MOVE": function (message) {
        // Example: ["MOVE",{"begStep":237,"unitId":5,"begJ":0,"begI":1,"endJ":13,"endI":10}]

        var moveMessage = message[1];
        var unit = getUnitById(moveMessage.unitId);
        
        if (unit == null) {
            console.log("Движение не может быть создано. Юнит id=" + moveMessage.unitId + " не найден.");
            return;
        }

        movementManager.startNewMovement(
            moveMessage.begStep,
            unit,
            moveMessage.waypoints
        );
    },
    "MOVEUPDATE": function (message) {
        // Example: ["MOVEUPDATE",{"begStep":68,"unitId":43,"waypoints":[{"j":53,"i":83},{"j":52,"i":82},{"j":96,"i":89}]}]

        var moveMessage = message[1];
        var unit = getUnitById(moveMessage.unitId);
        
        if (unit == null) {
            console.log("Движение не может быть обновлено. Юнит id=" + moveMessage.unitId + " не найден.");
            return;
        }

        movementManager.updateMovement(
            moveMessage.begStep,
            unit,
            moveMessage.waypoints
        );
    },
    "PING": function(message) {
        var messageArray = message.split(",");
        ping = (new Date()).getTime() - messageArray[1];
    },
    "STEPTIME": function(message) {
        console.log(step + ": Время хода изменилось на " + message[1]);
        clientLoopFreq = message[1];
        clearInterval(syncTimer);
        syncTimer = setInterval(syncLoop, clientLoopFreq);
    }
}
