package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The interface Shared object, shared between many Peers on a remote RMI registry.
 */
public interface SharedObject extends Remote {

    /**
     * Update the current tileset.
     *
     * @param tileset the new tileset
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    void updateTileset(List<Pair<Integer, Integer>> tileset) throws RemoteException;

    /**
     * Gets tileset.
     *
     * @return the tileset
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    List<Pair<Integer, Integer>> getTileset() throws RemoteException;

    /**
     * Gets peer remote name.
     *
     * @return the peer remote name
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    String getPeerRemoteName() throws RemoteException;

    /**
     * Check clock integrity.
     *
     * @param clock              the clock
     * @param tileset            the tileset
     * @param sourceRemoteNumber source remote number
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    void checkClockIntegrity(Integer clock, List<Pair<Integer, Integer>> tileset, Integer sourceRemoteNumber) throws RemoteException ;

    /**
     * Add remote.
     *
     * @param remoteName the remote name
     * @throws RemoteException   remote exception if RMI service encounters a problem
     * @throws NotBoundException the not bound exception
     */
    void addRemote(String remoteName) throws RemoteException, NotBoundException;

}

