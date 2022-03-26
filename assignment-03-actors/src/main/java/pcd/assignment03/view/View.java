package pcd.assignment03.view;

import java.util.List;

import pcd.assignment03.main.Process;

/**
 *
 * Manages the view part
 *
 */
public class View {

    private final ViewGUI gui;

    /**
     * Constructor
     *
     * @param width the view's width
     * @param height the view's height
     * @param pdfPath the path where the pdf are contained
     * @param ignoredFile the path where the ignored.txt is contained
     * @param nWords the number of most frequent words to obtain
     */
    public View(int width, int height, String pdfPath, String ignoredFile, int nWords){
        this.gui = new ViewGUI(width, height, pdfPath, ignoredFile, nWords);
    }

    /**
     * Add a listener
     * @param listener the view's listener
     */
    public void addListener(Process listener){
        gui.addListener(listener);
    }

    /**
     * Set the GUI visible
     */
    public void display() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui.setVisible(true);
        });
    }

    /**
     * Set the new result
     *
     * @param count the total of words processed until now
     * @param result the result
     * @param workEnded to let the view know if the job is finished or not
     */
    public void updateResult(final int count, final List<Pair<String, Integer>> result, final Boolean workEnded){
        gui.updateResult(count, result, workEnded);
    }

    /**
     * Change the view state
     *
     * @param state the new state
     */
    public void changeState(final String state){
        gui.updateState(state);
    }

}

