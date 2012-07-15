package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class MoveRequestMessage implements Message {
    private long unitId;
    private int j;
    private int i;

    public MoveRequestMessage(long unitId, int j, int i) {
        this.unitId = unitId;
        this.j = j;
        this.i = i;
    }

    public long getUnitId() {
        return unitId;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }
    
    @Override
    public String toJSON() {
        return "[\"MOVEREQ\",{\"unitId\":" + unitId + ",\"j\":" + j + ",\"i\":" + i + "}]";
    }
}
