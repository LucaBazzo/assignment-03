package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface SharedObject extends Remote {

    void updateTileset(List<Pair<Integer, Integer>> tileset) throws RemoteException;

    List<Pair<Integer, Integer>> getTileset() throws RemoteException;

    void addRemote(String remoteName) throws RemoteException, NotBoundException;

}

