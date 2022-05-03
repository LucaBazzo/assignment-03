package pcd.assignment03.management;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ObservatoImpl implements Observato {

    private List<Integer> tileset;
    private List<RemoteObserver> obsevers;

    public ObservatoImpl(List<Integer> tileset) {
        this.tileset = tileset;
        this.obsevers = new ArrayList<>();
    }

    @Override
    public void update(List<Integer> tileset) throws RemoteException {
        this.tileset = tileset;
        this.obsevers.forEach(o -> {
			try {
			    System.out.println(o);
				o.notify(this.tileset);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		});
    }

    @Override
    public List<Integer> get() throws RemoteException {
        return this.tileset;
    }

	@Override
	public void addObserver(RemoteObserver o) throws RemoteException {
		this.obsevers.add(o);
		System.out.println("Added observer:" + o);
	}
}
