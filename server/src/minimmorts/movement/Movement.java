package minimmorts.movement;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.GlobalLogFile;
import minimmorts.server.ServerMainLoop;
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
    
    public static void initLogging() {
        try {
            FileHandler fh = new FileHandler(Movement.class.getName() + ".txt");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

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
        return path.get(0).getJ();
    }

    public int getBegI() {
        return path.get(0).getI();
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
        int i;
        int j;
        if (n >= path.size() - 1) {
            j = path.get(path.size() - 1).getJ();
            i = path.get(path.size() - 1).getI();
            if (MovementManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Юнит {1} сходил из клетки {2} в клетку {3} Движение звершено.",
                    new Object[] {ServerMainLoop.getStep(), unit, new Point(unit.getJ(), unit.getI()), new Point(j, i)}
                );
            }
            unit.setJ(j);
            unit.setI(i);
            stopped = true;
        } else {
            j = path.get(n).getJ();
            i = path.get(n).getI();
            if (MovementManager.isLoggingEnabled()) {
                logger.log(
                    Level.INFO,
                    "{0}: Юнит {1} сходил из клетки {2} в клетку {3}",
                    new Object[] {ServerMainLoop.getStep(), unit, new Point(unit.getJ(), unit.getI()), new Point(j, i)}
                );
            }
            unit.setJ(j);
            unit.setI(i);
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
