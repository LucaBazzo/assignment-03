package pcd.assignment03.management;

import pcd.assignment03.rmi.HelloService;
import pcd.assignment03.rmi.HelloServiceImpl;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ObserverServer {
                
    public static void main(String args[]) {
        
        try {
            Observato ObseObj = new ObservatoImpl(List.of(1,2,3));
            Observato ObseObjStub = (Observato) UnicastRemoteObject.exportObject(ObseObj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            
            registry.rebind("obseObj", ObseObjStub);
            
            System.out.println("Objects registered.");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}