/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minimmorts.messages;

import minimmorts.units.Unit;

/**
 *
 * @author andrey
 */
public class DelUnitMessage implements Message {
    private long id;

    public DelUnitMessage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DelUnitMessage) {
            return ((DelUnitMessage) obj).getId() == this.getId();
        } else if (obj instanceof Unit) {
            return ((Unit) obj).getId() == this.getId();
        } else {
            return false;
        }
    }

    @Override
    public String toJSON() {
        return "[\"DELUNIT\"," + id + "]";
    }
}
