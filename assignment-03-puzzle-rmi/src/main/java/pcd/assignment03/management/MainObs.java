package pcd.assignment03.management;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class MainObs {

    private static final Integer START_NUMBER = 0;
    private static final String REMOTE_NAME = "shareObject-";

	public static void main(String[] args) {

	    List<ObserverClient> clients = new ArrayList<>();

        System.out.println("Started");
	    Boolean numberFound = false;
	    Integer count = START_NUMBER;

	    while (!numberFound) {

            try {
            	Registry registry = LocateRegistry.getRegistry();
            	SharedObject sharedObject = (SharedObject) registry.lookup(REMOTE_NAME + count);
            	
                ObserverClient client = new ObserverClient(REMOTE_NAME + count);
                clients.add(client);
            } catch (Exception e) {
                System.out.println(REMOTE_NAME + count + " not found. Creating a new object.");
                try {
                    SharedObject shareObject = new SharedObjectImpl(List.of(1, 2, 3));
                    shareObject = (SharedObject) UnicastRemoteObject.exportObject(shareObject, 0);

                    Registry registry = LocateRegistry.getRegistry();

                    // Bind the remote object's stub in the registry
                    registry.rebind(REMOTE_NAME + count, shareObject);
                    System.out.println(REMOTE_NAME + count + " registered.");

                    clients.forEach(c -> {
                        try {
                            c.addRemote();
                        }  catch (Exception ex) {
                            System.out.println(ex.toString());
                        }
                    });

                    numberFound = true;
                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }

            count ++;
        }
    }
}
