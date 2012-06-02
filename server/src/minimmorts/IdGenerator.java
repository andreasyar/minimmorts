package minimmorts;

/**
 * Генератор уникальных идентификаторов.
 * @author Sorc
 */
public class IdGenerator {
    private static long Id = 0;
    
    /**
     * Создаёт уникальный идентификатор.
     * @return уникальный идентификатор
     */
    public synchronized static long generateId() {
        IdGenerator.Id++;
        return IdGenerator.Id;
    }
}
