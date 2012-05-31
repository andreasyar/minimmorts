package minimmorts.messages;

import minimmorts.units.Unit;

/**
 *
 * @author Sorc
 */
public class UnitMessage implements Message {
    private long ownerId;
    private long id;
    private int j;
    private int i;

    public UnitMessage(long ownerId, long id, int j, int i) {
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
        if (obj instanceof UnitMessage) {
            return ((UnitMessage) obj).getOwnerId() == this.getOwnerId()
                        && ((UnitMessage) obj).getId() == this.getId()
                        && ((UnitMessage) obj).getJ() == this.getJ()
                        && ((UnitMessage) obj).getI() == this.getI();
        } else if (obj instanceof Unit) {
            return ((Unit) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public String toJSON() {
        return "[\"UNIT\",{\"ownerId\":" + ownerId + ",\"id\":" + id + ",\"j\":" + j + ",\"i\":" + i + "}]";
    }
}
