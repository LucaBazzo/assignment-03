package pcd.assignment03.management;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public class ObservatoImpl implements Observato{

    private List<Integer> tileset;

    public ObservatoImpl(List<Integer> tileset) {
        this.tileset = tileset;
    }

    @Override
    public void update(List<Integer> tileset) throws RemoteException {
        this.tileset = tileset;
    }

    @Override
    public List<Integer> get() throws RemoteException {
        return this.tileset;
    }
}
