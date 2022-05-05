package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.rmi.RemoteException;
import java.util.List;

public interface Peer {

    void update(List<Pair<Integer, Integer>> tileset);

    void addSharedObject(String remoteName);
}
