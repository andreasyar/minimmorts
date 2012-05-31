/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minimmorts.messages;

import minimmorts.units.Unit;

/**
 *
 * @author andrey
 */
public class NewUnitMessage implements Message {
    private long ownerId;
    private long id;
    private int j;
    private int i;

    public NewUnitMessage(long ownerId, long id, int j, int i) {
        this.ownerId = ownerId;
        this.id = id;
        this.j = j;
        this.i = i;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public long getId() {
        return id;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NewUnitMessage) {
            return ((NewUnitMessage) obj).getOwnerId() == this.getOwnerId()
                        && ((NewUnitMessage) obj).getId() == this.getId()
                        && ((NewUnitMessage) obj).getJ() == this.getJ()
                        && ((NewUnitMessage) obj).getI() == this.getI();
        } else if (obj instanceof Unit) {
            return ((Unit) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public String toJSON() {
        return "[\"NEWUNIT\",{\"ownerId\":" + ownerId + ",\"id\":" + id + ",\"j\":" + j + ",\"i\":" + i + "}]";
    }
}
