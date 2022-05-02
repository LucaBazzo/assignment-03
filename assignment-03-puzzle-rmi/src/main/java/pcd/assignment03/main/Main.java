package pcd.assignment03.main;

import pcd.assignment03.view.View;

/**
 *
 * The class in which the program starts
 *
 */
public class Main {

    private final static Integer N_ROWS = 3;
    private final static Integer N_COLUMNS = 5;
    private final static String IMAGE_PATH = "resources/bletchley-park-mansion.jpg";

    public static void main(String[] args) {

        Controller controller = new Controller();
        View view = new View(N_ROWS, N_COLUMNS, IMAGE_PATH);

        view.addListener(controller);
    }

}
