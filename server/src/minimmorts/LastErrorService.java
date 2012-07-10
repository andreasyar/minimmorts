package minimmorts;

import minimmorts.messages.Message;

/**
 * Простенькая реализация системы ошибок в виде сохранения последнего сообщения об ошибке.
 * @author andrey
 */
public class LastErrorService {
    
    private static Message lastError = null;
    
    public static void setLastError(Message error) {
        LastErrorService.lastError = error;
    }
        
    public static Message getLastError() {
        return LastErrorService.lastError;
    }
}
