package pcd.assignment03.management;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class SharedObjectImpl implements SharedObject {

    private List<Integer> tileset;
    private List<RemoteObserver> obsevers;

    public SharedObjectImpl(List<Integer> tileset) {
        this.tileset = tileset;
        this.obsevers = new ArrayList<>();
    }

    @Override
    public void updateTileset(List<Integer> tileset) throws RemoteException {
        this.tileset = tileset;
        System.out.println("Tileset updated --> " + tileset);
        /*this.obsevers.forEach(o -> {
            try {
                System.out.println(o);
                o.notify(this.tileset);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });*/
    }

    @Override
    public List<Integer> getTileset() throws RemoteException {
        return this.tileset;
    }

    @Override
    public void addRemote(String remoteName) throws RemoteException {
        new ObserverClient(remoteName);
    }


}
