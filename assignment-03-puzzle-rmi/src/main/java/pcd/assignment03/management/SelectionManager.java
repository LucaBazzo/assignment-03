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


/** Manages the selection and swap of puzzle pieces, even if received by other nodes
*
*/
public class SelectionManager {
	
	private final static boolean DEBUG = false;

    private final List<TileProperties> tiles;
    private final Controller controller;
    private final Peer peer;

    private Optional<TileProperties> selectedTile = Optional.empty();
    
    private Integer internalClock = 0;

    /** SelectionManager constructor.
    *
    * @param controller reference to the controller
    * @param tiles list of puzzle pieces
    */
    public SelectionManager(Controller controller, List<TileProperties> tiles) throws RemoteException {
        this.tiles = tiles;
        this.controller = controller;
        this.peer = new PeerImpl(this);
        
        this.StartCheckClock();
    }

    /**
     * Stores the selected tile or allows a swap with the previously selected one.
     *
     * @param tile		the selected tile
     */
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

    /**
     * @return the list of tiles in a format that can be sent to other applications
     */
    public List<Pair<Integer, Integer>> getPairList() {
        return convertTilesetToPairList(this.tiles);
    }

    /** 
     * Updates the local tile list with the one received
     * 
     * @param pairList		the list of tiles obtained from another application
     * @param updateClock	performed only when the update is not caused by a checkClockIntegrity
     */
    public void updateTileset(List<Pair<Integer, Integer>> pairList, boolean updateClock) {
        log("Tileset updated --> " + pairList);
        convertPairListToTileset(pairList);
        
        if(updateClock)
        	this.internalClock++;
        
        controller.updateView(this.tiles, isPuzzleCompleted());
    }

    /** 
     * Requires the display of the sent tileset
     * 
     * @param pairList		the list of tiles obtained from another application, can be empty if we are the first application
     */
    public void displayTileset(Optional<List<Pair<Integer, Integer>>> pairList) {
        log("Tileset: " + pairList.toString());
        if(pairList.isPresent())
        	this.convertPairListToTileset(pairList.get());
        else
        	this.shuffleTileset();
        controller.displayView(this.tiles, isPuzzleCompleted());
    }
    
    /** Check if the internal clock is different (but not greater) from the one received from other nodes,
     *  in that case it means that this node's tileset isn't the latest in the cluster and needs to be updated.
     *  If clocks are equals, tileset are checked and, if different, it means that some updates went lost between those nodes
     *  and so it is prioritized the one coming from the node with lower remote number. 
     *  In this way, only the nodes with greater remote number will be updated.
     *  
     *  @param clock				the internal clock number of the other application
     *  @param pairList 			the tileset of the other application
     *  @param sourceRemoteNumber 	the remote number of who requested the clock check
     */
    public void checkClockIntegrity(Integer clock, List<Pair<Integer, Integer>> pairList, Integer sourceRemoteNumber) {
		List<Pair<Integer, Integer>> tileList = convertTilesetToPairList(this.tiles);
		if(DEBUG)
			log(this.internalClock + " - " + clock + " / " + !pairList.equals(tileList) 
					+ " / " + this.peer.getRemoteNumber() + " - " + sourceRemoteNumber);
		
        if ((this.internalClock < clock) || (this.internalClock == clock && !pairList.equals(tileList) 
        		&& this.peer.getRemoteNumber() > sourceRemoteNumber))
        {
        	if(DEBUG)
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
    
    private void StartCheckClock() {
    	//start polling for checking clocks difference between cluster nodes
    	ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
        	if(DEBUG)
        		log("DEBUG - Polling - internal clock " + internalClock);
        	this.peer.sendClockInfo(internalClock, convertTilesetToPairList(tiles));
        };
        
        executorService.scheduleAtFixedRate(task, 0, 3, TimeUnit.SECONDS);
    }

    private void log(String ... messages) {
        Arrays.stream(messages).forEach(msg -> System.out.println("[Selection Manager] " + msg));
    }
}
