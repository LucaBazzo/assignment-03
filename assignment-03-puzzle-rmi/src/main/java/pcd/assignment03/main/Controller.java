package pcd.assignment03.main;

import pcd.assignment03.management.SelectionManager;
import pcd.assignment03.utils.Pair;
import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.util.List;
import java.util.Optional;

/**
 *
 * The Controller of the processes
 *
 */
public class Controller implements Process {

    private View view;
    private SelectionManager selectionManager;

    public Controller() {
    }


    @Override
    public void initialize(View view, List<TileProperties> tiles) {
        this.view = view;
        this.selectionManager = new SelectionManager(tiles);
        this.view.display();
    }

    @Override
    public void tileSelected(TileProperties tile) {
        Optional<Pair<List<TileProperties>, Boolean>> result = this.selectionManager.selectTile(tile);

        result.ifPresent(res -> this.view.updateView(res.getFirst(), res.getSecond()));
    }
}
