package pcd.assignment03.management;

import pcd.assignment03.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SelectionManager {

    private final List<TileProperties> tiles;

    private Optional<TileProperties> selectedTile = Optional.empty();

    public SelectionManager(List<TileProperties> tiles) {
        this.tiles = tiles;
    }

    public Optional<Pair<List<TileProperties>, Boolean>> selectTile(TileProperties tile) {
        if(selectedTile.isPresent()) {
            this.swap(selectedTile.get(), tile);
            selectedTile = Optional.empty();
            return Optional.of(new Pair<>(this.tiles, isPuzzleCompleted()));
        }
        selectedTile = Optional.of(tile);
        return Optional.empty();
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

    private void log(String ... messages) {
        Arrays.stream(messages).forEach(msg -> System.out.println("[Selection Manager] " + msg));
    }
}
