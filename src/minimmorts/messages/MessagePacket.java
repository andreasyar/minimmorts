/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minimmorts.messages;

import java.util.LinkedList;

/**
 *
 * @author Sorc
 */
public class MessagePacket implements Message {
    private LinkedList<Message> messages;

    public MessagePacket(LinkedList<Message> messages) {
        this.messages = messages;
    }

    public MessagePacket(Message message) {
        this.messages = new LinkedList<>();
        this.messages.add(message);
    }

    @Override
    public String toJSON() {
        String out = "[";
        for (int i = 0; i < messages.size() - 1; i++) {
                out += messages.get(i).toJSON() + ",";
        }
        out += messages.get(messages.size() - 1).toJSON() + "]";
        return out;
    }
}
