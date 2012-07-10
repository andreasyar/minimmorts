package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class MoveRequestMessage implements Message {
    private long unitId;
    private int j;
    private int i;

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
