
// Загрузчик спрайтов.

function SpriteLoader(sprites) {

    // Эту функцию можно вызвать только как конструктор (с new).
    if (this.constructor !== arguments.callee) {
        throw new Error("Constructor called like a simple function!");
    }

    // Ссылка на набор спрайтов из ресурсов.
    this.sprites = sprites;

    /**
     * Возвращает спрайт по его id.
     * @param id Идентификатор спрайта.
     * @return Возвращает спрайт или null, если спрайт не найден.
     */
    this.getSpriteById = function(id) {
        for (var i in this.sprites) {
            if (this.sprites[i].id == id) {
                if (this.sprites[i].image == null) {
                    this.sprites[i].image = new Image();
                    this.sprites[i].image.src = this.sprites[i].src;
                    this.sprites[i].image.oncontextmenu = "return false;"
                }
                return this.sprites[i].image;
            }
        }
        
        return null;
    }

    /**
     * Возвращает спрайт по его имени.
     * @param name Имя спрайта.
     * @return Возвращает спрайт или null, если спрайт не найден.
     */
    this.getSpriteByName = function(name) {
        for (var i in this.sprites) {
            if (this.sprites[i].name == name) {
                if (this.sprites[i].sprite == null) {
                    this.sprites[i].image = new Image();
                    this.sprites[i].image.src = this.sprites[i].src;
                    this.sprites[i].image.oncontextmenu = "return false;"
                    this.sprites[i].sprite = new Sprite(
                        this.sprites[i].image,
                        this.sprites[i].w,
                        this.sprites[i].h,
                        this.sprites[i].base.x,
                        this.sprites[i].base.y
                    );
                }
                return this.sprites[i].sprite;
            }
        }
        
        console.log("Sprite name=" + name + " not found.");
        return null;
    }

    return this;
}
