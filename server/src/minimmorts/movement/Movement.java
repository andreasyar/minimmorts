package minimmorts.movement;

import java.util.LinkedList;
import java.util.logging.Logger;
import minimmorts.units.Unit;

/**
 *
 * @author Sorc
 */
public class Movement {
    private static final Logger logger = Logger.getLogger(MovementManager.class.getName());

    private Unit unit;
    private int begJ;
    private int begI;
    private int endJ;
    private int endI;

    /**
     * Скорость движения в клетках за шаг.
     */
    private float speed;

    LinkedList<Point> path;
    private long begStep;
    private boolean stopped;

    public Movement(Unit unit, int begJ, int begI, int endJ, int endI, float speed, LinkedList<Point> path, long begStep) {
        this.unit = unit;
        this.begJ = begJ;
        this.begI = begI;
        this.endJ = endJ;
        this.endI = endI;
        this.speed = speed;
        this.path = path;
        this.begStep = begStep;
        this.stopped = false;
    }

    public Unit getUnit() {
        return unit;
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

    public boolean isStopped() {
        return this.stopped;
    }

    public long getBegStep() {
        return begStep;
    }

    void updatePosition(long step) {
        int n = (int) Math.floor(((double) step - (double) begStep) * (double) speed);
        /*logger.log(Level.INFO, "{0} {1} {2} {3}",
            new Object[] {
                ((double) step - (double) begStep),
                ((double) step - (double) begStep) * (double) speed,
                Math.floor(((double) step - (double) begStep) * (double) speed),
                n
            }
        );*/
        //logger.log(Level.INFO, "n = {0}, path.size() = {1}", new Object[] {n, path.size()});
        if (n >= path.size()) {
            unit.setJ(path.get(path.size() - 1).getX());
            unit.setI(path.get(path.size() - 1).getY());
            stopped = true;
        } else {
            unit.setJ(path.get(n).getX());
            unit.setI(path.get(n).getY());
        }
    }

    public LinkedList<Point> getPath() {
        return path;
    }
}
