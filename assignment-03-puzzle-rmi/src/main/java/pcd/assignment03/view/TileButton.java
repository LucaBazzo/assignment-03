package pcd.assignment03.view;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Class representing a selectable puzzle piece
 */
public class TileButton extends JButton{

	@Serial
	private static final long serialVersionUID = -3522866387161416711L;

	/**
	 * Instantiates a new Tile button.
	 *
	 * @param tile the tile
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
