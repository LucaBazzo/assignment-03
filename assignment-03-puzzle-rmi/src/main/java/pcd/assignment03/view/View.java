package pcd.assignment03.view;

import pcd.assignment03.main.Process;
import pcd.assignment03.management.TileProperties;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the view part
 */
public class View {

    private final PuzzleBoard puzzleBoard;
    private final ArrayList<Process> listeners;

    /**
     * Instantiates a new View.
     *
     * @param nRows     number of rows of the puzzle grid
     * @param nColumns  number of columns of the puzzle grid
     * @param imagePath the image path
     */
    public View(Integer nRows, Integer nColumns, String imagePath){
        this.puzzleBoard = new PuzzleBoard(this, nRows, nColumns, imagePath);
        this.listeners = new ArrayList<>();
    }

    /**
     * Add listener to this view.
     *
     * @param listener the listener to be added
     * @throws RemoteException remote exception if RMI service encounters a problem
     */
    public void addListener(Process listener) throws RemoteException {
        this.listeners.add(listener);
        listener.initialize(this, puzzleBoard.getTileList());
    }

    /**
     * Set the GUI visible
     *
     * @param tileList          the tile list
     * @param isPuzzleCompleted the is puzzle completed
     */
    public void display(List<TileProperties> tileList, Boolean isPuzzleCompleted) {
        update(tileList, isPuzzleCompleted);
        javax.swing.SwingUtilities.invokeLater(() -> this.puzzleBoard.setVisible(true));
    }

    /**
     * Notify tile selected to other Peers.
     *
     * @param tile the tile
     */
    public void notifyTileSelected(TileProperties tile) {
        this.listeners.forEach(l -> l.tileSelected(tile));
    }

    /**
     * Update view with a new tile list.
     *
     * @param tileList          new tile list
     * @param isPuzzleCompleted true if puzzle is completed
     */
    public void updateView(List<TileProperties> tileList, Boolean isPuzzleCompleted) {
        this.update(tileList, isPuzzleCompleted);
    }

    private void update(List<TileProperties> tileList, Boolean isPuzzleCompleted) {
        this.puzzleBoard.UpdatePuzzle(tileList);
        if(isPuzzleCompleted)
            this.puzzleBoard.PuzzleCompleted();
    }

}
