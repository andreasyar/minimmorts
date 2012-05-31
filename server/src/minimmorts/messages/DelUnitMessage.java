package minimmorts.messages;

import minimmorts.units.Unit;

/**
 * Сообщение об удалении юнита. Отправляется сервером клиенту, когда с карты
 * пропадает юнит, например, при отключении клиента от сервера.
 * @author andrey
 */
public class DelUnitMessage implements Message {
    
    /**
     * Идентификатор удаляемого юнита.
     */
    private long id;

    public DelUnitMessage(long id) {
        this.id = id;
    }

    /**
     * Возвращает идентификатор удаляемого юнита.
     * @return Идентификатор удаляемого юнита.
     */
    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DelUnitMessage) {
            return ((DelUnitMessage) obj).getId() == this.getId();
        } else if (obj instanceof Unit) {
            return ((Unit) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    /**
     * Преобразует сообщение в JSON строку.
     * @return JSON представление сообщения.
     */
    @Override
    public String toJSON() {
        return "[\"DELUNIT\"," + id + "]";
    }
}
