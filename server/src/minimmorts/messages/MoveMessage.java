package minimmorts.messages;

import java.util.LinkedList;
import minimmorts.movement.Point;

/**
 *
 * @author Sorc
 */
public class MoveMessage implements Message {
    private long begStep;
    private long unitId;
    private final LinkedList<Point> waypoints;
    
    public MoveMessage(long begStep, long unitId, int begJ, int begI, int endJ, int endI) {
        this.begStep = begStep;
        this.unitId = unitId;
        waypoints = new LinkedList<>();
        waypoints.add(new Point(begJ, begI));
        waypoints.add(new Point(endJ, endI));
    }

    public MoveMessage(long begStep, long unitId, LinkedList<Point> waypoints) {
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
        return waypoints.getFirst().getJ();
    }

    public int getBegI() {
        return waypoints.getFirst().getI();
    }

    public int getEndJ() {
        return waypoints.getLast().getJ();
    }

    public int getEndI() {
        return waypoints.getLast().getI();
    }

    @Override
    public String toJSON() {
        String out = "[\"MOVE\",{\"begStep\":" + begStep
                     + ",\"unitId\":" + unitId
                     + ",\"waypoints\":[";
        for (int i = 0; i < waypoints.size() - 1; i++) {
            out += "{\"j\":" + waypoints.get(i).getJ() + ",\"i\":" + waypoints.get(i).getI() + "},";
        }
        out += "{\"j\":" + waypoints.getLast().getJ() + ",\"i\":" + waypoints.getLast().getI() + "}]}]";
        return out;
    }
}
