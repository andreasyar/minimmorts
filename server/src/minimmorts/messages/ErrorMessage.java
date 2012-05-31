package minimmorts.messages;

/**
 *
 * @author andrey
 */
public class ErrorMessage implements Message {
    private String text;

    ErrorMessage(String text) {
        this.text = text;
    }

    @Override
    public String toJSON() {
        return "[\"ERROR\", \"" + text + "\"]";
    }
}
