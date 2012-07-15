package minimmorts.messages;

import java.util.ArrayList;
import minimmorts.movement.Point;

/**
 *
 * @author Sorc
 */
public class MoveMessage implements Message {
    private long begStep;
    private long unitId;
    private final ArrayList<Point> waypoints;
    
    public MoveMessage(long begStep, long unitId, int begJ, int begI, int endJ, int endI) {
        this.begStep = begStep;
        this.unitId = unitId;
        waypoints = new ArrayList<>();
        waypoints.add(new Point(begJ, begI));
        waypoints.add(new Point(endJ, endI));
    }

    public MoveMessage(long begStep, long unitId, ArrayList<Point> waypoints) {
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
    
    public int getBegJ() {
        return waypoints.get(0).getJ();
    }

    public int getBegI() {
        return waypoints.get(0).getI();
    }

    public int getEndJ() {
        return waypoints.get(waypoints.size() - 1).getJ();
    }

    public int getEndI() {
        return waypoints.get(waypoints.size() - 1).getI();
    }

    @Override
    public String toJSON() {
        StringBuilder out = new StringBuilder();
        out.append("[\"MOVE\",{\"begStep\":");
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
