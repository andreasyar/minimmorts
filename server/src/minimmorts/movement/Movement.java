package minimmorts.movement;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
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
    
    /**
     *
     */
    private static Logger logger = null;

    /**
     * Флаг включения логирования.
     */
    private static boolean loggingEnabled = false;

    public static void enableLogging() {
        Movement.loggingEnabled = true;
    }

    public static void disableLogging() {
        Movement.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return Movement.loggingEnabled;
    }

    private Unit unit;

    /**
     * Скорость движения в клетках за шаг.
     */
    private float speed;

    LinkedList<Point> path;
    private long begStep;
    private boolean stopped;
    
    private final LinkedList<Point> waypoints;
    
    public static void initLogging() {
        try {
            FileHandler fh = new FileHandler(Movement.class.getName() + ".txt");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        } catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public Movement(Unit unit, LinkedList<Point> waypoints, float speed, LinkedList<Point> path, long begStep) throws NullPointerException, IOException {
        if (Movement.logger == null) {
            Movement.logger = Logger.getLogger(Movement.class.getName());
            FileHandler h = new FileHandler(Movement.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            Movement.logger.addHandler(h);
            Movement.logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            Movement.logger.setLevel(Level.ALL);
        }
        if (path == null) {
            throw new NullPointerException("path is null");
        }
        this.unit = unit;
        this.waypoints = waypoints;
        this.speed = speed;
        this.path = path;
        this.begStep = begStep;
        this.stopped = false;

        Q = (int) (this.speed * 10.0f);
        m = Q / gcd(Q, 10);
    }

    public Unit getUnit() {
        return unit;
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

    void setPath(LinkedList<Point> path) throws NullPointerException {
        if (path == null) {
            throw new NullPointerException("path is null");
        }
        this.path = path;
    }

    public LinkedList<Point> getWaypoints() {
        return waypoints;
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

    void update(int endJ, int endI) throws IndexOutOfBoundsException, NoSuchElementException, NullPointerException {
        Point nextPathPoint;
        String derp = this.waypointsToString();
        int n = (int) Math.floor(((double) ServerMainLoop.getStep() - (double) this.begStep) * (double) this.speed);
        if (n >= this.path.size() - 1) {
            nextPathPoint = this.path.getLast();
        } else {
            nextPathPoint = this.path.get(n + 1);
        }
        
        Point lastPathPoint = waypoints.removeLast();
        
        /*
         * Если следующая точка пути юнита является одновременно и вейпоинтом,
         * то нам не нужно добавлять эту точку к вейпоинтам, она и так там есть.
         */
        if (!waypoints.getLast().equals(nextPathPoint)) {
            waypoints.add(nextPathPoint);
        }
        
        /*
         * Если следующая точка пути юнита является точкой куда собирается
         * сходить игрок, то нам не нужно добавлять точку к вейпоинтам, юнит
         * и так туда сходит.
         */
        if (nextPathPoint.getJ() != endJ || nextPathPoint.getI() != endI) {
            waypoints.add(new Point(endJ, endI));
        }
        
        if (haveSameWaypoints()) {
            logger.log(
                Level.SEVERE,
                "{0}: {1} {2} {3} {4} {5} {6}",
                new Object[] {ServerMainLoop.getStep(), n, nextPathPoint, lastPathPoint, this.waypointsToString(), n >= this.path.size() - 1, derp}
            );
            throw new NullPointerException("Попытка добавить вэйпоинт такой же как предыдущий.");
        }
        // TODO Здесь должно быть пересоздание пути, но мы пока что поручим это вызывающему коду.
    }

    public String waypointsToString() {
        String out = "";
        for (int i = 0; i < waypoints.size(); i++) {
            out += waypoints.get(i);
        }
        return out;
    }
    
    public String pathToString() {
        String out = "";
        for (int i = 0; i < path.size(); i++) {
            out += path.get(i);
        }
        return out;
    }

    private boolean haveSameWaypoints() {
        Point cur = waypoints.getFirst();
        for (int i = 1; i < waypoints.size(); i++) {
            if (cur.equals(waypoints.get(i))) {
                return true;
            } else {
                cur = waypoints.get(i);
            }
        }
        return false;
    }

    void shrink() {
        List<Point> subList;
        Point p;
        int s = (int) ((float) m / speed);
        int wpCount;
        
        while (begStep + s <= ServerMainLoop.getStep()) {
            /*logger.log(
                Level.INFO,
                "{0}: Урезание пути юнита {1} m={2},s={3},path={4},wp={5}",
                new Object[] {ServerMainLoop.getStep(), unit, m, s, pathToString(), waypointsToString()}
            );*/
            subList = path.subList(0, m);
            
            wpCount = 0;
            for (int i = wpCount; i < subList.size(); i++) {
                for (int j = wpCount; j < Math.min(subList.size(), waypoints.size()); j++) {
                    if (subList.get(i).equals(waypoints.get(j))) {
                        wpCount++;
                        break;
                    }
                }
            }
            
            /*logger.log(
                Level.INFO,
                "{0}: Урезание пути юнита {1} Удалим {2} точек пути.",
                new Object[] {ServerMainLoop.getStep(), unit, wpCount}
            );*/
            waypoints.subList(0, wpCount).clear();
            p = new Point(path.get(m).getJ(), path.get(m).getI());
            if (!waypoints.getFirst().equals(p)) {
                waypoints.addFirst(p);
            }
            
            subList.clear();
            
            begStep += s;
            /*logger.log(
                Level.INFO,
                "{0}: Урезание пути юнита {1} завершено path={2},wp={3}",
                new Object[] {ServerMainLoop.getStep(), unit, pathToString(), waypointsToString()}
            );*/
        }
    }

    private int Q;

    private int m;

    private static int gcd(int a, int b) {
        int temp;
        
        while (b > 0) {
            temp = b;
            b = a % b;
            a = temp;
        }
        
        return a;
    }
}
