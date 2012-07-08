
// Сообщения дла взаимодействия с сервером.

/**
 * Сообщение входа в игру игрока.
 */
function LoginMessage(login, password) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "LOGIN";

    /**
     * Логин.
     */
    this.login = login;

    /**
     * Пароль.
     */
    this.password = password;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {login:this.login, password:this.password})
    }

    return this;
}

/**
 * Сообщение о выходе игрока из игры.
 */
function LogoutMessage() {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "LOGOUT";

    /**
     * Преобразование в JSON. Временно.
     */
    this.toJSON = function() {
        return JSON.stringify(new Array(this.type))
    }

    return this;
}

/**
 * Требование хода.
 */
function MoveRequestMessage(unitId, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "MOVEREQ";

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
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {unitId: this.unitId, j: this.j, i: this.i});
    }

    return this;
}

/**
 * Отчёт о состоянии клиента.
 * @param step Номер шага.
 * @param units Массив сообщений с состоянием юнитов.
 * @param actions Массив действий.
 */
function StateReportMessage(step, units, actions) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "STATE";

    /**
     * Номер шага.
     */
    this.step = step;

    /**
     * Список юнитов.
     */
    this.units = units;

    /**
     * Список действий.
     */
    this.actions = actions;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, this.step, this.units, this.actions);
    }
}

/**
 * Сообщение с состоянием юнита.
 */
function UnitMessage(ownerId, id, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "UNIT";

    /**
     * Идентификатор владельца (клиента) юнита.
     */
    this.ownerId = ownerId;

    /**
     * Идентификатор юнита.
     */
    this.id = id;

    /**
     * Номер клетки на карте по ширине, где находится юнит.
     */
    this.j = j;

    /**
     * Номер клетки на карте по высоте, где находится юнит.
     */
    this.i = i;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {ownerId: this.ownerId, id: this.id, j: this.j, i: this.i});
    }
}

/**
 * Пакет сообщений. Обёртка для одновременной отправки массива сообщений.
 * @param messages Массив сообщений.
 */
function MessagePacket(messages) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Массив сообщений.
     */
    this.messages = messages;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.messages);
    }

    return this;
}

/**
 * Диагностическое сообщение.
 * @param time текущее время клиента
 */
function PingMessage(time) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "PING";

    /**
     * Текущее время клиента.
     */
    this.time = time;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {time: this.time});
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

        autoReconnect = false;
        clearTimeouts()
        disconnect();
        alert("Ошибка синхронизации. Юнит id=" + syncerrorMessage.unitId);
    },
    "WELCOME": function (msg) {
        // Format:
        // ["WELCOME",clientId,step,[units],[actions]]

        var i;

        clientId = msg[1];
        document.title = "MINIMMORTS HTML5 bot " + clientId;
        step = msg[2];
        camera = new Camera();
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

        clearTimeouts();
        aiTimer = setTimeout(aiLoop, 2000);
        syncLoop();
    },
    "CONFIRM": function (msg) {
        if (ignoreConfirmProbability != null && getRandomInt(ignoreConfirmProbability) == 0) {
            if (DEBUG_PROTOCOL) {
                console.log("Эмуляция лагов. Сообщение CONFIRM будет проигнорировано.")
            }
            return;
        }

        if (step == msg[1]) {
            for (var i in msg[2]) {
                MESSAGE_HANDLERS[msg[2][i][0]](msg[2][i]);
            }

            syncLoop();
        } else {
            console.log("Cant wake up. Step", msg[1], "confirmed but we are waiting for confirmation of step", step);
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
        // Example:

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
        // Example: 

        var moveMessage = message[1];
        var unit = getUnitById(moveMessage.unitId);
        
        if (unit == null) {
            console.log("Движение не может быть обновлено. Юнит id=" + moveMessage.unitId + " не найден.");
            return;
        }

        movementManager.updateMovement(
            unit,
            moveMessage.waypoints
        );
    },
    "PING": function(message) {
        ping = (new Date()).getTime() - message[0][1].time;
    }
}
