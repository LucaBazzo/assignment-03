package pcd.assignment03.management;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObservable extends Remote {

    void addObserver(RemoteObserver o) throws RemoteException;

}