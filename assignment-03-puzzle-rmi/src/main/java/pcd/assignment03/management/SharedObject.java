package pcd.assignment03.management;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SharedObject extends Remote {

    void updateTileset(List<Integer> tileset) throws RemoteException;

    List<Integer> getTileset() throws RemoteException;

    void addRemote(String remoteName) throws RemoteException, NotBoundException;

}

