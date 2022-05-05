package pcd.assignment03.management;

import java.io.Serializable;

public class TileProperties implements Comparable<TileProperties>, Serializable {

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

    public int getOriginalPosition() {
        return originalPosition;
    }

    public void setCurrentPosition(final int newPosition) {
        currentPosition = newPosition;
    }

    @Override
    public int compareTo(TileProperties other) {
        return Integer.compare(this.currentPosition, other.currentPosition);
    }

    @Override
    public String toString() {
        return "TileProperties{" +
                "originalPosition=" + originalPosition +
                ", currentPosition=" + currentPosition +
                ", isInRightPlace=" + isInRightPlace() +
                '}';
    }
}
