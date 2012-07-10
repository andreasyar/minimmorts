package minimmorts.messages;

import java.util.LinkedList;
import minimmorts.movement.Point;

/**
 *
 * @author Sorc
 */
public class MoveUpdateMessage implements Message {
    private long begStep;
    private long unitId;
    private final LinkedList<Point> waypoints;

    MoveUpdateMessage(long begStep, long unitId, LinkedList<Point> waypoints) {
        this.begStep = begStep;
        this.unitId = unitId;
        this.waypoints = waypoints;
    }

    public long getBegStep() {
        return begStep;
    }

    public long getUnitId() {
        return unitId;
    }

    @Override
    public String toJSON() {
        String out = "[\"MOVEUPDATE\",{\"begStep\":" + begStep
                     + ",\"unitId\":" + unitId
                     + ",\"waypoints\":[";
        for (int i = 0; i < waypoints.size() - 1; i++) {
            out += "{\"j\":" + waypoints.get(i).getJ() + ",\"i\":" + waypoints.get(i).getI() + "},";
        }
        out += "{\"j\":" + waypoints.getLast().getJ() + ",\"i\":" + waypoints.getLast().getI() + "}]}]";
        return out;
    }
}
