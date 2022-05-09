package pcd.assignment03.management;

import pcd.assignment03.main.Controller;
import pcd.assignment03.utils.Pair;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

public class SelectionManager {

    private final List<TileProperties> tiles;
    private final Controller controller;
    private final Peer peer;

    private Optional<TileProperties> selectedTile = Optional.empty();

    public SelectionManager(Controller controller, List<TileProperties> tiles) throws RemoteException {
        this.tiles = tiles;
        this.controller = controller;
        this.peer = new PeerImpl(this);
    }

    public void selectTile(TileProperties tile) {
        if(selectedTile.isPresent()) {
            this.swap(selectedTile.get(), tile);
            selectedTile = Optional.empty();
            
            //asynchronously call to update the view without waiting the peer
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                public void run() {
                	peer.update(convertTilesetToPairList(tiles));
                }
            });
            
            this.controller.updateView(this.tiles, isPuzzleCompleted());
            this.selectedTile = Optional.empty();
        }
        else
        	selectedTile = Optional.of(tile);
    }

    public List<Pair<Integer, Integer>> getPairList() {
        return convertTilesetToPairList(this.tiles);
    }

    public void updateTileset(List<Pair<Integer, Integer>> pairList) {
        System.out.println("Peer: tileset updated --> " + pairList);
        convertPairListToTileset(pairList);
        controller.updateView(this.tiles, isPuzzleCompleted());
    }

    public void displayTileset(Optional<List<Pair<Integer, Integer>>> pairList) {
        System.out.println(" tileset: " + pairList.toString());
        if(pairList.isPresent())
        	this.convertPairListToTileset(pairList.get());
        else
        	this.shuffleTileset();
        controller.displayView(this.tiles, isPuzzleCompleted());
    }

    private void swap(final TileProperties firstTile, final TileProperties secondTile) {
        int pos = firstTile.getCurrentPosition();
        firstTile.setCurrentPosition(secondTile.getCurrentPosition());
        secondTile.setCurrentPosition(pos);

        log(secondTile.getCurrentPosition() + " --> " + firstTile.getCurrentPosition());
    }

    private boolean isPuzzleCompleted() {
        return tiles.stream().allMatch(TileProperties::isInRightPlace);
    }

    private void convertPairListToTileset(List<Pair<Integer, Integer>> pairList) {
        this.tiles.forEach(tile -> tile.setCurrentPosition(pairList.stream().filter(d -> d.getSecond() == tile.getOriginalPosition()).findFirst().get().getFirst()));
    }

    private List<Pair<Integer, Integer>> convertTilesetToPairList(List<TileProperties> tiles) {
        List<Pair<Integer, Integer>> pairList = new ArrayList<>();

        tiles.forEach(tile -> pairList.add(new Pair<Integer, Integer>(tile.getCurrentPosition(), tile.getOriginalPosition())));

        return pairList;
    }
    
    private void shuffleTileset()
    {
    	List<Integer> currentPositions = new ArrayList<>();
        this.tiles.forEach(tile -> currentPositions.add(tile.getCurrentPosition()));
        Collections.shuffle(currentPositions);
        for(int i=0; i< this.tiles.size(); i++) {
        	this.tiles.get(i).setCurrentPosition(currentPositions.get(i));
        }
    }

    private void log(String ... messages) {
        Arrays.stream(messages).forEach(msg -> System.out.println("[Selection Manager] " + msg));
    }
}
