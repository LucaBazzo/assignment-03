package pcd.assignment03.management;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Observato extends Remote {

    void update(List<Integer> tileset) throws RemoteException;

    List<Integer> get() throws RemoteException;
    
    void addObserver(RemoteObserver o) throws RemoteException;

}
