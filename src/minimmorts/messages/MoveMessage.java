package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class MoveMessage implements Message {
    private long begStep;
    private long unitId;
    private int j;
    private int i;

    public MoveMessage(long begStep, long unitId, int j, int i) {
        this.begStep = begStep;
        this.unitId = unitId;
        this.j = j;
        this.i = i;
    }

    public long getBegStep() {
        return begStep;
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
        return "[\"MOVE\",{\"begStep\":" + begStep + ",\"unitId\":" + unitId + ",\"j\":" + j + ",\"i\":" + i + "}]";
    }
}
