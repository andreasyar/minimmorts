package minimmorts.map;

/**
 *
 * @author Sorc
 */
public class Map {
    private int width;
    private int height;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public boolean isOnMap(int j, int i) {
        return j >= 0 && j < width && i >= 0 && i < height;
    }

    public int getPassability(int i, int y) {
        return 1;
    }
}
