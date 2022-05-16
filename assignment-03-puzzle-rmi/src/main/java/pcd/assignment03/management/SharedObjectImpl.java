package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.rmi.RemoteException;
import java.util.List;


public class SharedObjectImpl implements SharedObject {

    private final Peer peer;
    private final String peerRemoteName;
    private final SelectionManager selectionManager;

    public SharedObjectImpl(Peer peer, String peerRemoteName, SelectionManager selectionManager) {
        this.peer = peer;
        this.peerRemoteName = peerRemoteName;
        this.selectionManager = selectionManager;
    }

    @Override
    public void updateTileset(List<Pair<Integer, Integer>> tileset) throws RemoteException {
        this.selectionManager.updateTileset(tileset, true);
    }

    @Override
    public List<Pair<Integer, Integer>> getTileset() throws RemoteException {
        return this.selectionManager.getPairList();
    }
    
    @Override
	public String getPeerRemoteName() throws RemoteException {
		return this.peerRemoteName;
	}

    @Override
    public void addRemote(String remoteName) throws RemoteException {
        this.peer.addSharedObject(remoteName);
    }

	@Override
	public void checkClockIntegrity(Integer clock, List<Pair<Integer, Integer>> tileset, Integer sourceRemoteNumber)  throws RemoteException {
		this.selectionManager.checkClockIntegrity(clock, tileset, sourceRemoteNumber);
	}
}
