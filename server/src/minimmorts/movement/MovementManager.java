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
import minimmorts.units.Unit;

/**
 *
 * @author Sorc
 */
public class MovementManager {
    private static final Logger logger = Logger.getLogger(MovementManager.class.getName());

    private final LinkedList<Movement> movements = new LinkedList<>();
    
    public static void initLogging() {
        try {
            FileHandler fh = new FileHandler(MovementManager.class.getName() + ".txt");
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
    
    public boolean isUnitMove(Unit unit) {
        for (Movement m : movements) {
            if (m.getUnit().equals(unit)) {
                return true;
            }
        }
        
        return false;
    }

    public Movement startNewMovement(long step, Map map, Unit unit, int j, int i) {
        Movement movement = createMovement(step, map, unit, j, i, 0.4f);
        if (movement == null) {
            //logger.log(Level.SEVERE, "Cant create movement!");
            return null;
        }
        /*System.err.print("New path for unit id=" + unit.getId() + " created on step " + step + " ");
        for (Point p : movement.getPath()) {
            System.err.print(p + " ");
        }
        System.err.println();*/
        movements.add(movement);
        return movement;
    }

    public Movement updateMovement(long step, Map map, Unit unit, int j, int i) {
        Movement existedMovement = null;
        Point nextPoint;
        Movement updatedMovement;
        
        for (Movement m : movements) {
            if (m.getUnit().equals(unit)) {
                existedMovement = m;
                break;
            }
        }
        if (existedMovement == null) {
            //logger.log(Level.SEVERE, "Movement not found.");
            return null;
        }
        if ( (nextPoint = existedMovement.getNextPathPoint(unit.getJ(), unit.getI())) == null) {
            //logger.log(Level.SEVERE, "Next path point not found.");
            return null;
        }
        //logger.log(Level.INFO, "Next path point for unit id=" + unit.getId() + " is " + nextPoint);
        updatedMovement = createMovement(
            step,
            map,
            existedMovement.getUnit(),
            nextPoint.getJ(),
            nextPoint.getI(),
            j,
            i,
            0.4f
        );
        if (updatedMovement == null) {
            //logger.log(Level.SEVERE, "Cant create movement!");
            return null;
        }
        
        /*System.err.print("Path for unit id=" + unit.getId() + " updated on step " + step + " ");
        for (Point p : updatedMovement.getPath()) {
            System.err.print(p + " ");
        }
        System.err.println();*/
        
        movements.set(movements.indexOf(existedMovement), updatedMovement);
        
        return updatedMovement;
    }

    public void updatePositions(long step) {
        boolean found = true;
        while (found) {
            found = false;
            for (Movement m : movements) {
                if (m.isStopped()) {
                    found = true;
                    movements.remove(m);
                    //logger.log(Level.INFO, "Movement of unit {0} removed on step {1}", new Object[] {m.getUnit().getId(), step});
                    break;
                }
            }
        }
        for (Movement m : movements) {
            m.updatePosition(step);
        }
    }

    private Movement createMovement(long step, Map map, Unit unit, int endJ, int endI, float speed) {
        return createMovement(step, map, unit, unit.getJ(), unit.getI(), endJ, endI, speed);
    }

    private Movement createMovement(long step, Map map, Unit unit, int begJ, int begI, int endJ, int endI, float speed) {
        LinkedList<Point> path = getPath(map, new Cell(begI, begJ, 0, 0, null), new Cell(endI, endJ, 0, 0, null));
        if (path == null) {
            //logger.log(Level.SEVERE, "Path not found!");
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
}
