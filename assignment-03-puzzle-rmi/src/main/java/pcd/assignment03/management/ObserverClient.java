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
            
            obj.update(List.of(response.get(0) + 1, response.get(1) + 1, response.get(2) + 1));

            Thread.sleep(10000);

            Thread.sleep(10000);
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