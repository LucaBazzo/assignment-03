package pcd.assignment03.management;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteObserver extends Remote {

	void notify(List<Integer> tilset) throws RemoteException;

}