package pcd.assignment03.main;


import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.rmi.RemoteException;
import java.util.List;

/**
 * The interface Process, used by Controller
 */
public interface Process {

    /**
     * Initialize the controller with the reference of View and the initial tileset
     *
     * @param view  View reference
     * @param tiles initial tileset
     * @throws RemoteException if RMI service encounters problems
     */
    void initialize(View view, List<TileProperties> tiles) throws RemoteException;

    /**
     * Select a Tile
     *
     * @param tile Tile clicked
     */
    void tileSelected(TileProperties tile);
}
