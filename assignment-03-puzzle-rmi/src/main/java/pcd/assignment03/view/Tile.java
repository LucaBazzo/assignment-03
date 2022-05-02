package pcd.assignment03.view;

import pcd.assignment03.management.TileProperties;

import java.awt.*;

public class Tile extends TileProperties {

    private final Image image;

    public Tile(final int originalPosition, final int currentPosition, final Image image) {
        super(originalPosition, currentPosition);
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
}
