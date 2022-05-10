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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SelectionManager {

    private final List<TileProperties> tiles;
    private final Controller controller;
    private final Peer peer;

    private Optional<TileProperties> selectedTile = Optional.empty();
    
    private Integer internalClock = 0;
    private boolean polling = true;

    public SelectionManager(Controller controller, List<TileProperties> tiles) throws RemoteException {
        this.tiles = tiles;
        this.controller = controller;
        this.peer = new PeerImpl(this);
        
        if (polling) 
        {
            //start polling for checking clocks different between cluster nodes
        	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

            Runnable task = () -> {
            	System.out.println("DEBUG - Polling - internal clock " + internalClock);
            	this.peer.sendClockInfo(internalClock, convertTilesetToPairList(tiles));
            };
            
            executorService.scheduleAtFixedRate(task, 0, 3, TimeUnit.SECONDS);
            polling = false;
        }
    }

    public void selectTile(TileProperties tile) {
        if(selectedTile.isPresent()) {
            this.swap(selectedTile.get(), tile);
            selectedTile = Optional.empty();
            
            this.internalClock++;
            
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

    public void updateTileset(List<Pair<Integer, Integer>> pairList, boolean updateClock) {
        log("Tileset updated --> " + pairList);
        convertPairListToTileset(pairList);
        
        if(updateClock)
        	this.internalClock++;
        
        controller.updateView(this.tiles, isPuzzleCompleted());
    }

    public void displayTileset(Optional<List<Pair<Integer, Integer>>> pairList) {
        log("Tileset: " + pairList.toString());
        if(pairList.isPresent())
        	this.convertPairListToTileset(pairList.get());
        else
        	this.shuffleTileset();
        controller.displayView(this.tiles, isPuzzleCompleted());
    }
    
    public void checkClockIntegrity(Integer clock, List<Pair<Integer, Integer>> pairList, Integer sourceRemoteNumber) {
    	System.out.println("DEBUG - check clock integrity: clock received - " + clock + " source number received - " + sourceRemoteNumber);

		List<Pair<Integer, Integer>> tileList = convertTilesetToPairList(this.tiles);
		log(this.internalClock + " - " + clock + " / " + !pairList.equals(tileList) 
				+ " / " + this.peer.getRemoteNumber() + " - " + sourceRemoteNumber);
		
        if ((this.internalClock < clock) || (this.internalClock == clock && !pairList.equals(tileList) 
        		&& this.peer.getRemoteNumber() > sourceRemoteNumber))
        {
          log("DEBUG - Update performed");
          this.internalClock = clock;
          updateTileset(pairList, false);
        }
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
    
    private List<TileProperties> convertPairListToTileset2(List<Pair<Integer, Integer>> pairList) {
    	List<TileProperties> l = new ArrayList<>();
    	pairList.forEach(pair -> l.add(new TileProperties(pair.getSecond(), pair.getFirst())));
        return l;
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
