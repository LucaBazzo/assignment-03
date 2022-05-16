package pcd.assignment03.view;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 *  Represents the button to select a specific tile
 */
public class TileButton extends JButton{

	/**
	 * Instantiates a new TileButton.
	 *
	 * @param tile the tile to associate the button with
	 */
	public TileButton(final Tile tile) {
		super(new ImageIcon(tile.getImage()));
		
		addMouseListener(new MouseAdapter() {            
            @Override
            public void mouseClicked(MouseEvent e) {
            	setBorder(BorderFactory.createLineBorder(Color.red));
            }
        });
	}
}
