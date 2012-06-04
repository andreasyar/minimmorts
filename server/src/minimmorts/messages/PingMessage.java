package minimmorts.messages;

/**
 *
 * @author andrey
 */
public class PingMessage implements Message {
    private long time;

    PingMessage(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toJSON() {
        return "[\"PING\", {\"time\":" + time + "}]";
    }
}
