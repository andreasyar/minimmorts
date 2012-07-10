package minimmorts.units;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import minimmorts.*;
import minimmorts.client.Client;
import minimmorts.messages.Message;
import minimmorts.messages.MessageManager;
import minimmorts.messages.UnitMessage;
import minimmorts.server.ServerMainLoop;

/**
 *
 * @author Sorc
 */
public class UnitManager {

    /**
     * 
     */
    private static Logger logger = null;

    /**
     * Флаг включения логирования.
     */
    private static boolean loggingEnabled = false;

    private final LinkedList<Unit> allUnits = new LinkedList<>();

    public static void enableLogging() {
        UnitManager.loggingEnabled = true;
    }

    public static void disableLogging() {
        UnitManager.loggingEnabled = false;
    }

    public static boolean isLoggingEnabled() {
        return UnitManager.loggingEnabled;
    }

    public UnitManager() throws IOException {
        if (UnitManager.logger == null) {
            UnitManager.logger = Logger.getLogger(UnitManager.class.getName());
            FileHandler h = new FileHandler(UnitManager.class.getName() + ".txt");
            h.setFormatter(new SimpleFormatter());
            logger.addHandler(h);
            logger.addHandler(GlobalLogFile.getGlobalLogFileHandler());
            logger.setLevel(Level.ALL);
        }
    }

    /**
     * Создаёт юнита.
     * @param owner клиент владелец юнита
     * @param j местоположение юнита на карте по ширине
     * @param i местоположение юнита на карте по высоте
     * @throws NullPointerException если вместо клиента владельца
     * юнита получен null
     * @return новый юнит
     */
    public Unit createUnit(Client owner, int j, int i) throws NullPointerException {
        if (owner == null) {
            throw new NullPointerException("Владелец юнита - null.");
        }
        
        Unit unit = new Unit(owner.getId(), IdGenerator.generateId(), j, i, owner.getColor());
        synchronized (allUnits) {
            allUnits.add(unit);
        }
        return unit;
    }

    public boolean isStateValid(LinkedList<Message> unitMessages) {
        UnitMessage m;
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
                        //logger.log(Level.INFO, errorText);
                        return false;
                    }
                } else {
                    String errorText = "State invalid. Unit id:" + m.getId() + " not found.";
                    ErrnoErrorService.setErrno(errorText);
                    //logger.log(Level.INFO, errorText);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Помечает юнитов на удаление по идентификатору владельца и возвращает
     * помеченных юнитов.
     * @param ownerId идентификатор владельца
     * @return список помеченных юнитов
     */
    public LinkedList<Unit> markUnitsDeletedByOwnerId(long ownerId) {
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
    
    /**
     * Удаляет помеченных на удаление юнитов.
     */
    public void deleteMarkedUnits() {
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

    /**
     * Возвращает юнита по его идентификатору.
     * @param идентификатор юнита
     * @param юнит или <b>null</b> если юнит не найден по его идентификатору или
     * юнит помечен на удаление.
     */
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

    /**
     * Возвращает всех юнитов, не помеченных на удаление.
     * @return копия списока юнитов
     */
    public LinkedList<Unit> getAllActiveUnits() {
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

    /**
     * Возвращает список копий юнитов, не отмеченных на удаление. Если таковых
     * нет, то возвращается пустой список.
     */
    public LinkedList<Unit> cloneUnits() throws CloneNotSupportedException {
        LinkedList<Unit> clone = new LinkedList<>();
        
        synchronized (allUnits) {
            for (Unit u : allUnits) {
                if (!u.isDeleted()) {
                    clone.add(u.clone());
                }
            }
        }
        
        return clone;
    }

    public boolean isStateValid(LinkedList<Unit> serverUnits, LinkedList<Message> clientUnits) throws NullPointerException {
        if (serverUnits == null) {
            throw new NullPointerException("List of server units is null.");
        }
        if (clientUnits == null) {
            throw new NullPointerException("List of client reported units is null.");
        }
        
        UnitMessage m;
        int i;
        Unit u;
        
        for (Message um : clientUnits) {
            m = (UnitMessage) um;
            
            if ( (i = serverUnits.indexOf(m)) != -1) {
                u = serverUnits.get(i);
                
                if (u.getOwnerId() != m.getOwnerId() || u.getJ() != m.getJ() || u.getI() != m.getI()) {
                    if (isLoggingEnabled()) {
                        logger.info(ServerMainLoop.getStep() + ": Неверное состояние юнита id=" + u.getId() + 
                                    " Server: ownerId=" + u.getOwnerId() + ",j=" + u.getJ() + ",i=" + u.getI() +
                                    " Client: ownerId=" + m.getOwnerId() + ",j=" + m.getJ() + ",i=" + m.getI());
                    }
                    LastErrorService.setLastError(MessageManager.createSyncErrorMessage(m.getId()));
                    return false;
                }
            } else {
                String errorText = "Неверное состояние. Юнит id=" + m.getId() +
                                   " существует в отчёте клиента, но не найден на сервере.";
                LastErrorService.setLastError(MessageManager.createErrorMessage(errorText));
                return false;
            }
        }
        
        for (Unit u2 : serverUnits) {
            if ( (i = clientUnits.indexOf(u2)) == -1) {
                String errorText = "Неверное состояние. Юнит id=" + u2.getId() +
                                   " существует на сервере, но не найден в отчёте клиента.";
                LastErrorService.setLastError(MessageManager.createErrorMessage(errorText));
                return false;
            }
        }

        return true;
    }

    public boolean isClientHasUnit(long clientId, long unitId) {
        synchronized (allUnits) {
            for (Unit u : allUnits) {
                if (u.getId() == unitId && u.getOwnerId() == clientId) {
                    return true;
                }
            }
        }

        return false;
    }
}
