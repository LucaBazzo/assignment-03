package pcd.assignment03.main;

import pcd.assignment03.management.SelectionManager;
import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.rmi.RemoteException;
import java.util.List;

/**
 * The Controller of the processes
 */
public class Controller implements Process {

    private View view;
    private SelectionManager selectionManager;

    /**
     * Instantiates a new Controller.
     */
    public Controller() {
    }


    @Override
    public void initialize(View view, List<TileProperties> tiles) throws RemoteException {
        System.out.println(tiles.toString());
        this.view = view;
        this.selectionManager = new SelectionManager(this, tiles);
    }

    @Override
    public void tileSelected(TileProperties tile) {
        this.selectionManager.selectTile(tile);
    }

    /**
     * Update view with a new tileset
     *
     * @param tileset           new tileset
     * @param isPuzzleCompleted true if puzzle is completed
     */
    public void updateView(List<TileProperties> tileset, boolean isPuzzleCompleted) {
        System.out.println("Controller: tileset updated --> " + tileset.toString());
        this.view.updateView(tileset, isPuzzleCompleted);
    }

    /**
     * Display view.
     *
     * @param tileset           initial tileset
     * @param isPuzzleCompleted true if puzzle is completed
     */
    public void displayView(List<TileProperties> tileset, boolean isPuzzleCompleted) {
        this.view.display(tileset, isPuzzleCompleted);
    }
}
