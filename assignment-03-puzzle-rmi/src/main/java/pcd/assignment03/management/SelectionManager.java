package pcd.assignment03.management;

import pcd.assignment03.main.Controller;
import pcd.assignment03.utils.Pair;
import pcd.assignment03.view.Tile;

import java.awt.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectionManager {

    private final List<TileProperties> tiles;
    private final Controller controller;
    private final Peer peer;

    private Optional<TileProperties> selectedTile = Optional.empty();

    public SelectionManager(Controller controller, List<TileProperties> tiles) throws RemoteException {
        this.tiles = tiles;
        this.controller = controller;
        this.peer = new PeerImpl(this);
    }

    public void selectTile(TileProperties tile) {
        if(selectedTile.isPresent()) {
            this.swap(selectedTile.get(), tile);
            selectedTile = Optional.empty();
            this.controller.updateView(this.tiles, isPuzzleCompleted());
            this.peer.update(convertTilesetToPairList(this.tiles));
        }
        selectedTile = Optional.of(tile);
    }

    public List<Pair<Integer, Integer>> getPairList() {
        return convertTilesetToPairList(this.tiles);
    }

    public void updateTileset(List<Pair<Integer, Integer>> pairList) {
        System.out.println("Peer: tileset updated --> " + pairList);
        convertPairListToTileset(pairList);
        controller.updateView(this.tiles, isPuzzleCompleted());
    }

    public void displayTileset(Optional<List<Pair<Integer, Integer>>> pairList) {
        System.out.println(" tileset: " + pairList.toString());
        pairList.ifPresent(this::convertPairListToTileset);
        controller.displayView(this.tiles, isPuzzleCompleted());
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

        tiles.forEach(tile -> pairList.add(new Pair(tile.getCurrentPosition(), tile.getOriginalPosition())));

        return pairList;
    }

    private void log(String ... messages) {
        Arrays.stream(messages).forEach(msg -> System.out.println("[Selection Manager] " + msg));
    }
}
