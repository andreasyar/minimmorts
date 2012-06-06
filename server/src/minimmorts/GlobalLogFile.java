package minimmorts;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Sorc
 */
public class GlobalLogFile {
    private static FileHandler h = null;
    
    public static FileHandler getGlobalLogFileHandler() throws IOException {
        if (h == null) {
            h = new FileHandler(GlobalLogFile.class.getName() + "-" + System.currentTimeMillis() + ".txt");
            h.setFormatter(new SimpleFormatter());
        }
        
        return h;
    }
}
