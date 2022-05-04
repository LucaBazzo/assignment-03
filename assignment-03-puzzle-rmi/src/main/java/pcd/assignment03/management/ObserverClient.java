package pcd.assignment03.management;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ObserverClient implements RemoteObserver {

    private final String remoteName;
    private SharedObject sharedObject;

    public ObserverClient(String remoteName) {
        this.remoteName = remoteName;
        this.start();
    }
    
    private void start() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            sharedObject = (SharedObject) registry.lookup(remoteName);

            List<Integer> response = sharedObject.getTileset();
            System.out.println("response: " + response.toString());

            sharedObject.updateTileset(List.of(response.get(0) + 1, response.get(1) + 1, response.get(2) + 1));

            System.out.println("done.");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void addRemote() throws RemoteException, NotBoundException {
        this.sharedObject.addRemote(this.remoteName);
    }

	@Override
	public void notify(List<Integer> tileset) throws RemoteException {
		System.out.println(tileset);
	}
}