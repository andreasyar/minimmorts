package minimmorts;

/**
 *
 * @author andrey
 */
public class SimpleErrorService {
    private static String errorText;
    public static void setLastError(String errorText) {
        SimpleErrorService.errorText = errorText;
    }
    public static String getLastError() {
        return SimpleErrorService.errorText;
    }
}
