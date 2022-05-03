package pcd.assignment03.test;

import pcd.assignment03.management.Observato;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Succursale implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -905645444505287895L;
    private BanqueRMIInterface look_up;
    private int id = 0;
    private List<Succursale> remoteObjectList;

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException, Exception {
        Succursale test = new Succursale();
        test.doThings();

    }

    public void doThings() throws RemoteException, MalformedURLException, NotBoundException, Exception {
        Registry registry = LocateRegistry.getRegistry();
        look_up= (BanqueRMIInterface) registry.lookup("banqueObj");
        System.out.println("Baguette");
        int moneyAmount = 450;

        id = look_up.registerToBank(moneyAmount, this);

        while (true) {
            //do things
        }
    }

    public void updateRemoteObjectList(List<Succursale> remoteObjectList) {
        this.remoteObjectList = remoteObjectList;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }
}