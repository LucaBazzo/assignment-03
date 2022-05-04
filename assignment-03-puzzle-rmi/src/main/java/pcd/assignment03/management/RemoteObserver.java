package pcd.assignment03.management;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteObserver extends Remote, Serializable {

	void notify(List<Integer> tileset) throws RemoteException;

}