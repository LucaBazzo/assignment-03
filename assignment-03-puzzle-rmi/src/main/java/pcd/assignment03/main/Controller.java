package pcd.assignment03.main;

import pcd.assignment03.management.Peer;
import pcd.assignment03.management.PeerImpl;
import pcd.assignment03.management.SelectionManager;
import pcd.assignment03.utils.Pair;
import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

/**
 *
 * The Controller of the processes
 *
 */
public class Controller implements Process {

    private View view;
    private SelectionManager selectionManager;

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

    public void updateView(List<TileProperties> tileset, boolean isPuzzleCompleted) {
        System.out.println("COntroller: tileset updated --> " + tileset.toString());
        this.view.updateView(tileset, isPuzzleCompleted);
    }

    public void displayView(List<TileProperties> tileset, boolean isPuzzleCompleted) {
        this.view.display(tileset, isPuzzleCompleted);
    }
}
