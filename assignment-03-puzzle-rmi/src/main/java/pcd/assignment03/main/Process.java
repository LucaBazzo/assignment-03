package pcd.assignment03.main;


import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.rmi.RemoteException;
import java.util.List;

public interface Process {

    void initialize(View view, List<TileProperties> tiles) throws RemoteException;

    void tileSelected(TileProperties tile);
}
