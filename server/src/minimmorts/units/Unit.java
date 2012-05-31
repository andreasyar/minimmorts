package minimmorts.units;

import minimmorts.messages.UnitMessage;

/**
 *
 * @author Sorc
 */
public class Unit {
    private long ownerId;
    private long id;
    private int j;
    private int i;

    public Unit(long ownerId, long id, int j, int i) {
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

    public void setJ(int j) {
        this.j = j;
    }

    public void setI(int i) {
        this.i = i;
    }
    
    public String toJSON() {
        return "[\"UNIT\", " + ownerId + ", " + id + ", " + j + ", " + i + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Unit) {
            return ((Unit) obj).getId() == this.getId();
        } else if (obj instanceof UnitMessage) {
            return ((UnitMessage) obj).getId() == this.getId();
        } else {
            return false;
        }
    }
}
