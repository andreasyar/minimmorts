package minimmorts.movement;

/**
 *
 * @author Sorc
 */
public class Point {
    protected int j;
    protected int i;

    public Point(int j, int i) {
        this.j = j;
        this.i = i;
    }

    public int getJ() {
        return j;
    }

    public int getI() {
        return i;
    }

    @Override
    public String toString() {
        return "[" + j + ", " + i + "]";
    }
}
