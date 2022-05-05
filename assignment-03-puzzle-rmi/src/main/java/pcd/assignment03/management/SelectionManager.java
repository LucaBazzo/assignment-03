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
        //List<TileProperties> tilesetTest = new ArrayList<>();
        controller.updateView(convertPairListToTileset(pairList), isPuzzleCompleted());
        //pairList.forEach(p -> tilesetTest.add(new TileProperties(p.getFirst(), p.getSecond())));
        //controller.updateView(tilesetTest, isPuzzleCompleted());
    }

    public void displayTileset(Optional<List<Pair<Integer, Integer>>> pairList) {
        System.out.println(" tileset: " + pairList.toString());
        if(pairList.isPresent())
            convertPairListToTileset(pairList.get());
        System.out.println(" tileset: " + tiles.toString());
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

    private List<TileProperties> convertPairListToTileset(List<Pair<Integer, Integer>> pairList) {
        List<TileProperties> tilesCopy = List.copyOf(this.tiles);
        tilesCopy.forEach(tile -> tile.setCurrentPosition(pairList.get(tile.getCurrentPosition()).getFirst()));
        return tilesCopy;
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
