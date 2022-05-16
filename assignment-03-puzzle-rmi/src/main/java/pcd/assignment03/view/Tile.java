package pcd.assignment03.view;

import pcd.assignment03.management.TileProperties;

import java.awt.*;

/**
 * Class representing a puzzle piece
 */
public class Tile extends TileProperties {

	private static final long serialVersionUID = -8372007800962773205L;
	
	private final Image image;

    /**
     * Instantiates a new Tile.
     *
     * @param originalPosition original position of the puzzle piece prior the shuffling
     * @param currentPosition  current position of the puzzle piece
     * @param image            the image
     */
    public Tile(final int originalPosition, final int currentPosition, final Image image) {
        super(originalPosition, currentPosition);
        this.image = image;
    }

    /**
     * Gets the image associated to this puzzle piece.
     *
     * @return the image
     */
    public Image getImage() {
        return image;
    }
}
