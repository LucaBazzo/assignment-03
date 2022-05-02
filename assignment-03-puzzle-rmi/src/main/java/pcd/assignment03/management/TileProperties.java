package pcd.assignment03.management;

public class TileProperties implements Comparable<TileProperties>{

    private final int originalPosition;
    private int currentPosition;

    public TileProperties(final int originalPosition, final int currentPosition) {
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    public boolean isInRightPlace() {
        return currentPosition == originalPosition;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(final int newPosition) {
        currentPosition = newPosition;
    }

    @Override
    public int compareTo(TileProperties other) {
        return Integer.compare(this.currentPosition, other.currentPosition);
    }
}
