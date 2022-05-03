package pcd.assignment03.management;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MainObs {

	public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry();
            Observato obj = (Observato) registry.lookup("obseObj");

            ObserverClient client = new ObserverClient();
            client.Start(args);
        } catch (Exception e) {
            try {
                Registry registry = LocateRegistry.getRegistry();
                Observato ObseObj = new ObservatoImpl(List.of(1, 2, 3));
                Observato ObseObjStub = (Observato) UnicastRemoteObject.exportObject(ObseObj, 0);

                // Bind the remote object's stub in the registry
                registry.rebind("obseObj", ObseObjStub);
                System.out.println("Objects registered.");
            } catch (Exception ex) {

            }
        }

    }
}
