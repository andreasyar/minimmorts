package minimmorts.movement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.units.Unit;

/**
 *
 * @author Sorc
 */
public class Movement {
    private static final Logger logger = Logger.getLogger(Movement.class.getName());

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
        if (n >= path.size() - 1) {
            unit.setJ(path.get(path.size() - 1).getJ());
            unit.setI(path.get(path.size() - 1).getI());
            //logger.log(Level.INFO, "Unit {0} comes to [{1}, {2}] on step {3} Final", new Object[] {unit.getId(), unit.getJ(), unit.getI(), step});
            stopped = true;
        } else {
            unit.setJ(path.get(n).getJ());
            unit.setI(path.get(n).getI());
            //logger.log(Level.INFO, "Unit {0} comes to [{1}, {2}] on step {3}", new Object[] {unit.getId(), unit.getJ(), unit.getI(), step});
        }
    }

    public LinkedList<Point> getPath() {
        return path;
    }

    Point getNextPathPoint(int j, int i) {
        Iterator pathIterator = path.iterator();
        Point p;
        while (pathIterator.hasNext()) {
            p = (Point) pathIterator.next();
            if (p.getJ() == j && p.getI() == i) {
                if (pathIterator.hasNext()) {
                    return (Point) pathIterator.next();
                } else {
                    return p;
                }
            }
        }
        
        return null;
    }
}
