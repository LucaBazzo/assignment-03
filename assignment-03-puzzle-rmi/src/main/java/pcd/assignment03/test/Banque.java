package pcd.assignment03.test;
import pcd.assignment03.management.Observato;
import pcd.assignment03.management.ObservatoImpl;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class Banque extends UnicastRemoteObject implements BanqueRMIInterface {
    private static final long serialVersionUID = 1L;
    int totalMoney = 0;
    List<Succursale> remoteObjectList = new ArrayList<Succursale>();

    protected Banque() throws RemoteException {
        super();

    }

    @Override
    public int registerToBank(int moneyAmount, Succursale succursale) throws RemoteException {
        succursale.setId(remoteObjectList.size());
        remoteObjectList.add(succursale);
        totalMoney += moneyAmount;
        System.out.println(" succursale #" + succursale.getId() + " added " + moneyAmount + "$ to the lot");
        System.out.println("New total of money is: " + totalMoney);

        updateClientsRemoteObjectList();

        return remoteObjectList.size() - 1;
    }

    public static void main(String[] args) {
        try {
            //startRmiRegistry();

            Registry registry = LocateRegistry.getRegistry();
            Banque banque = new Banque();
            //Banque banqueObjStub = (Banque) UnicastRemoteObject.exportObject(banque, 0);

            // Bind the remote object's stub in the registry
            registry.rebind("banqueObj", banque);

            System.out.println("Server ready");
            System.out.println("Server really ready");

        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void updateClientsRemoteObjectList() {
        for (int i = 0; i < remoteObjectList.size(); i++) {
            System.out.println("updating client #" + remoteObjectList.get(i).getId());
            remoteObjectList.get(i).updateRemoteObjectList(remoteObjectList);
        }
    }


    public static void startRmiRegistry() {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
            System.out.println("RMI registry ready.");
        } catch (Exception e) {
            System.out.println("Exception starting RMI registry:");
            e.printStackTrace();
        }
    }
}