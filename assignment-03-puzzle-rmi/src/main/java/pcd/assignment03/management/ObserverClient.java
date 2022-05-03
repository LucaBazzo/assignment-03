package pcd.assignment03.management;

import pcd.assignment03.rmi.HelloService;
import pcd.assignment03.rmi.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ObserverClient {

    private ObserverClient() {}

    public static void main(String[] args) {

        String host = (args.length < 1) ? null : args[0];
        try {
            Registry registry = LocateRegistry.getRegistry(host);
            Observato obj = (Observato) registry.lookup("obseObj");
            
            List<Integer> response = obj.get();
            System.out.println("response: " + response.toString());

            System.out.println("done.");
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}