package pcd.assignment03.management;

import java.io.Serializable;

/**
 * Representation of Tile without the image.
 */
public class TileProperties implements Comparable<TileProperties>, Serializable {

    private final int originalPosition;
    private int currentPosition;

    /**
     * Instantiates a new TileProperties.
     *
     * @param originalPosition original position of the puzzle piece prior the shuffling
     * @param currentPosition  current position of the puzzle piece
     */
    public TileProperties(final int originalPosition, final int currentPosition) {
        this.originalPosition = originalPosition;
        this.currentPosition = currentPosition;
    }

    /**
     *
     *
     * @return true if this tile is in its original position
     */
    public boolean isInRightPlace() {
        return currentPosition == originalPosition;
    }

    /**
     * Gets current position.
     *
     * @return the current position
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Gets original position.
     *
     * @return the original position
     */
    public int getOriginalPosition() {
        return originalPosition;
    }

    /**
     * Sets current position.
     *
     * @param newPosition the new current position
     */
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
