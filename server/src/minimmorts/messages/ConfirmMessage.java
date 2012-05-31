package minimmorts.messages;

import java.util.LinkedList;

/**
 *
 * @author Sorc
 */
public class ConfirmMessage implements Message {
    private long step;
    private LinkedList<Message> messages;

    public ConfirmMessage(long step, LinkedList<Message> messages) {
        this.step = step;
        this.messages = messages;
    }

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
