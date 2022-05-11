package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of Peer interface.
 */
public class PeerImpl implements Peer{

    private static final Integer START_NUMBER = 0;
    private static final String REMOTE_NAME = "shareObject-";
    private final List<SharedObject> sharedObjects = new ArrayList<>();
    private final SelectionManager selectionManager;
    
    private Integer remoteNumber = START_NUMBER;

    /**
     * Instantiates a new Peer.
     *
     * @param selectionManager reference to the selection manager
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    public PeerImpl(SelectionManager selectionManager) throws RemoteException {
        this.selectionManager = selectionManager;
        this.start();
    }

    private void start() throws RemoteException {

        System.out.println("Started");
        boolean numberFound = false;
        
        while (!numberFound) {
            String remoteName = REMOTE_NAME + remoteNumber;
            try {
                Registry registry = LocateRegistry.getRegistry();
                SharedObject sharedObject = (SharedObject) registry.lookup(remoteName);
                this.addSharedObject(remoteName);
            } catch (Exception e) {
                System.out.println(remoteName + " not found. Creating a new object.");
                try {
                    SharedObject shareObject = new SharedObjectImpl(this, remoteName, selectionManager);
                    shareObject = (SharedObject) UnicastRemoteObject.exportObject(shareObject, 0);

                    Registry registry = LocateRegistry.getRegistry();

                    // Bind the remote object's stub in the registry
                    registry.rebind(remoteName, shareObject);
                    System.out.println(remoteName + " registered.");

                    System.out.println("Starting clients notification");
                    sharedObjects.forEach(c -> {
                        try {
                            c.addRemote(remoteName);
                        }  catch (Exception ex) {
                            System.out.println(ex);
                        }
                    });

                    numberFound = true;
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            remoteNumber++;
        }
        remoteNumber--;

        selectionManager.displayTileset(sharedObjects.isEmpty() ? Optional.empty() : Optional.of(sharedObjects.get(0).getTileset()));

    }

    @Override
    public void update(List<Pair<Integer, Integer>> tileset) {
        this.sharedObjects.forEach(s -> {
            try {
                s.updateTileset(tileset);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void addSharedObject(String remoteName) {
        try {
            Registry registry = LocateRegistry.getRegistry();
            SharedObject sharedObject = (SharedObject) registry.lookup(remoteName);

            System.out.println( remoteName + " obtained ");

            List<Pair<Integer, Integer>> response = sharedObject.getTileset();
            //System.out.println(remoteName + " tileset: " + response.toString());
            sharedObjects.add(sharedObject);
        } catch (Exception e) {
            System.err.println("Client exception: " + e);
            e.printStackTrace();
        }
    }
    
    @Override
	public void sendClockInfo(Integer internalClock, List<Pair<Integer, Integer>> tileset){
		this.sharedObjects.forEach(obj -> {
			try {
				System.out.println("Sending clock to " + obj.getPeerRemoteName());
				obj.checkClockIntegrity(internalClock, tileset, this.remoteNumber);
			} catch (RemoteException e) {
				System.out.println("Object no more present");
			}
		});
	}

	@Override
	public Integer getRemoteNumber() {
		return this.remoteNumber;
	}

}
