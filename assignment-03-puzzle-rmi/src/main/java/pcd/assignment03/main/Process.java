package pcd.assignment03.main;


import pcd.assignment03.management.TileProperties;
import pcd.assignment03.view.View;

import java.util.List;

public interface Process {

    void initialize(View view, List<TileProperties> tiles);

    void tileSelected(TileProperties tile);
}
