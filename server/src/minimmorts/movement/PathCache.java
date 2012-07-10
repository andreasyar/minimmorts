package minimmorts.movement;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
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
class PathCache {
    
    public static final int MAX_CACHE_SIZE = 100;
    
    private static Logger logger = null;

    private static boolean loggingEnabled = false;
    
    private final LinkedList<Pair<LinkedList<Point>, Integer>> pathCache = new LinkedList<>();
    
    private final HashMap<Integer, HashMap<Integer, LinkedList<Integer>>> pathMap = new HashMap<>();

    public PathCache() throws IOException {
        if (PathCache.logger == null) {
            PathCache.logger = Logger.getLogger(PathCache.class.getName());
            FileHandler h = new FileHandler(PathCache.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            PathCache.logger.addHandler(h);
            PathCache.logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            PathCache.logger.setLevel(Level.ALL);
        }
    }
    
    public boolean cachePath(LinkedList<Point> path) {
        if (!isPathCached(path.getFirst().getJ(), path.getFirst().getI(), path.getLast().getJ(), path.getLast().getI())) {
            Pair newPair = new Pair((LinkedList<Point>) path.clone(), 0);
            int i;  // Индекс сохранённого пути в кеше.
            int curJ, curI;
            
            /*
             * Если нужно, освободим место в кеше под путь.
             */
            if (pathCache.size() == PathCache.MAX_CACHE_SIZE) {
                if (MovementManager.isLoggingEnabled()) {
                    logger.log(Level.INFO, "{0}: Кеш путей достиг максимума.", ServerMainLoop.getStep());
                }
                Pair oldPair = getRarestUsedPath();
                if ( (i = pathCache.indexOf(oldPair)) == -1) {
                    throw new NoSuchElementException("Just found pair not found in list :O");
                }
                cleanPathMap((LinkedList<Point>) oldPair.getKey(), i);
                pathCache.set(i, newPair);
            } else {
                pathCache.add(newPair);
                if ( (i = pathCache.indexOf(newPair)) == -1) {
                    throw new NoSuchElementException("Just add pair not found in list :O");
                }
            }
            
            /*
             * Отметим на карте путей клетки, через которых проходит добавленный путь.
             */
            for (int j = 0; j < path.size(); j++) {
                curJ = path.get(j).getJ();
                curI = path.get(j).getI();
                if (!pathMap.containsKey(curI)) {
                    pathMap.put(curI, new HashMap<Integer, LinkedList<Integer>>());
                }
                if (!pathMap.get(curI).containsKey(curJ)) {
                    pathMap.get(curI).put(curJ, new LinkedList<Integer>());
                }
                pathMap.get(curI).get(curJ).add(i);
            }
            
            return true;
        }
        
        return false;
    }
    
    public LinkedList<Point> getCachedPath(int begJ, int begI, int endJ, int endI, Unit unit) {
        if (pathMap.containsKey(begI) && pathMap.get(begI).containsKey(begJ) && pathMap.containsKey(endI) && pathMap.get(endI).containsKey(endJ)) {
            for (int i = 0; i < pathMap.get(begI).get(begJ).size(); i++) {
                for (int j = 0; j < pathMap.get(endI).get(endJ).size(); j++) {
                    if (pathMap.get(begI).get(begJ).get(i) == pathMap.get(endI).get(endJ).get(j)) {
                        Pair<LinkedList<Point>, Integer> p = pathCache.get(pathMap.get(begI).get(begJ).get(i));
                        LinkedList<Point> path = p.getKey();
                        LinkedList<Point> savedPath = new LinkedList<>();
                        boolean isPathReverted = false;
                        boolean doSave = false;
                        int k;
                        for (k = 0; k < path.size(); k++) {
                            if (path.get(k).getJ() == begJ && path.get(k).getI() == begI) {
                                isPathReverted = false;
                                break;
                            }
                            if (path.get(k).getJ() == endJ && path.get(k).getI() == endI) {
                                isPathReverted = true;
                                break;
                            }
                        }
                        if (isPathReverted) {
                            /*for (k = path.size() - 1; k >= 0; k--) {
                                if (path.get(k).getJ() == begJ && path.get(k).getI() == begI) {
                                    doSave = true;
                                }
                                if (path.get(k).getJ() == endJ && path.get(k).getI() == endI) {
                                    savedPath.add(path.get(k));
                                    break;
                                }
                                if (doSave) {
                                    savedPath.add(path.get(k));
                                }
                            }*/
                            return null;
                        } else {
                            if (MovementManager.isLoggingEnabled()) {
                                logger.log(
                                    Level.INFO,
                                    "{0}: В кеше найден нужный путь для юнита {1} {2} проходящий через точки {3}->{4}",
                                    new Object[] {ServerMainLoop.getStep(), unit, MovementManager.pathToString(path), new Point(begJ, begI), new Point(endJ, endI)}
                                );
                            }
                            doSave = false;
                            for (k = 0; k < path.size(); k++) {
                                if (path.get(k).getJ() == begJ && path.get(k).getI() == begI) {
                                    doSave = true;
                                }
                                if (path.get(k).getJ() == endJ && path.get(k).getI() == endI) {
                                    savedPath.add(path.get(k));
                                    break;
                                }
                                if (doSave) {
                                    savedPath.add(path.get(k));
                                }
                            }
                            
                            /*
                             * Увеличим счетчик использований этого пути.
                             */
                            p.setValue(p.getValue() + 1);
                        }
                        
                        return savedPath;
                    }
                }
            }
        }
        
        return null;
    }
    
    private Pair getRarestUsedPath() {
        int used = 0;
        int i;
        
        while (pathCache.size() > 0 && used <= Integer.MAX_VALUE) {
            for (i = 0; i < pathCache.size(); i++) {
                if (pathCache.get(i).getValue() == used) {
                    return pathCache.get(i);
                }
            }
            used++;
        }
        
        return null;
    }
    
    private boolean isPathCached(int begJ, int begI, int endJ, int endI) {
        if (pathMap.containsKey(begI) && pathMap.get(begI).containsKey(begJ) && pathMap.containsKey(endI) && pathMap.get(endI).containsKey(endJ)) {
            for (int i = 0; i < pathMap.get(begI).get(begJ).size(); i++) {
                for (int j = 0; j < pathMap.get(endI).get(endJ).size(); j++) {
                    if (pathMap.get(begI).get(begJ).get(i) == pathMap.get(endI).get(endJ).get(j)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    private void cleanPathMap(LinkedList<Point> path, int i) {
        /*logger.log(
            Level.INFO,
            "{0}: Очистка карты путей от пути {1}",
            new Object[] {ServerMainLoop.getStep(), MovementManager.pathToString(path)}
        );*/
        for (int j = 0; j < path.size(); j++) {
            /*logger.log(
                Level.INFO,
                "{0}: Очистка клетки карты путей {1} от значения {2}",
                new Object[] {ServerMainLoop.getStep(), path.get(j), i}
            );*/
            if (!pathMap.get(path.get(j).getI()).get(path.get(j).getJ()).removeFirstOccurrence(i)) {
                throw new NoSuchElementException("Элемент " + i + " не существует в клетке карты путей " + path.get(j));
            }
        }
    }
}
