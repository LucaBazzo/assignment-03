package pcd.assignment03.view;

import pcd.assignment03.utils.ApplicationConstants;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 *  Interface that shows the puzzle and in which the user can interact
 */
public class PuzzleBoard extends JFrame {

	private final int rows, columns;
    private final ViewEvent viewEvent;

	private List<TileProperties> tiles = new ArrayList<>();

	private final JPanel board;
    private final Random puzzleSeed;

    /**
     * Instantiates a new PuzzleBoard.
     *
     * @param rows number of rows
     * @param columns number of columns
     * @param imagePath path to the puzzle image
     * @param viewEvent intermediary for the management of events from java to scala actors
     */
    public PuzzleBoard(final int rows, final int columns, final String imagePath, final ViewEvent viewEvent) {
    	this.rows = rows;
		this.columns = columns;
		this.viewEvent = viewEvent;

        this.puzzleSeed = new Random(ApplicationConstants.DefaultSeed());

        setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        
        createTiles(imagePath);
        paintPuzzle();
    }

    /**
     * Allows the graphic update of the puzzle
     *
     * @param tiles the updated puzzle
     */
    public void UpdatePuzzle(List<TileProperties> tiles) {
        this.tiles = tiles;
        paintPuzzle();
    }

    /**
     * Called when the puzzle is completed. Shows a notification on the screen
     */
    public void PuzzleCompleted() {
        JOptionPane.showMessageDialog(this,
                "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Returns the list of tiles
     *
     * @return the puzzle properties without any image
     */
    public List<TileProperties> getTileList() {
        return this.tiles;
    }

    private void createTiles(final String imagePath) {
		final BufferedImage image;
        
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;
        
        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows*columns).forEach(randomPositions::add);
        Collections.shuffle(randomPositions, puzzleSeed);
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
            	final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns, 
                        					i * imageHeight / rows, 
                        					(imageWidth / columns), 
                        					imageHeight / rows)));

                tiles.add(new Tile(position, randomPositions.get(position), imagePortion));
                position++;
            }
        }
	}
    
    private void paintPuzzle() {
    	board.removeAll();
    	
    	Collections.sort(tiles);
    	
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton((Tile) tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> this.viewEvent.notifyTileSelected((Tile) tile));
    	});
    	
    	pack();
    }
}

