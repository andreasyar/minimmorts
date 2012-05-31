
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
function MoveMessage(unitId, j, i) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    /**
     * Тип сообщения.
     */
    this.type = "MOVE";

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

// Обработчики сообщений.
var MESSAGE_HANDLERS = {
    "ERROR": function (msg) {
        // ["ERROR", "Some text"]
        
        alert(msg[1]);
        disconnect();
    },
    "WELCOME": function (msg) {
        clientId = msg[1];
        step  = msg[2];
        
        for (var i in msg[3]) {
            MESSAGE_HANDLERS[msg[3][i][0]](msg[3][i]);
        }
        
        setTimeout("drawLoop();", 1000 / FPS);
        setTimeout("syncLoop();", 100);
        scr.addEventListener("mousedown", scrOnMousedown, false);
        scr.addEventListener("mouseup", scrOnMouseup, false);
        camera = new Camera();
        map = new Map(100, 100);
        hideLoginForm();
    },
    "CONFIRM": function (msg) {
        if (step == msg[1]) {
            for (var i in msg[2]) {
                MESSAGE_HANDLERS[msg[2][i][0]](msg[2][i]);
            }
            
            awaiting_confirm = false;
            setTimeout("syncLoop();", 0);
        } else {
            console.log("Cant wake up. Step", msg[1], "confirmed but we are waiting for confirmation of step", step);
        }
    },
    "STATE": function (msg) {
        // [["STATE",3349,[["UNIT",{"ownerId":13,"id":14,"j":4,"i":4}],["UNIT",{"ownerId":13,"id":15,"j":5,"i":5}]],[["MOVE",{"unitId":14,"j":6,"i":9}],["MOVE",{"unitId":15,"j":7,"i":9}]]]]
        
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
            cur_units_list.push(new Unit(units[i][1].ownerId, units[i][1].id, units[i][1].j, units[i][1].i));
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
        // ["NEWUNIT",{"ownerId":10,"id":11,"j":4,"i":4}]

        var unit = msg[1];
        var cur_units_list;

        if (clientId == unit.ownerId) {
            cur_units_list = self_units_list;
        } else {
            cur_units_list = units_list;
        }
        cur_units_list.push(new Unit(unit.ownerId, unit.id, unit.j, unit.i));
    },
    "DELUNIT": function (msg) {
        // ["DELUNIT",11]

        delUnitById(msg[1]);
    },
    "MOVE": function (msg) {
        // Example: ["MOVE",{"begStep":237,"unitId":5,"j":13,"i":10}]

        var move_message = msg[1];

        movementManager.startMovement(move_message.begStep, getUnitById(move_message.unitId), move_message.j, move_message.i);
    }
}

function UNITSTATUS_PLAYER(player) {
    var i, chat_field

    if (self_player == undefined) {

        // Создадим своего игрока.
        if (MESSAGE_HANDLERS["HELLO"].selfPlayerId == player.id) {
            self_player = new Player(
                player.id,
                player.mapId,
                player.x,
                player.y,
                player.name,
                new SpriteLoader(SPRITES),
                player.speed
            )
            camera = new Camera(self_player)
            for (i in maps) {
                maps[i].player = self_player
                maps[i].camera = camera
            }

            // Запускаем синхронизацию игрового времени.
            timeSync.run(5000)

            // Запускаем главный цикл.
            setTimeout("mainLoop();", 1000 / FPS)

            hideLoginForm()
            showChat()
            scr.addEventListener("click", scrOnClick, false)
        } else {
            console.log("Свой игрок уже создан. Сообщение будет "
                        + "проигнорировано.")
        }
        return
    } else if (self_player.id == player.id) {

        // Изменим атрибуты своего игрока.
        self_player.j = player.x
        self_player.i = player.y
        self_player.name = player.name
        //players_list[i].spriteCategory = player.spriteCategory
        self_player.speed = player.speed
        return
    }

    for (i in players_list) {

        // Изменим атрибуты существующего игрока.
        if (players_list[i].id == player.id) {

            players_list[i].j = player.x
            players_list[i].i = player.y
            players_list[i].name = player.name
            //players_list[i].spriteCategory = player.spriteCategory
            players_list[i].speed = player.speed
            return
        }
    }

    // Если игрок не найден, то добавим нового игрока.
    players_list.push(new Player(
        player.id,
        player.mapId,
        player.x,
        player.y,
        player.name,
        new SpriteLoader(SPRITES),
        player.speed
    ))
    chat_field = document.getElementById("chat_field")
    chat_field.value += "Player " + player.name + " logged in.\n"
    chat_field.scrollTop = chat_field.scrollHeight
}
function UNITSTATUS_NPC(npc) {
    for (var i in npc_list) {

        // Изменим атрибуты существующего NPC
        if (npc_list[i].id == npc.id) {

            npc_list[i].j = npc.x
            npc_list[i].i = npc.y
            npc_list[i].name = npc.name
            //npc_list[i].spriteCategory = npc.spriteCategory
            npc_list[i].speed = npc.speed
            return
        }
    }

    // Если NPC не найден, то добавим нового NPC.
    npc_list.push(new NPC(
        npc.id,
        npc.x,
        npc.y,
        npc.name,
        new SpriteLoader(SPRITES),
        npc.speed
    ))
}

function UNITUPD_PLAYER(player) {
    for (var i in players_list) {

        // Изменим атрибуты существующего NPC
        if (players_list[i].id == player.id) {

            //players_list[i].spriteCategory = player.spriteCategory
            players_list[i].speed = player.speed
            return
        }
    }
}
function UNITUPD_NPC(npc) {
    for (var i in npc_list) {

        // Изменим атрибуты существующего NPC
        if (npc_list[i].id == npc.id) {

            //npc_list[i].spriteCategory = npc.spriteCategory
            npc_list[i].speed = npc.speed
            return
        }
    }
}

function DELUNIT_PLAYER(id) {
    var chat_field

    if (self_player.id == id) {
        // TODO отключение игрока от сервера
        return
    }

    for (var i in players_list) {
        if (players_list[i].id == id) {
            chat_field = document.getElementById("chat_field")
            chat_field.value += "Player " + players_list[i].name + " logged out.\n"
            chat_field.scrollTop = chat_field.scrollHeight
            players_list.splice(i, 1)
            break
        }
    }
}
function DELUNIT_NPC(id) {
    for (var i in npc_list) {
        if (npc_list[i].id == id) {
            npc_list.splice(i, 1)
            break
        }
    }
}
