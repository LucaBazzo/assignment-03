package pcd.assignment03.view;

import pcd.assignment03.management.TileProperties;

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
import java.io.Serial;
import java.util.*;
import java.util.stream.IntStream;


/**
 * Class for the management of the puzzle
 */
public class PuzzleBoard extends JFrame {

	@Serial
    private static final long serialVersionUID = 1836190396492499398L;

	private final static Integer DEFAULT_SEED = 1;

    private final View view;

	private final int rows, columns;

	private List<TileProperties> tiles = new  ArrayList<>();
	private final JPanel board;

    private final Random puzzleSeed;


    /**
     * Instantiates a new Puzzle board.
     *
     * @param view      View reference
     * @param rows      number of rows of the puzzle grid
     * @param columns   number of columns of the puzzle grid
     * @param imagePath image path
     */
    public PuzzleBoard(final View view, final int rows, final int columns, final String imagePath) {
    	this.rows = rows;
		this.columns = columns;

        this.view = view;

        this.puzzleSeed = new Random(DEFAULT_SEED);

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
     * Update puzzle with a new tileset
     *
     * @param tiles new tileset
     */
    public void UpdatePuzzle(List<TileProperties> tiles) {
        this.tiles = tiles;
        paintPuzzle();
    }

    /**
     * Puzzle completed.
     */
    public void PuzzleCompleted() {
        JOptionPane.showMessageDialog(this,
                "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Gets tile list.
     *
     * @return the tile list
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
    	System.out.println(tiles.toString());
    	Collections.sort(tiles);
    	
    	tiles.forEach(tile -> {
    		final TileButton btn = new TileButton((Tile) tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> this.view.notifyTileSelected(tile));
    	});
    	
    	pack();
    }



}

