package minimmorts.movement;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.GlobalLogFile;
import minimmorts.map.Map;
import minimmorts.server.ServerMainLoop;
import minimmorts.units.Unit;

/**
 *
 * @author Sorc
 */
public class MovementManager {
    
    /**
     *
     */
    private static Logger logger = null;

    /**
     * Флаг включения логирования.
     */
    private static boolean loggingEnabled = false;

    /**
     * Список всех движений.
     */
    private final LinkedList<Movement> movements = new LinkedList<>();

    public MovementManager() throws IOException {
        if (MovementManager.logger == null) {
            MovementManager.logger = Logger.getLogger(MovementManager.class.getName());
            FileHandler h = new FileHandler(MovementManager.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            MovementManager.logger.addHandler(h);
            MovementManager.logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            MovementManager.logger.setLevel(Level.ALL);
        }
    }

    public static void enableLogging() {
        MovementManager.loggingEnabled = true;
    }

    public static void disableLogging() {
        MovementManager.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return MovementManager.loggingEnabled;
    }
    
    public boolean isUnitMove(Unit unit) {
        for (Movement m : movements) {
            if (m.getUnit().equals(unit) && !m.isStopped()) {
                return true;
            }
        }
        
        return false;
    }

    public Movement startNewMovement(long step, Map map, Unit unit, int j, int i) throws NullPointerException, IOException {
        Movement movement = createMovement(step, map, unit, j, i, 0.3f);
        if (movement == null) {
            return null;
        }
        
        if (MovementManager.isLoggingEnabled()) {
            String path = "";
            for (Point p : movement.getPath()) {
                path += p;
            }
            logger.log(
                Level.INFO,
                "{0}: Создан новый путь для юнита {1} {2}",
                new Object[] {ServerMainLoop.getStep(), unit, path}
            );
        }
        
        movements.add(movement);
        
        return movement;
    }

    public Movement updateMovement(long step, Map map, Unit unit, int j, int i) {
        Movement existedMovement = null;
        LinkedList<Point> newPath;
        
        for (Movement m : movements) {
            if (m.getUnit().equals(unit)) {
                existedMovement = m;
                break;
            }
        }
        if (existedMovement == null) {
            logger.log(
                Level.SEVERE,
                "{0}: Движение юнита {1} в клетку {2}, которое следует обновить, не найдено.",
                new Object[] {ServerMainLoop.getStep(), unit, new Point(j, i)}
            );
            return null;
        }
        existedMovement.update(j, i);
        if ( (newPath = regeneratePath(map, existedMovement)) == null) {
            return null;
        }
        existedMovement.setPath(newPath);
        
        return existedMovement;
    }

    public void updatePositions(long step) {
        boolean found = true;
        while (found) {
            found = false;
            for (Movement m : movements) {
                if (m.isStopped()) {
                    found = true;
                    movements.remove(m);
                    if (MovementManager.isLoggingEnabled()) {
                        logger.log(
                            Level.INFO,
                            "{0}: Удалено движение юнита {1}",
                            new Object[] {ServerMainLoop.getStep(), m.getUnit()}
                        );
                    }
                    break;
                }
            }
        }
        
        for (Movement m : movements) {
            m.updatePosition(step);
        }
    }

    private Movement createMovement(long step, Map map, Unit unit, int endJ, int endI, float speed) throws NullPointerException, IOException {
        return createMovement(step, map, unit, unit.getJ(), unit.getI(), endJ, endI, speed);
    }

    private Movement createMovement(long step, Map map, Unit unit, int begJ, int begI, int endJ, int endI, float speed) throws NullPointerException, IOException {
        LinkedList<Point> path = getPath(map, new Cell(begI, begJ, 0, 0, null), new Cell(endI, endJ, 0, 0, null));
        if (path == null) {
            logger.log(
                Level.SEVERE,
                "{0}: Путь движения юнита {1} из точки {2} в точку {3} не найден.",
                new Object[] {ServerMainLoop.getStep(), unit, new Point(begJ, begI), new Point(endJ, endI)}
            );
            return null;
        }
        
        Movement m = new Movement(unit, unit.getJ(), unit.getI(), endJ, endI, speed, path, step);
        
        return m;
    }

    public void removeByUnits(LinkedList<Unit> removedUnits) {
        for (Unit u : removedUnits) {
            for (Movement m : movements) {
                if (m.getUnit().equals(u)) {
                    movements.remove(m);
                    break;
                }
            }
        }
    }

    private LinkedList<Point> regeneratePath(Map map, Movement movement) {
        LinkedList<Point> waypoints = movement.getWaypoints();
        LinkedList<Point> newPath = new LinkedList<>();
        LinkedList<Point> path;
        
        for (int i = 1; i < waypoints.size(); i++) {
            path = getPath(
                map,
                new Cell(waypoints.get(i - 1).getI(), waypoints.get(i - 1).getJ(), 0, 0, null),
                new Cell(waypoints.get(i).getI(), waypoints.get(i).getJ(), 0, 0, null)
            );
            if (path == null) {
                logger.log(
                    Level.SEVERE,
                    "{0}: Путь движения юнита {1} из точки {2} в точку {3} не найден. Waypoints {4}",
                    new Object[] {ServerMainLoop.getStep(), movement.getUnit(), waypoints.get(i - 1), waypoints.get(i), movement.waypointsToString()}
                );
                return null;
            }
            if (!newPath.isEmpty()) {
                path.removeFirst(); // Чтобы точки пути не дублировались.
            }
            newPath.addAll(path);
        }
        
        if (MovementManager.isLoggingEnabled()) {
            String pathStr = "";
            for (Point p : movement.getPath()) {
                pathStr += p;
            }
            logger.log(
                Level.INFO,
                "{0}: Пересоздан путь для юнита {1} {2}",
                new Object[] {ServerMainLoop.getStep(), movement.getUnit(), pathStr}
            );
        }
        
        return newPath;
    }

    private class Cell {
        private int i;
        private int j;
        private int f;
        private int g;
        private int h;
        private Cell parent;

        public Cell(int i, int j, int g, int h, Cell parent) {
            this.i = i;
            this.j = j;
            this.f = g + h;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        public int getF() {
            return f;
        }

        public int getG() {
            return g;
        }

        public int getH() {
            return h;
        }

        public Cell getParent() {
            return parent;
        }

        public void setF(int f) {
            this.f = f;
        }

        public void setG(int g) {
            this.g = g;
        }

        public void setParent(Cell parent) {
            this.parent = parent;
        }

        public boolean equal(int i, int j) {
            return this.i == i && this.j == j;
        }

        public boolean equal(Cell cell) {
            return this.i == cell.getI() && this.j == cell.getJ();
        }

        @Override
        public String toString() {
            return "[" + j + ", " + i + "]";
        }
    }

    private int cost(Cell cur, int i, int j) {
        int g = cur.getG();
        if ( (cur.getI() - 1 == i && cur.getJ() == j)
                || (cur.getI() + 1 == i && cur.getJ() == j)
                || (cur.getI() == i && cur.getJ() - 1 == j)
                || (cur.getI() == i && cur.getJ() + 1 == j)) {

            g += 10;
        } else {
            g += 14;
        }
        return g;
    }

    private int heuristic(int i, int j, int k, int l) {
        return 10 * (Math.abs(j - l) + Math.abs(i - k));
    }

    private Cell getByIndex(LinkedList<Cell> list, int i, int j) {
        for (int n = 0; n < list.size(); n++) {
            if (list.get(n).getI() == i && list.get(n).getJ() == j) {
                return list.get(n);
            }
        }
        return null;
    }

    private boolean isInList(LinkedList<Cell> list, int i, int j) {
        for (int n = 0; n < list.size(); n++) {
            if (list.get(n).getI() == i && list.get(n).getJ() == j) {
                return true;
            }
        }
        return false;
    }

    private LinkedList<Point> getPath(Map map, Cell beg, Cell end) {
        LinkedList<Cell> open = new LinkedList<>();
        LinkedList<Cell> closed = new LinkedList<>();
        int i, j, g, h, n;
        int[] rows = new int[3], cols = new int[3];
        Cell cur, nei;
        int limit = 1000;
        int stat_cur = 0;
        int stat_beg = 0;
        int stat_onmap = 0;
        int stat_close = 0;

        if (!map.isOnMap(end.getJ(), end.getI())) {
            return null;
        }
        
        if (beg.equal(end)) {
            return null;
        }

        open.add(beg);

        while(open.size() != 0 && limit-- > 0) {

            // Текущей будет клетка из списка открытых клеток с нимимальным f.
            cur = null;
            for (n = 0; n < open.size(); n++) {
                if (cur == null) {
                    cur = open.get(n);
                } else if (open.get(n).getF() < cur.getF()) {
                    cur = open.get(n);
                }
            }

            // Поместим текущую клтку в список закрытых и удалим из списка
            // открытых клеток.
            closed.add(cur);
            open.remove(cur);

            // Обойдём всех соседей текущей клетки.
            rows[0] = cur.getI() - 1;
            rows[1] = cur.getI();
            rows[2] = cur.getI() + 1;
            cols[0] = cur.getJ() - 1;
            cols[1] = cur.getJ();
            cols[2] = cur.getJ() + 1;
            for (int k = 0; k < rows.length; k++) {
                for (int l = 0; l < cols.length; l++) {
                    i = rows[k];
                    j = cols[l];
                    //derpy += i + " " + j + " ";

                    // Текущую клетку не рассматриваем.
                    if (cur.equal(i, j)) {
                        stat_cur++;
                        continue;
                    }

                    // Начальную клетку не рассматриваем.
                    if (beg.equal(i, j)) {
                        stat_beg++;
                        continue;
                    }

                    // Клетку вне карты не рассматриваем.
                    if (!map.isOnMap(j, i)) {
                        stat_onmap++;
                        continue;
                    }

                    // Клетку из списка закрытых не рассматриваем.
                    if (isInList(closed, i, j)) {
                        stat_close++;
                        continue;
                    }

                    if (!isInList(open, i, j)) {
                        g = cost(cur, i, j);
                        h = heuristic(i, j, end.getI(), end.getJ());
                        nei = new Cell(i, j, g, h, cur);

                        if (nei.equal(end.getI(), end.getJ())) {
                            LinkedList<Point> res_array = new LinkedList<>();
                            Cell tmp = nei;
                            do {
                                res_array.addFirst(new Point(tmp.getJ(), tmp.getI()));
                                tmp = tmp.getParent();
                            } while (tmp != null);
                            return res_array;
                            //console.log(stat)
                            //return nei;  // Путь найден!
                        } else {
                            open.add(nei);
                        }
                    } else {
                        nei = getByIndex(open, i, j);
                        g = cost(cur, nei.getI(), nei.getJ());
                        if (nei.getG() > g) {
                            nei.setParent(cur);
                            nei.setG(g);
                            nei.setF(nei.getG() + nei.getH());
                        }
                    }
                }
            }
        }

        if (limit <= 0) {
            //logger.log(Level.SEVERE, "Path from {0} to {1} not found!", new Object[] {beg.toString(), end.toString()});
            return null;
        }

        //console.log(stat)
        return null;    // Нет пути
    }

    public LinkedList<Movement> getAllMovements() {
        LinkedList<Movement> clone;
        synchronized (movements) {
            clone = (LinkedList<Movement>) movements.clone();
        }
        return clone;
    }

    public LinkedList<Movement> getAllActiveMovements() {
        LinkedList<Movement> activeMovements = new LinkedList<>();;
        synchronized (movements) {
            for (Movement m : movements) {
                if (!m.isStopped()) {
                    activeMovements.add(m);
                }
            }
        }
        return activeMovements;
    }
}
