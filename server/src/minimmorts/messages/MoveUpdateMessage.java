package minimmorts.messages;

import java.util.ArrayList;
import minimmorts.movement.Point;

/**
 *
 * @author Sorc
 */
public class MoveUpdateMessage implements Message {
    private long begStep;
    private long unitId;
    private final ArrayList<Point> waypoints;

    MoveUpdateMessage(long begStep, long unitId, ArrayList<Point> waypoints) {
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
        StringBuilder out = new StringBuilder();
        out.append("[\"MOVEUPDATE\",{\"begStep\":");
        out.append(begStep);
        out.append(",\"unitId\":");
        out.append(unitId);
        out.append(",\"waypoints\":[");
        if (waypoints.size() > 0) {
            for (int i = 0; i < waypoints.size() - 1; i++) {
                out.append("{\"j\":");
                out.append(waypoints.get(i).getJ());
                out.append(",\"i\":");
                out.append(waypoints.get(i).getI());
                out.append("},");
            }
            out.append("{\"j\":");
            out.append(waypoints.get(waypoints.size() - 1).getJ());
            out.append(",\"i\":");
            out.append(waypoints.get(waypoints.size() - 1).getI());
            out.append("}]}]");
        } else {
            out.append("]}]");
        }
        return out.toString();
    }
}
