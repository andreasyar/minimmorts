package minimmorts.server;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import minimmorts.messages.Message;
import minimmorts.units.Unit;

/**
 *
 * @author andrey
 */
class State {
    private final long step;
    private final LinkedList<Unit> units = new LinkedList<>();
    private final ConcurrentHashMap<Long, LinkedList<Message>> outboxes = new ConcurrentHashMap();

    public State(long step) {
        this.step = step;
    }
    
    public void addAllUnits(LinkedList<Unit> units) {
        this.units.addAll(units);
    }
    
    public void addClient(long clientId, LinkedList<Message> outbox) {
        outboxes.put(clientId, outbox);
    }

    public long getStep() {
        return step;
    }

    public LinkedList<Unit> getUnits() {
        return units;
    }

    public ConcurrentHashMap<Long, LinkedList<Message>> getOutboxes() {
        return outboxes;
    }
}
