package pcd.assignment03.view;

import pcd.assignment03.main.Process;
import pcd.assignment03.management.TileProperties;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Manages the view part
 *
 */
public class View {

    private final PuzzleBoard puzzleBoard;
    private final ArrayList<Process> listeners;

    public View(Integer nRows, Integer nColumns, String imagePath){
        this.puzzleBoard = new PuzzleBoard(this, nRows, nColumns, imagePath);
        this.listeners = new ArrayList<>();
    }

    public void addListener(Process listener){
        this.listeners.add(listener);
        listener.initialize(this, puzzleBoard.getTileList());
    }

    /**
     * Set the GUI visible
     */
    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> this.puzzleBoard.setVisible(true));
    }

    public void notifyTileSelected(TileProperties tile) {
        this.listeners.forEach(l -> l.tileSelected(tile));
    }

    public void updateView(List<TileProperties> tileList, Boolean isPuzzleCompleted) {
        this.update(tileList, isPuzzleCompleted);
    }

    private void update(List<TileProperties> tileList, Boolean isPuzzleCompleted) {
        this.puzzleBoard.UpdatePuzzle(tileList);
        if(isPuzzleCompleted)
            this.puzzleBoard.PuzzleCompleted();
    }

}
