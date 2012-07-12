/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minimmorts.messages;

/**
 *
 * @author Sorc
 */
public class StepTimeMessage implements Message {
    private long newTime;

    public StepTimeMessage(long newTime) {
        this.newTime = newTime;
    }

    @Override
    public String toJSON() {
        return "[\"STEPTIME\", " + newTime + "]";
    }
}
