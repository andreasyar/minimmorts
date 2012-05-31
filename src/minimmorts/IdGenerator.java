package minimmorts;

/**
 *
 * @author Sorc
 */
public class IdGenerator {
    private static long Id = 0;
    
    public synchronized static long generateId() {
        IdGenerator.Id++;
        return IdGenerator.Id;
    }
}
