package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class MoveUpdateMessage implements Message {
    private long begStep;
    private long unitId;
    private int begJ;
    private int begI;
    private int endJ;
    private int endI;

    MoveUpdateMessage(long begStep, long unitId, int begJ, int begI, int endJ, int endI) {
        this.begStep = begStep;
        this.unitId = unitId;
        this.begJ = begJ;
        this.begI = begI;
        this.endJ = endJ;
        this.endI = endI;
    }

    public long getBegStep() {
        return begStep;
    }

    public long getUnitId() {
        return unitId;
    }

    public int getBegJ() {
        return begJ;
    }

    public int getBegI() {
        return begI;
    }

    public int getEndJ() {
        return endJ;
    }

    public int getEndI() {
        return endI;
    }

    @Override
    public String toJSON() {
        return "[\"MOVEUPDATE\",{\"begStep\":" + begStep
                       + ",\"unitId\":" + unitId
                       + ",\"begJ\":" + begJ
                       + ",\"begI\":" + begI
                       + ",\"endJ\":" + endJ
                       + ",\"endI\":" + endI + "}]";
    }
}
