package minimmorts;

/**
 * Простенькая реализация системы ошибок в виде errno.
 * @author andrey
 */
public class ErrnoErrorService {
    
    private static String errno;
    
    public static void setErrno(String errorText) {
        ErrnoErrorService.errno = errorText;
    }
        
    public static String getErrno() {
        return ErrnoErrorService.errno;
    }
}
