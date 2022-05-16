package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.util.List;

/**
 * The interface Peer.
 */
public interface Peer {

    /**
     * Update the Peer with a new tile list.
     *
     * @param tileset new tileset
     */
    void update(List<Pair<Integer, Integer>> tileset);

    /**
     * Given a remote name, a lookup is performed to obtain the sharedObject associated with that name.
     *
     * @param remoteName name of the Peer
     */
    void addSharedObject(String remoteName);

    /**
     * Send local information to all other nodes in the cluster.
     *
     * @param internalClock the internal clock
     * @param tileset       the tileset
     */
    void sendClockInfo(Integer internalClock, List<Pair<Integer, Integer>> tileset);

    /**
     * Gets remote number.
     *
     * @return the remote number
     */
    Integer getRemoteNumber();
}
