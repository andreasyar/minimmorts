package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class SyncErrorMessage implements Message {
    private long unitId;

    public SyncErrorMessage(long unitId) {
        this.unitId = unitId;
    }

    @Override
    public String toJSON() {
        return "[\"SYNCERROR\", {\"unitId\":" + unitId + "}]";
    }
}
