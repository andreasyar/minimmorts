
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
function MoveMessage(begStep, unitId, begJ, begI, endJ, endI) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "MOVE";

    /**
     * Номер шага начала движения (игнорируется сервером).
     */
    this.begStep = begStep;

    /**
     * Идентификатор движущегося юнита.
     */
    this.unitId = unitId;

    /**
     * j-индекс клетки отправления (игнорируется сервером).
     */
    this.begJ = begJ;

    /**
     * i-индекс клетки отправления (игнорируется сервером).
     */
    this.begI = begI;

    /**
     * j-индекс клетки назначения.
     */
    this.endJ = endJ;

    /**
     * i-индекс клетки назначения.
     */
    this.endI = endI;

    /**
     * Преобразование в JSON.
     */
    this.toJSON = function() {
        return new Array(this.type, {unitId: this.unitId, begJ: this.begJ, begI: this.begI, endJ: this.endJ, endI: this.endI});
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
    "ERROR": function (msg) {
        // Format:
        // ["ERROR", "error text"]
        
        alert(msg[1]);
        disconnect();
    },
    "WELCOME": function (msg) {
        // Format:
        // ["WELCOME",clientId,step,[units],[actions]]
        
        var i;
        
        clientId = msg[1];
        step  = msg[2];
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
        
        actions = new Array();
        stored_actions = new Array();
        selected_units = new Array();
        movementManager = new MovementManager();
        
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
        
        setTimeout("drawLoop();", 1000 / FPS);
        setTimeout("pingLoop();", 100);
        syncLoop();
        scr.addEventListener("mousedown", scrOnMousedown, false);
        scr.addEventListener("mouseup", scrOnMouseup, false);
        hideLoginForm();
    },
    "CONFIRM": function (msg) {
        if (step == msg[1]) {
            for (var i in msg[2]) {
                MESSAGE_HANDLERS[msg[2][i][0]](msg[2][i]);
            }
            
            syncLoop();
        } else {
            console.log("Cant wake up. Step", msg[1], "confirmed but we are waiting for confirmation of step", step);
        }
    },
    "STATE": function (msg) {
        // [["STATE",3349,[["UNIT",{"ownerId":13,"id":14,"j":4,"i":4,"r":128,"g":0,"b":0}],["UNIT",{"ownerId":13,"id":15,"j":5,"i":5,"r":128,"g":0,"b":0}]],[["MOVE",{"unitId":14,"j":6,"i":9}],["MOVE",{"unitId":15,"j":7,"i":9}]]]]
        
        if (!is_initial_state_received) {
            is_initial_state_received = true;
            setTimeout("drawLoop();", 1000 / FPS);
            setTimeout("syncLoop();", 100);
            scr.addEventListener("mousedown", scrOnMousedown, false);
            scr.addEventListener("mouseup", scrOnMouseup, false);
            camera = new Camera();
            map = new Map(100, 100);
        }
        self_units_list = new Array();
        units_list = new Array();
        actions = new Array();

        step = msg[1];
        var units = msg[2];
        var recv_actions = msg[3];
        var cur_units_list;
        var action_type;
        var i;

        for (i in units) {
            if (clientId == units[i][1].ownerId) {
                cur_units_list = self_units_list;
            } else {
                cur_units_list = units_list;
            }
            cur_units_list.push(new Unit(units[i][1].ownerId, units[i][1].id, units[i][1].j, units[i][1].i, {r:units[i][1].r,g:units[i][1].g,b:units[i][1].b}));
        }

        for (i in recv_actions) {
            action_type = recv_actions[i][0];
            switch (action_type) {
                case "MOVE":
                    
                    break;
            }
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
            console.log("Юнит id=" + moveMessage.unitId + " не найден. Движение не может быть создано.");
            return;
        }

        movementManager.startNewMovement(
            moveMessage.begStep,
            unit,
            moveMessage.begJ,
            moveMessage.begI,
            moveMessage.endJ,
            moveMessage.endI
        );
    },
    "MOVEUPDATE": function (message) {
        // Example: ["MOVEUPDATE",{"begStep":16776,"unitId":35,"begJ":9,"begI":7,"endJ":3,"endI":9}]

        var moveMessage = message[1];
        var unit = getUnitById(moveMessage.unitId);
        
        if (unit == null) {
            console.log("Юнит id=" + moveMessage.unitId + " не найден. Движение не может быть обновлено.");
            return;
        }

        if (moveMessage.begStep == step) {
            movementManager.updateMovement(
                moveMessage.begStep,
                unit,
                moveMessage.endJ,
                moveMessage.endI
            );
        } else {
            console.log("Получен запрос изменения не за текущий шаг! Текущий шаг " + step + " шаг изменения пути " + moveMessage.begStep + " Запрос проигнорирован.");
        }
    }
}
