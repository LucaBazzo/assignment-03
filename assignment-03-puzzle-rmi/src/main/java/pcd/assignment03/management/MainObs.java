package pcd.assignment03.management;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class MainObs {

	public static void main(String[] args) {

        ObserverClient client = new ObserverClient();
        client.Start(args);
    }
}
