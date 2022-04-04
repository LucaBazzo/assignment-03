package pcd.assignment03.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Serial;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * Container of the graphic part
 *
 */
public class ViewGUI extends JFrame implements ActionListener {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ViewEvent viewEvent;

    private final JButton startButton;
    private final JButton stopButton;

    private final JFileChooser pdfPathFieldChooser;
    private final JFileChooser ignoredPathChooser;

    private final JTextField pdfPathName;
    private final JTextField ignoredPathField;
    private final JTextField nWordsField;

    private final JTextArea textArea;
    private final JTextField state;

    /**
     * Constructor
     *
     * @param width the view's width
     * @param height the view's height
     * @param pdfPath the path where the pdf are contained
     * @param ignoredPath the path where the ignored.txt is contained
     * @param nWords the number of most frequent words to obtain
     */
    public ViewGUI(int width, int height, String pdfPath, String ignoredPath,
                   int nWords, ViewEvent viewEvent){
        super("Most Frequent Words Viewer");

        setSize(width, height);

        this.viewEvent = viewEvent;

        pdfPathFieldChooser = new JFileChooser(new File(pdfPath));
        pdfPathFieldChooser.setCurrentDirectory(new File(pdfPath));

        ignoredPathChooser = new JFileChooser(new File(ignoredPath));
        ignoredPathChooser.setCurrentDirectory(new File(ignoredPath));

        pdfPathName = new JTextField(15);
        pdfPathName.setText(pdfPath);
        pdfPathName.setEnabled(false);

        ignoredPathField = new JTextField(15);
        ignoredPathField.setText(ignoredPath);
        ignoredPathField.setEnabled(false);

        nWordsField = new JTextField(5);
        nWordsField.setText(String.valueOf(nWords));
        nWordsField.addActionListener(this);

        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        stopButton.setEnabled(false);
        JButton pdfPathButton = new JButton("Choose directory");
        JButton ignoredPathButton = new JButton("Choose file");

        //Field part
        JPanel fieldPanel = new JPanel();
        fieldPanel.add(new JLabel("PDF path "));
        fieldPanel.add(pdfPathName);
        //fieldPanel.add(pdfPathFieldChooser);
        fieldPanel.add(new JLabel("Ignored file "));
        fieldPanel.add(ignoredPathField);
        fieldPanel.add(new JLabel("N° Words "));
        fieldPanel.add(nWordsField);

        //Result part
        JPanel centerPanel = new JPanel();
        GridBagLayout glayout = new GridBagLayout();
        centerPanel.setLayout(glayout);
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel resultPanel = new JPanel();
        textArea = new JTextArea(14, 55);
        textArea.setEditable(false);

        JScrollPane scroll = new JScrollPane (textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        resultPanel.add(scroll);

        JPanel infoPanel = new JPanel();
        state = new JTextField(30);
        state.setText("Idle");
        state.setEditable(false);
        infoPanel.add(new JLabel("State"));
        infoPanel.add(state);

        // Put constraints on different panels
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(resultPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        centerPanel.add(infoPanel, gbc);

        //Control part
        JPanel controlPanel = new JPanel();
        controlPanel.add(pdfPathButton);
        controlPanel.add(ignoredPathButton);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        JPanel viewPanel = new JPanel();
        LayoutManager layout = new BorderLayout();
        viewPanel.setLayout(layout);
        viewPanel.add(BorderLayout.NORTH, fieldPanel);
        viewPanel.add(BorderLayout.CENTER, centerPanel);
        viewPanel.add(BorderLayout.SOUTH, controlPanel);
        setContentPane(viewPanel);

        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        pdfPathButton.addActionListener(this);
        ignoredPathButton.addActionListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }


    /**
     * Set the new result
     *
     * @param count the total of words processed until now
     * @param result the result
     * @param workEnded to let the view know if the job is finished or not
     */
    public void updateResult(final int count, final List<Pair<String, Integer>> result, final Boolean workEnded){
        SwingUtilities.invokeLater(() -> this.setResult(count, result, workEnded));
    }

    /**
     * Change the view state
     *
     * @param s the new state
     */
    public void updateState(final String s){
        SwingUtilities.invokeLater(() -> {
            state.setText(s);
        });
    }

    /**
     * Checks witch event has been executed
     * @param ev the event
     */
    public void actionPerformed(ActionEvent ev){
        String cmd = ev.getActionCommand();

        switch(cmd) {
            case "Choose directory" :
            {
                chooseDirectory();
                break;
            }
            case "Choose file" :
            {
                chooseFile();
                break;
            }
            case "Start" :
            {
                emptyTextArea();
                notifyStarted();
                break;
            }
            case "Stop" :
            {
                notifyStopped();
                break;
            }
            default :
            {
                checkNumber(cmd);
            }
        }
    }

    private void setResult(final int count, final List<Pair<String, Integer>> result, final Boolean workEnded) {
        textArea.setText(" Words processed: " + count);
        if(workEnded) {
            textArea.append("\n\n [Most Frequent Words]");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
        else {
            textArea.append("\n\n [Partial Most Frequent Words]");
        }

        for (Pair<String, Integer> pair : result) {
            textArea.append("\n  "+ pair.toString());
        }
    }

    private void emptyTextArea() {
        textArea.setText("");
    }

    private void chooseDirectory() {

        pdfPathFieldChooser.setDialogTitle("Select PDF directory");
        pdfPathFieldChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        pdfPathFieldChooser.setAcceptAllFileFilterUsed(false);

        if (pdfPathFieldChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            pdfPathName.setText(pdfPathFieldChooser.getSelectedFile().getPath());
        }
        else {
            System.out.println("No Selection ");
        }
    }

    private void chooseFile() {
        ignoredPathChooser.setDialogTitle("Select ignored word file");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
        ignoredPathChooser.setFileFilter(filter);

        if (ignoredPathChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            ignoredPathField.setText(ignoredPathChooser.getSelectedFile().getPath());
        }
        else {
            System.out.println("No Selection ");
        }
    }

    private void checkNumber(final String text) {
        int num = 0;
        try{
            num = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            nWordsField.setText("0");
        }

        if (num < 0)
            nWordsField.setText("0");

    }

    private void notifyStarted(){
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        String pdfPath = pdfPathName.getText();
        String ignoredPath = ignoredPathField.getText();
        int nWords = Integer.parseInt(nWordsField.getText());

        this.viewEvent.notifyStart(pdfPath, ignoredPath, nWords);
    }

    private void notifyStopped(){
        stopButton.setEnabled(false);
        startButton.setEnabled(true);

        this.viewEvent.notifyStop();

    }
}

