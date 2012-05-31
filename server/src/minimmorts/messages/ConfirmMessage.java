package minimmorts.messages;

import java.util.LinkedList;

/**
 * Сообщение о подтверждении состояния. Отправляется сервером клиенту, если
 * состояние клиента было проверено и подтверждена правильность состояния.
 * @author Sorc
 */
public class ConfirmMessage implements Message {
    
    /**
     * Номер шага, состояние которого подтверждается.
     */
    private long step;
    
    /**
     * Уведомления клиенту о событиях, произошедших на подтвержденном шагу.
     */
    private LinkedList<Message> messages;

    public ConfirmMessage(long step, LinkedList<Message> messages) throws NullPointerException {
        this.step = step;
        if (messages != null) {
            this.messages = messages;
        } else {
            throw new NullPointerException("Messages list is null.");
        }
    }

    /**
     * Преобразует сообщение в JSON строку.
     * @return JSON представление сообщения.
     */
    @Override
    public String toJSON() {
        String out = "[\"CONFIRM\"," + step + ",[";
        if (messages.size() > 0) {
            for (int i = 0; i < messages.size() - 1; i++) {
                out += messages.get(i).toJSON() + ",";
            }
            out += messages.get(messages.size() - 1).toJSON() + "]]";
        } else {
            out += "]]";
        }
        return out;
    }
}
