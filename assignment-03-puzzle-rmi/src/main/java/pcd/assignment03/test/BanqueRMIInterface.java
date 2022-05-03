package pcd.assignment03.test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface BanqueRMIInterface extends Remote {
    public int registerToBank(int moneyAmount, Succursale succursale) throws RemoteException;
}
