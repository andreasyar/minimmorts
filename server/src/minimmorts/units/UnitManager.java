package minimmorts.units;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import minimmorts.IdGenerator;
import minimmorts.ErrnoErrorService;
import minimmorts.messages.Message;
import minimmorts.messages.UnitMessage;

/**
 *
 * @author Sorc
 */
public class UnitManager {
    private static final Logger logger = Logger.getLogger(UnitManager.class.getName());

    private final LinkedList<Unit> allUnits = new LinkedList<>();

    public Unit createUnit(long ownerId, int j, int i) {
        Unit unit = new Unit(ownerId, IdGenerator.generateId(), j, i);
        synchronized (allUnits) {
            allUnits.add(unit);
        }
        return unit;
    }

    public boolean isStateValid(LinkedList<Message> unitMessages) {
        UnitMessage m;
        //boolean found;
        int i;
        Unit u;

        /*
         * Пройдём по всем юнитам и проверим, соответвует ли их состояние
         * состояниям в сообщениях (unitMessages). Пока что все клиенты
         * занют обо всех юнитах, поэтому не важно по чему пойдёт внешний
         * цикл по всем юнитам или по сообщениям.
         */
        synchronized (allUnits) {
            for (Message um : unitMessages) {
                m = (UnitMessage) um;
                if ( (i = allUnits.indexOf(m)) != -1) {
                    u = allUnits.get(i);
                    if (u.getOwnerId() != m.getOwnerId() || u.getJ() != m.getJ() || u.getI() != m.getI()) {
                        String errorText = "State invalid. Unit id:" + u.getId() +
                            " Server: ownerId:" + u.getOwnerId() + ",j:" + u.getJ() + ",i:" + u.getI() +
                            " Client: ownerId:" + m.getOwnerId() + ",j:" + m.getJ() + ",i:" + m.getI();
                        ErrnoErrorService.setErrno(errorText);
                        logger.log(Level.INFO, errorText);
                        return false;
                    }
                } else {
                    String errorText = "State invalid. Unit id:" + m.getId() + " not found.";
                    ErrnoErrorService.setErrno(errorText);
                    logger.log(Level.INFO, errorText);
                    return false;
                }
            }
        }

        return true;
    }

    public LinkedList<Unit> removeUnitsByOwnerId(long ownerId) {
        LinkedList<Unit> removedUnits = new LinkedList<>();
        
        synchronized (allUnits) {
            Iterator i = allUnits.iterator();
            Unit u;
            while (i.hasNext()) {
                u = (Unit) i.next();
                if (u.getOwnerId() == ownerId) {
                    removedUnits.add(u);
                    u.markDeleted();
                }
            }
        }
        
        return removedUnits;
    }
    
    public void removeMarkedUnits() {
        synchronized (allUnits) {
            Iterator i = allUnits.iterator();
            Unit u;
            while (i.hasNext()) {
                u = (Unit) i.next();
                if (u.isDeleted()) {
                    i.remove();
                }
            }
        }
    }

    public Unit getById(Long unitId) {
        synchronized (allUnits) {
            for (Unit u : allUnits) {
                if (u.getId() == unitId && !u.isDeleted()) {
                    return u;
                }
            }
        }

        return null;
    }

    public LinkedList<Unit> getAllUnits() {
        LinkedList<Unit> clone = new LinkedList<>();
        synchronized (allUnits) {
            for (Unit u : allUnits) {
                if (!u.isDeleted()) {
                    clone.add(u);
                }
            }
        }
        return clone;
    }
}
