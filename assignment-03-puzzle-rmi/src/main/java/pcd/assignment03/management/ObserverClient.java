package pcd.assignment03.management;

import pcd.assignment03.rmi.HelloService;
import pcd.assignment03.rmi.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ObserverClient implements RemoteObserver {

    public ObserverClient() {}
    
    public void Start(String[] args) {
    	String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            Observato obj = (Observato) registry.lookup("obseObj");
            obj.addObserver(this);
            
            List<Integer> response = obj.get();
            System.out.println("response: " + response.toString());

            System.out.println("done.");
            
            obj.update(List.of(3,4,5));
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

	@Override
	public void notify(List<Integer> tilset) throws RemoteException {
		System.out.println(tilset);
	}
}