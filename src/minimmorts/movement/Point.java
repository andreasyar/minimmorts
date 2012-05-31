package minimmorts.movement;

/**
 *
 * @author Sorc
 */
public class Point implements Cloneable {
    protected int x;
    protected int y;

    public static boolean onDiagonal(Point p1, Point p2) {
        return (p1.x == p2.x + 1 && p1.y == p2.y + 1)
                || (p1.x == p2.x + 1 && p1.y == p2.y - 1)
                || (p1.x == p2.x - 1 && p1.y == p2.y + 1)
                || (p1.x == p2.x - 1 && p1.y == p2.y - 1);
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getDistance(Point p) {
        return Math.abs(x - p.x) - Math.abs(y - p.y);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setLoation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public Point clone() {
        return new Point(x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point) {
            Point p = (Point) obj;
            return x == p.getX() && y == p.getY();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }
}
