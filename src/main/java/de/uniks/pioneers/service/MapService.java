package de.uniks.pioneers.service;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.controller.HexagonPointSubController;
import de.uniks.pioneers.model.Map;
import de.uniks.pioneers.model.*;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import kotlin.Pair;
import org.hexworks.mixite.core.api.*;
import org.hexworks.mixite.core.vendor.Maybe;

import javax.inject.Inject;
import java.util.*;

public class MapService {

    private final HashMap<Tile, Harbor> tileToHarbor = new HashMap<>();
    private final Group roadImageLayer = new Group();
    private final Group buildingImageLayer = new Group();
    private final Group selectCircleLayer = new Group();
    private final Group harborImageLayer = new Group();
    private final Group hexagonTileNumberImageLayer = new Group();
    private final Group hexagonTileImageLayer = new Group();
    private final Group hexagonPlankImageLayer = new Group();
    private final Group robberImageLayer = new Group();
    //Server to Local Grid Offsets
    private int zOffset = 0;
    private int xOffset = 0;
    private double viewXOffset = 0;
    private double viewYOffset = 0;

    public HashMap<ImageView, Hexagon<HexagonData>> imageViewToHex = new HashMap<>();
    public HashMap<Hexagon<HexagonData>, ImageView> hexToTileImageView = new HashMap<>();
    public HashMap<Hexagon<HexagonData>, StackPane> hexToNumberStackPane = new HashMap<>();

    private final HashMap<Hexagon<HexagonData>, ImageView> hexToPlankImg = new HashMap<>();

    @Inject
    public MapService() {
    }

    public double getViewXOffset() {
        return viewXOffset;
    }

    public double getViewYOffset() {
        return viewYOffset;
    }

    public Group getDrawLayer(Constants.DRAW_LAYER drawLayer) {
        //Gets the Group ("Layer") that is drawn on.
        switch (drawLayer) {
            case HARBOR_PLANKS -> {
                return hexagonPlankImageLayer;
            }
            case BUILDINGS -> {
                return buildingImageLayer;
            }
            case HARBOR -> {
                return harborImageLayer;
            }
            case ROADS -> {
                return roadImageLayer;
            }
            case HEXAGON_TILES -> {
                return hexagonTileImageLayer;
            }
            case HEXAGON_TILE_NUMBERS -> {
                return hexagonTileNumberImageLayer;
            }
            case SELECT_CIRCLES -> {
                return selectCircleLayer;
            }
            case ROBBER -> {
                return robberImageLayer;
            }
        }
        return null;
    }

    public void hideEditorGrid(){
        hexagonTileImageLayer.getChildren().forEach(img -> {
            Hexagon<HexagonData> associatedHex = imageViewToHex.get(img);
            img.setVisible(!associatedHex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_DUMMY));
        });
    }

    public void showEditorGrid(){
        hexagonTileImageLayer.getChildren().forEach(img -> {
            img.setVisible(true);
        });
    }

    public List<TileTemplate> editorGenerateTileTemplates(HexagonalGrid<HexagonData> hexGrid){
        //Generates a list of Tile Templates based on the hexagonal content of the passed hex Grid
        List<TileTemplate> tiles = new ArrayList<>();
        for(Hexagon<HexagonData> hex : hexGrid.getHexagons()){
            Tile tile = hex.getSatelliteData().get().getTile();
            //If it isn't a harbor or dummy tile, convert it.
            if(!tile.type().equals(Constants.TILE_TYPE_DUMMY) && !tile.type().equals(Constants.TILE_TYPE_HARBOR)){
                tiles.add(new TileTemplate(tile.x(),tile.y(),tile.z(),tile.type().equals(Constants.TILE_TYPE_RANDOM) ? null : tile.type(),tile.numberToken() == -2 ? null : tile.numberToken()));
            }
        }
        return tiles;
    }

    public List<HarborTemplate> editorGenerateHarborTemplates(HexagonalGrid<HexagonData> hexGrid){
        //Generates a list of Harbor Templates based on the hexagonal content of the passed hex Grid
        List<HarborTemplate> harbors = new ArrayList<>();
        for(Hexagon<HexagonData> hex : hexGrid.getHexagons()){
            Tile tile = hex.getSatelliteData().get().getTile();
            //Only do stuff if it is a harbor tile.
            if(tile.type().equals(Constants.TILE_TYPE_HARBOR)){
                Harbor associatedHarbor = tileToHarbor.get(tile);
                harbors.add(new HarborTemplate(associatedHarbor.x().intValue(),associatedHarbor.y().intValue(),associatedHarbor.z().intValue(),associatedHarbor.type(),associatedHarbor.side()));
            }
        }
        return harbors;
    }

    public void setHexagonContent(Hexagon<HexagonData> hex, String type, int number, int id, boolean isHarbor, int harborSide){
        //If old data is present and type, number or id is null (or -1 in case of number and id), the old data will be reused instead.
        Maybe<HexagonData> oldData = hex.getSatelliteData();
        CubeCoordinate tileCoords = coordsHexagonGridToServer(hex.getCubeCoordinate());
        final Tile tile;
        if(!isHarbor) {
            tile = new Tile(tileCoords.getGridX(), tileCoords.getGridY(), tileCoords.getGridZ(),
                    (oldData.isPresent() && type == null) ? oldData.get().getTile().type() : type,
                    (oldData.isPresent() && number == -1) ? oldData.get().getTile().numberToken() : number);
        }
        else {
            int[] harborCoords = moveHexToSide(tileCoords.getGridX(), tileCoords.getGridY(), tileCoords.getGridZ(), (harborSide+6) % 12);
            Harbor harbor = new Harbor(harborCoords[0], harborCoords[1], harborCoords[2], type, harborSide);
            tile = harborToTile(harbor, 0);
            tileToHarbor.put(tile, harbor);
        }
        //Overwrite hex
        hex.setSatelliteData(new HexagonData(true, true, 0, tile,
                oldData.isPresent() ? oldData.get().getHexagonPoints() : new ArrayList<>(), (oldData.isPresent() && id == -1) ? oldData.get().getId() : id));
    }

    public void setHexagonGraphic(Hexagon<HexagonData> hex, boolean editorMode, boolean editorOverwriteOldImage){
        //Check if the hex already has an image associated with it, and clear it if yes.
        ImageView hexImageView = hexToTileImageView.get(hex);
        boolean newHexImageView = hexImageView == null;

        StackPane hexNumberStackPane = hexToNumberStackPane.get(hex);
        boolean newHexNumberStackPane = hexNumberStackPane == null;

        ImageView planksImageView = hexToPlankImg.get(hex);
        boolean newPlanksImageView = planksImageView == null;

        //Get tile data
        Tile tile = hex.getSatelliteData().get().getTile();
        //Hexagon Image
        Image hexImg = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(tileToResource(tile))));
        if(newHexImageView) {
            hexImageView = new ImageView(hexImg);
            imageViewToHex.put(hexImageView, hex);
            hexToTileImageView.put(hex, hexImageView);
            hexImageView.setId("hex"+tile.x()+tile.y()+tile.z());
        }
        else{
            //Overwrite old image
            if(editorOverwriteOldImage) {
                hexImageView.setImage(hexImg);
            }
        }

        final double xPos = hex.getCenterX() + viewXOffset;
        final double yPos = hex.getCenterY() + viewYOffset;
        final double xHexOffset = -Constants.TILE_CENTER_XOFFSET;
        final double yHexOffset = -Constants.TILE_CENTER_YOFFSET;

        if(newHexImageView) {
            //Define position within inGamePane
            hexImageView.setLayoutX(xPos);
            hexImageView.setLayoutY(yPos);
            hexImageView.setX(xHexOffset);
            hexImageView.setY(yHexOffset - (tile.type().equals(Constants.TILE_TYPE_HARBOR) && !editorMode ? 27 : 0));
            hexImageView.setMouseTransparent(!editorMode);

            //Add to respective UI Group
            if (tile.type().equals(Constants.TILE_TYPE_HARBOR)) {
                harborImageLayer.getChildren().add(hexImageView);
            } else {
                hexagonTileImageLayer.getChildren().add(hexImageView);
            }
        }
        else{
            //Set Mouse transparent if harbor
            hexImageView.setMouseTransparent(hex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_HARBOR));
        }

        //If Harbor, show planks with proper rotation based on side
        if (tile.type().equals(Constants.TILE_TYPE_HARBOR)) {
            Harbor harbor = getEquivalentHarbor(hex.getSatelliteData().get().getTile());
            Image planksGraphic = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/map assets/harbor/harbor_planks.png")));

            if(newPlanksImageView){
                //Determine Rotation
                planksImageView = new ImageView(planksGraphic);

                //Set Position
                planksImageView.setLayoutX(xPos);
                planksImageView.setLayoutY(yPos);
                planksImageView.setX(xHexOffset);
                planksImageView.setY(yHexOffset);
                planksImageView.setMouseTransparent(true);

                hexagonPlankImageLayer.getChildren().add(planksImageView);
                hexToPlankImg.put(hex, planksImageView);
            }
            else{
                planksImageView.setImage(planksGraphic);
            }

            if(harbor != null) {
                double rotation = -270 + harbor.side().intValue() * 30;
                planksImageView.setRotate(rotation);
            }
        }

        if(tile.type().equals(Constants.TILE_TYPE_DUMMY) && planksImageView != null){
            planksImageView.setImage(null);
        }

        //Number Background + Number
        if (!tile.type().equals("desert") && !tile.type().equals(Constants.TILE_TYPE_HARBOR) && !tile.type().equals(Constants.TILE_TYPE_DUMMY) && tile.numberToken() != 0) {

            //Get Number Background based on tile information
            Image numImg = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(tileToNumberBG(tile, tile.numberToken() == -2))));
            final ImageView numImageView;
            if(newHexNumberStackPane) {
                //Define new StackPane for easy centered overlapping
                hexNumberStackPane = new StackPane();
                hexToNumberStackPane.put(hex, hexNumberStackPane);
                numImageView = new ImageView(numImg);

                //Create Number Label
                Label num = new Label(tile.numberToken() == -2 ? "" : String.valueOf(tile.numberToken()));
                num.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                num.setTextAlignment(TextAlignment.CENTER);
                num.getStyleClass().add((tile.numberToken() == 8 || tile.numberToken() == 6) ? "redNumbers" : "blackNumbers");

                //Determine map position of stack pane
                hexNumberStackPane.setLayoutX(xPos - Constants.NUM_BG_CENTER_XOFFSET);
                hexNumberStackPane.setLayoutY(yPos - Constants.NUM_BG_CENTER_YOFFSET);

                hexNumberStackPane.getChildren().add(numImageView);
                hexNumberStackPane.getChildren().add(num);
                hexNumberStackPane.setMouseTransparent(true);

                hexagonTileNumberImageLayer.getChildren().add(hexNumberStackPane);
            }
            else{
                //Overwrite old content
                ImageView numberBG = (ImageView) hexNumberStackPane.getChildren().get(0);
                numberBG.setImage(numImg);
                Label numberLabel = (Label) hexNumberStackPane.getChildren().get(1);
                numberLabel.setText(tile.numberToken() == -2 ? "" : String.valueOf(tile.numberToken()));
                numberLabel.getStyleClass().clear();
                numberLabel.getStyleClass().add((tile.numberToken() == 8 || tile.numberToken() == 6) ? "redNumbers" : "blackNumbers");
            }
        }
        else{
            //If it is that kind of tile and we have a numberBG/Number Label, reset both of them to nill.
            if(!newHexNumberStackPane){
                //Overwrite old content
                ImageView numberBG = (ImageView) hexNumberStackPane.getChildren().get(0);
                numberBG.setImage(null);
                Label numberLabel = (Label) hexNumberStackPane.getChildren().get(1);
                numberLabel.setText("");
            }
        }
    }

    public void generateMapGraphics(HexagonalGrid<HexagonData> grid, Group UImapGroup, boolean editorMode) {
        //Generates the graphics for the hexagonalGrid and associates them in their passed Group

        //Add to UI with proper draw Order
        UImapGroup.getChildren().add(hexagonPlankImageLayer);
        UImapGroup.getChildren().add(hexagonTileImageLayer);
        UImapGroup.getChildren().add(hexagonTileNumberImageLayer);
        UImapGroup.getChildren().add(roadImageLayer);
        UImapGroup.getChildren().add(buildingImageLayer);
        UImapGroup.getChildren().add(harborImageLayer);
        UImapGroup.getChildren().add(selectCircleLayer);
        UImapGroup.getChildren().add(robberImageLayer);

        grid.getHexagons().forEach(hex -> {
            //Assign image if the hexagon's tile is not of the DUMMY type
            String type = hex.getSatelliteData().get().getTile().type();
            if (!type.equals(Constants.TILE_TYPE_DUMMY) || editorMode) {
                setHexagonGraphic(hex, editorMode, true);
            }
        });
    }

    public void hexagonPointsDetermineNeighbors(List<HexagonPoint> hexPoints, HashMap<Point, HexagonPointSubController> pointToSubCon) {
        //Tell HexagonPointSubControllers what their neighbor Controllers are
        hexPoints.forEach(hexPoint -> {
            //get controller of hexPoint
            HexagonPointSubController myController = pointToSubCon.get(hexPoint.point());

            //collect all neighbor controllers
            ArrayList<HexagonPointSubController> neighborSubControllers = new ArrayList<>();
            hexPoint.pointNeighbors().forEach(neighborPoint -> neighborSubControllers.add(pointToSubCon.get(neighborPoint.point())));

            //collect all neighbor road controllers
            ArrayList<HexagonPointSubController> neighborRoadSubControllers = new ArrayList<>();
            hexPoint.pointRoadNeighbors().forEach(neighborRoadPoint -> neighborRoadSubControllers.add(pointToSubCon.get(neighborRoadPoint.point())));

            //set neighborController list in root controller
            myController.setNeighborSubCons(neighborSubControllers);
            myController.setNeighborRoadSubCons(neighborRoadSubControllers);
        });
    }

    public CubeCoordinate coordsServerToHexagonGrid(int x, int z) {
        return CubeCoordinate.fromCoordinates(x + xOffset, z + zOffset);
    }

    public CubeCoordinate coordsHexagonGridToServer(CubeCoordinate hexCoord) {
        return CubeCoordinate.fromCoordinates(hexCoord.getGridX() - xOffset, hexCoord.getGridZ() - zOffset);
    }

    public double getAngle(double from_x, double from_y, double to_x, double to_y) {
        //Delivers the angle between two points.
        //0 = EAST | 90 = SOUTH| 180 = WEST | 270 = NORTH
        double theta = Math.atan2(to_y - from_y, to_x - from_x);
        double angle = Math.toDegrees(theta);
        if (angle < 0) {
            angle += 360;
        }

        return Math.round(angle);
    }

    public String tileToResource(Tile tile) {
        String path;
        if (!tile.type().equals(Constants.TILE_TYPE_HARBOR)) {
            if(!tile.type().equals(Constants.TILE_TYPE_DUMMY) && !tile.type().equals(Constants.TILE_TYPE_RANDOM)) {
                //Gets a tile and returns a random associated resource image file path with it
                path = "views/images/map assets/" + tile.type() + "/" + tile.type();
                Random rand = new Random();
                int index;
                if (!tile.type().equals("desert")) index = rand.nextInt(1, 6 + 1);
                else index = rand.nextInt(1, 2 + 1);

                path += "_" + index + ".png";
            }
            else{
                path = tile.type().equals(Constants.TILE_TYPE_DUMMY) ? "views/images/map assets/hexGrid.png" :  "views/images/map assets/randomTile.png";
            }
        } else {
            path = "views/images/map assets/harbor/" + "harbor_" + getHarborTileType(tile) + ".png";
        }

        return path;
    }

    public String tileToNumberBG(Tile tile, boolean randomNum) {
        //Gets a tile and returns the associated number BG
        return "views/images/map assets/elements/number-bgs/" + (randomNum ? "randomNumber" : tile.type()) + ".png";
    }

    public String buildingTypeToResource(Building building, boolean getRoof) {
        //Gets a building and returns the associated image file path with it
        String appendix = building.type().equals("road") ? "" : getRoof ? "_roof" : "_body";
        String path = "views/images/map assets/elements/" + building.type() + appendix;
        return path + ".png";
    }

    public boolean pointCollectionContains(Collection<Point> pointCollection, Point point) {
        // Helper function to see if a collection of points contains another point
        // Unfortunately we can't just use .contains(), since double values come with rounding errors
        ArrayList<Point> pointList = (ArrayList<Point>) pointCollection;
        for (Point collectionPoint : pointList) {
            if (Math.abs(collectionPoint.getCoordinateX() - point.getCoordinateX()) <= 1
                    && Math.abs(collectionPoint.getCoordinateY() - point.getCoordinateY()) <= 1) {
                return true;
            }
        }
        return false;
    }

    public void hexagonClockRelations(Hexagon<HexagonData> hex, Point point) {
        double angle = getAngle(hex.getCenterX(), hex.getCenterY(), point.getCoordinateX(), point.getCoordinateY());
        HashMap<Point, Integer> map = hex.getSatelliteData().get().getHexPointToClock();
        map.put(point, (((int) angle / 30) + 3) % 12);
    }

    public int getClockRelation(double from_x, double from_y, double to_x, double to_y, int normalizationFactor){
        //Clock relation from from point to to point

        //The "raw" angle from from_point to to_point
        double angle = getAngle(from_x, from_y, to_x, to_y);

        //The "normalized" angle to multiples of normalizationFactor (30 is normal, 60 would correspond to sides of hex)
        int angleNormalized = (int) ((Math.round((angle) / (double) normalizationFactor))*normalizationFactor) % 360;

        //At last, the clock relation of the normalized angle to the hex
        int clockRelation = ((angleNormalized / 30) + 3) % 12;

        return clockRelation;
    }

    public Hexagon<HexagonData> pointDetermineHexagonOwner(Point point, Collection<Hexagon<HexagonData>> pointHexNeighbors, boolean roadCheck) {
        //Determines which of the hexagons in the collection own the point
        Hexagon<HexagonData> hexOwner = null;
        for (Hexagon<HexagonData> adjacentHex : pointHexNeighbors) {
            int relation = adjacentHex.getSatelliteData().get().getHexPointToClock().get(point);
            if (!roadCheck && (relation == 0 || relation == 6) || roadCheck && (relation == 3 || relation == 7 || relation == 11)) {
                hexOwner = adjacentHex;
                break;
            }
        }
        return hexOwner;
    }

    public int harborTypeToInt(String harborType) {
        if (harborType == null) return -1;       //Has no type, means it's a generic harbor
        else {
            return switch (harborType) {
                case "grain" -> 0;
                case "brick" -> 1;
                case "ore" -> 2;
                case "lumber" -> 3;
                case "wool" -> 4;
                case "random" -> -2;
                default -> -1;          //generic
            };
        }
    }

    public Harbor getEquivalentHarbor(Tile tile) {
        return tileToHarbor.get(tile);
    }

    private List<Point> harborGetConnectedPoints(Hexagon<HexagonData> hexHarbor) {
        //Function that returns if the harbor is connected (e.g. has planks to) the hexagon Point
        Harbor harbor = tileToHarbor.get(hexHarbor.getSatelliteData().get().getTile());
        int harborSide = harbor.side().intValue();
        int legalClockside1 = ((harborSide + 5) % 12);
        int legalClockside2 = ((harborSide + 7) % 12);

        //Filter out "unconnected" points
        ArrayList<Point> connectedHexPoints = new ArrayList<>();
        for (HexagonPoint hexPoint : hexHarbor.getSatelliteData().get().getHexagonPoints()) {
            int clockRelation = hexHarbor.getSatelliteData().get().getHexPointToClock().get(hexPoint.point());
            if (clockRelation == legalClockside1 || clockRelation == legalClockside2) {
                connectedHexPoints.add(hexPoint.point());
            }
        }
        return connectedHexPoints;
    }

    public List<Harbor> getNeighborHarbors(HexagonPoint hexagonPoint) {
        //Returns the hexagonPoint's harbor neighbors in the form of a List.
        ArrayList<Harbor> neighborHarbors = new ArrayList<>();
        hexagonPoint.hexagonNeighbors().forEach(hex -> {
            Tile hexTile = hex.getSatelliteData().get().getTile();
            Harbor candidateHarbor = getEquivalentHarbor(hexTile);
            if (candidateHarbor != null) {
                List<Point> connectedPoints = harborGetConnectedPoints(hex);
                if (connectedPoints.contains(hexagonPoint.point())) {
                    neighborHarbors.add(candidateHarbor);
                }
            }
        });
        return neighborHarbors;
    }

    public String getHarborTileType(Tile harborTile) {
        int temp = harborTile.numberToken();
        return switch (temp) {
            case 0 -> Constants.HARBOR_TYPE.GRAIN.toString();
            case 1 -> Constants.HARBOR_TYPE.BRICK.toString();
            case 2 -> Constants.HARBOR_TYPE.ORE.toString();
            case 3 -> Constants.HARBOR_TYPE.LUMBER.toString();
            case 4 -> Constants.HARBOR_TYPE.WOOL.toString();
            case -1 -> Constants.HARBOR_TYPE.GENERIC.toString();
            case -2 -> Constants.HARBOR_TYPE.RANDOM.toString();
            default -> "ERROR";
        };
    }

    private int[] moveHexToSide(int source_x, int source_y, int source_z, int clockSide) {
        // moves the hexagon into the clockwise direction and returns the new coordinates in this format:
        // [x,y,z]
        switch (clockSide) {
            case 1 -> {
                source_x++;
                source_z--;
            }
            case 3 -> {
                source_x++;
                source_y--;
            }
            case 5 -> {
                source_z++;
                source_y--;
            }
            case 7 -> {
                source_x--;
                source_z++;
            }
            case 9 -> {
                source_x--;
                source_y++;
            }
            case 11 -> {
                source_z--;
                source_y++;
            }
        }

        return new int[]{source_x, source_y, source_z};
    }

    public List<Tile> harborsToTiles(List<Harbor> harbors) {
        // Comfort function that converts harbor to tile
        // the numberToken of a HarborTile represents its Type
        // This function also transposes the hexagon to the edge based on its side argument
        ArrayList<Tile> tiles = new ArrayList<>();
        if (harbors != null) {
            for (Harbor harbor : harbors) {
                Tile harborTile = harborToTile(harbor, 0);
                tiles.add(harborTile);
                tileToHarbor.put(harborTile, harbor);
            }
        }
        return tiles;
    }

    public Constants.MAP_ELEMENTS editorGetRandomMapElement(Constants.MAP_ELEMENT_TYPE type){
        Random random = new Random();
        int x;
        switch(type){
            case NUMBER -> {
                Constants.MAP_ELEMENTS[] pool = {Constants.MAP_ELEMENTS.NUMBER_2, Constants.MAP_ELEMENTS.NUMBER_3, Constants.MAP_ELEMENTS.NUMBER_4, Constants.MAP_ELEMENTS.NUMBER_5, Constants.MAP_ELEMENTS.NUMBER_6, Constants.MAP_ELEMENTS.NUMBER_8,
                        Constants.MAP_ELEMENTS.NUMBER_9, Constants.MAP_ELEMENTS.NUMBER_10, Constants.MAP_ELEMENTS.NUMBER_11, Constants.MAP_ELEMENTS.NUMBER_12};
                x = random.nextInt(0,10);
                return pool[x];
            }
            case TILE -> {
                Constants.MAP_ELEMENTS[] pool = {Constants.MAP_ELEMENTS.FIELDS, Constants.MAP_ELEMENTS.DESERT, Constants.MAP_ELEMENTS.PASTURE, Constants.MAP_ELEMENTS.HILLS, Constants.MAP_ELEMENTS.MOUNTAINS, Constants.MAP_ELEMENTS.FOREST};
                x = random.nextInt(0,6);
                if(pool[x].equals(Constants.MAP_ELEMENTS.DESERT)){
                    //Reroll in case of desert to make it less likely to appear
                    x = random.nextInt(0,6);
                }
                return pool[x];
            }
            case HARBOR -> {
                Constants.MAP_ELEMENTS[] pool = {Constants.MAP_ELEMENTS.HARBOR_GENERIC, Constants.MAP_ELEMENTS.HARBOR_BRICK, Constants.MAP_ELEMENTS.HARBOR_GRAIN, Constants.MAP_ELEMENTS.HARBOR_LUMBER, Constants.MAP_ELEMENTS.HARBOR_ORE, Constants.MAP_ELEMENTS.HARBOR_WOOL};
                x = random.nextInt(0,6);
                return pool[x];
            }
        }
        return null;
    }

    private Tile harborToTile(Harbor harbor, int sideOffset){
        //Converts a single Harbor into a tile
        int[] coords = moveHexToSide(harbor.x().intValue(), harbor.y().intValue(), harbor.z().intValue(), (harbor.side().intValue() + sideOffset) % 12);
        return new Tile(coords[0], coords[1], coords[2], Constants.TILE_TYPE_HARBOR, harborTypeToInt(harbor.type()));
    }

    public HexagonalGrid<HexagonData> generateEditorHexagonalGrid(){
        /*
            Generates the HexagonGrid used in the Editor. Unlike the usual approach where we take a map and then
            fit that to our grid, this time we do it the other way around.
         */

        //Build Hexagon Grid
        HexagonalGrid<HexagonData> grid = buildHexagonGrid(25, 25, CubeCoordinate.fromCoordinates(0,0));

        int idCounter = 0;
        //Populate grid with tile data using offsets
        for(Hexagon<HexagonData> hex : grid.getHexagons()){
            setHexagonContent(hex, Constants.TILE_TYPE_DUMMY, 0, idCounter++, false, -1);
        }

        //Calculate offset to center the grid within the screen
        determineViewOffsets(grid, 813, 750, true, CubeCoordinate.fromCoordinates(0,0));

        return grid;
    }

    public void populateEditorMap(HexagonalGrid<HexagonData> grid, MapTemplate source_map_template){
        //Populates the editor map with info present in the passed source_map.

        //Convert mapTemplate to map to load it into editor
        List<Tile> tiles = new ArrayList<>();
        source_map_template.tiles().forEach(tile -> {
            tiles.add(new Tile(tile.x(), tile.y(), tile.z(), tile.type() == null ? Constants.TILE_TYPE_RANDOM : tile.type(), tile.numberToken() == null ? -2 : tile.numberToken()));
        });
        List<Harbor> harbors = new ArrayList<>();
        source_map_template.harbors().forEach(harbor -> {
            harbors.add(new Harbor(harbor.x(), harbor.y(), harbor.z(), harbor.type(), harbor.side()));
        });
        de.uniks.pioneers.model.Map source_map = new de.uniks.pioneers.model.Map("0", tiles, harbors);

        populateHexagonalGrid(grid, source_map, true);
    }

    private HexagonalGrid<HexagonData> buildHexagonGrid(int grid_width, int grid_height, CubeCoordinate sourceMapCenterCoord){
        //Build Hexagonal grid using the calculated dimensions
        HexagonalGridBuilder<HexagonData> builder = new HexagonalGridBuilder<HexagonData>()
                .setGridHeight(grid_height)
                .setGridWidth(grid_width)
                .setGridLayout(HexagonalGridLayout.RECTANGULAR)
                .setOrientation(HexagonOrientation.POINTY_TOP)
                .setRadius(Constants.TILE_RADIUS);
        HexagonalGrid<HexagonData> grid = builder.build();

        //Calculate the hexagon coordinate offset to the relative center of the hexagon grid + store it
        zOffset = ((int) Math.floor((grid_height - 1.0) / 2.0)) - sourceMapCenterCoord.getGridZ();
        xOffset = ((int) Math.floor(-(zOffset / 2.0) + grid_width / 2.0)) - sourceMapCenterCoord.getGridX();

        return grid;
    }

    private MapToGridInfo getMapToGridInfo(Map source_map){
        //Figure out height/width of the Map Data by iterating over tile positions
        int highest_z = 0, lowest_z = 0, highest_x = 0, lowest_x = 0, highest_y = 0, lowest_y = 0;
        List<Tile> tiles = source_map.tiles();
        for (Tile tile : tiles) {
            highest_z = Math.max(highest_z, tile.z().intValue());
            lowest_z = Math.min(lowest_z, tile.z().intValue());
            highest_y = Math.max(highest_y, tile.y().intValue());
            lowest_y = Math.min(lowest_y, tile.y().intValue());
            highest_x = Math.max(highest_x, tile.x().intValue());
            lowest_x = Math.min(lowest_x, tile.x().intValue());
        }

        //Calculate Grid height/width
        int grid_height = Math.abs(lowest_z - highest_z) + 4;
        int grid_width = Math.max(Math.abs(lowest_x - highest_x), Math.abs(lowest_y - highest_y)) + 4;

        //Calculate CubeCoordinate of "true" center hex
        double factor = tiles.size() == 1 ? 1.0 : 2.0;
        CubeCoordinate centerCoord = CubeCoordinate.fromCoordinates((int) Math.round((lowest_x + highest_x)/factor), (int) Math.round((lowest_z + highest_z)/factor));

        return new MapToGridInfo(grid_height, grid_width, centerCoord);
    }

    public HexagonalGrid<HexagonData> generateHexagonalGrid(Map source_map) {

        /*
            Converts/Generates a HexagonGrid grid of the map information passed to this function, usually the server map

            IMPORTANT:
            The coordinates of the Hexagons in our HexagonGrid do not align with the coordinates of the Hexagons of
            the server map! This is because they both use different origin points.
            Use the CoordsServerToHexagonGrid(..) function to translate Server Coords. If you need the Server Coords
            from a HexagonGrid Hexagon, access the Tile associated within its HexagonData.
        */

        MapToGridInfo mapInfo = getMapToGridInfo(source_map);

        //Build Hexagon Grid
        HexagonalGrid<HexagonData> grid = buildHexagonGrid(mapInfo.grid_width(), mapInfo.grid_height(), mapInfo.centerCoord());

        int idCounter = populateHexagonalGrid(grid, source_map, false);

        //Catch leftover hexagons and assign DUMMY tiles to them, which store proper coordinates but have a dummy type
        for (Hexagon<HexagonData> hex : grid.getHexagons()) {
            if (hex.getSatelliteData().isEmpty()) {
                CubeCoordinate tileCoords = this.coordsHexagonGridToServer(hex.getCubeCoordinate());
                hex.setSatelliteData(new HexagonData(true, true, 0,
                        new Tile(tileCoords.getGridX(), tileCoords.getGridY(), tileCoords.getGridZ(), Constants.TILE_TYPE_DUMMY, 0), new ArrayList<>(), idCounter++));
            }
        }

        //Calculate offset to center the grid within the screen
        determineViewOffsets(grid, 1600, 900, false, mapInfo.centerCoord());

        return grid;
    }

    private int populateHexagonalGrid(HexagonalGrid<HexagonData> grid, Map source_map, boolean isEditorGrid){
        //Populates the HexagonalGrid with information present from the source_map
        //Returns the last given id

        //Convert Harbors to Tiles, then use that list for further operations
        //Harbors have the HARBOR type
        ArrayList<Tile> tilesAndHarbors = new ArrayList<>();
        tilesAndHarbors.addAll(source_map.tiles());
        tilesAndHarbors.addAll(harborsToTiles(source_map.harbors()));

        int idCounter = 0;
        //Populate grid with tile data using offsets
        for (Tile tile : tilesAndHarbors) {
            Maybe<Hexagon<HexagonData>> maybe_hex = grid.getByCubeCoordinate(coordsServerToHexagonGrid(tile.x().intValue(), tile.z().intValue()));
            if (maybe_hex.isPresent()) {
                maybe_hex.get().setSatelliteData(new HexagonData(true, true, 0, tile,
                        new ArrayList<>(), isEditorGrid ? maybe_hex.get().getSatelliteData().get().getId() : idCounter++));
            } else {
                System.out.println("ERROR: COULDN'T ASSOCIATE TILE WITH HEXAGON ON GRID");
            }
        }

        return idCounter;
    }

    private void determineViewOffsets(HexagonalGrid<HexagonData> grid, int paneWidth, int paneHeight, boolean editorMode, CubeCoordinate sourceMapCenterCoord) {
        //Calculate offset to center the grid within the screen
        Hexagon<HexagonData> currentCenterHex = grid.getByCubeCoordinate(coordsServerToHexagonGrid(sourceMapCenterCoord.getGridX(), sourceMapCenterCoord.getGridZ())).get();
        double currentCenterX = currentCenterHex.getCenterX();
        double currentCenterY = currentCenterHex.getCenterY();
        viewXOffset = paneWidth*0.5 - currentCenterX - (editorMode ? 30 : 0);
        viewYOffset = paneHeight*0.5 - currentCenterY - (!editorMode ? 42 : 0);
    }

    public ArrayList<HexagonPoint> generateHexagonPoints(HexagonalGrid<HexagonData> grid) {
        /*
            Generates HexagonPoint models for the previously generated grid. A HexagonPoint is a point of one (or more)
            Hexagons, which saves its pixel coordinate, and it's neighboring Hexagons/HexagonPoints. Each HexagonPoint
            is later given a HexagonPointSubController to drive logic based on it.
         */

        //List of "hit" points that have a HexagonPoint
        ArrayList<Point> hitPoints = new ArrayList<>();
        //List of created HexagonPoints
        ArrayList<HexagonPoint> hexPoints = new ArrayList<>();

        //Generate HexagonPoints by iterating over all Hexagons (which have a real tile associated with them) and their (Pixel) Points.
        grid.getHexagons().forEach(hex -> {
            String type = hex.getSatelliteData().get().getTile().type();
            if (!type.equals(Constants.TILE_TYPE_DUMMY) && !type.equals(Constants.TILE_TYPE_HARBOR)) {
                //Get Hexagon Region of Hexagon: Neighbors, and self.
                Collection<Hexagon<HexagonData>> HexagonRegion = grid.getNeighborsOf(hex);
                HexagonRegion.add(hex);

                //Iterate over (Pixel) Points of Hexagon
                hex.getPoints().forEach(point -> {
                    if (!pointCollectionContains(hitPoints, point)) {
                        //Marks a point as "hit" so we don't generate another HexagonPoint for it.
                        hitPoints.add(point);

                        //Find Hexagons within region which share this point: These are the point HexagonNeighbors
                        ArrayList<Hexagon<HexagonData>> pointHexNeighbors = new ArrayList<>();
                        HexagonRegion.forEach(regionHexagon -> {
                            if (pointCollectionContains(regionHexagon.getPoints(), point) && !pointHexNeighbors.contains(regionHexagon)) {
                                pointHexNeighbors.add(regionHexagon);
                            }
                        });

                        //Log the relation the Hexagons have to this new point, clockwise
                        pointHexNeighbors.forEach(adjacentHex -> hexagonClockRelations(adjacentHex, point));

                        //Determine which of my HexagonNeighbors is my owner
                        //A Hexagon owns a point if the clock relation to it is 0,3,6,7 or 11
                        Hexagon<HexagonData> hexOwner = pointDetermineHexagonOwner(point, pointHexNeighbors, false);

                        //Create hexagonPoint and save it in a list. Also save the associated point HexagonNeighbors in the hexagonPoint.
                        HexagonPoint newHexPoint = new HexagonPoint(point, new ArrayList<>(), pointHexNeighbors, hexOwner, new ArrayList<>(), false);
                        hexPoints.add(newHexPoint);

                        //Log this HexagonPoint on the previously identified HexagonNeighbors.
                        newHexPoint.hexagonNeighbors().forEach(hexNeighbor -> hexNeighbor.getSatelliteData().get().getHexagonPoints().add(newHexPoint));
                    }
                });
            }
        });

        //Find pointNeighbors of each HexagonPoint
        hexPoints.forEach(sourcePoint -> {
            //Collect all the HexagonPoints which might be a neighbor using hexagonNeighbors of the source HexagonPoint
            ArrayList<HexagonPoint> candidateHexPoints = new ArrayList<>();
            sourcePoint.hexagonNeighbors().forEach(hexNeighbor -> candidateHexPoints.addAll(hexNeighbor.getSatelliteData().get().getHexagonPoints()));

            //Determine which one is a neighbor by calculating distance
            candidateHexPoints.forEach(candidate -> {
                double dist = sourcePoint.point().distanceFrom(candidate.point());
                if (dist <= Constants.TILE_CENTER_YOFFSET + 1 && dist >= Constants.TILE_CENTER_YOFFSET - 1) {
                    sourcePoint.pointNeighbors().add(candidate);
                }
            });
        });

        return hexPoints;
    }

    public ArrayList<HexagonPoint> generateRoadHexagonPoints(ArrayList<HexagonPoint> hexPoints) {
        /*
            Generates HexagonPoint models for the previously generated grid. These are road hexagon points which lie
            in the middle of the "sides" of the hexagon. They work identically to the normal point HexagonPoints in that
            they have an owner and know their RoadHexagonPoint neighbors.
         */

        //List of created HexagonRoadPoints
        ArrayList<HexagonPoint> hexRoadPoints = new ArrayList<>();

        //List of "hit" HexagonRoadPoints, saved via a Pair of two HexagonPoints
        ArrayList<Pair<HexagonPoint, HexagonPoint>> hitRoadPoints = new ArrayList<>();

        //Generate HexagonRoadPoints by iterating over HexagonPoints
        hexPoints.forEach(hexPoint -> {
            //Iterate over neighbors
            hexPoint.pointNeighbors().forEach(neighborPoint -> {
                //If the pair isn't hit, make a new hexPoint
                Pair<HexagonPoint, HexagonPoint> candidatePair = new Pair<>(hexPoint, neighborPoint);
                if (!hitRoadPoints.contains(candidatePair)) {
                    //Log pair as hit - both ways
                    hitRoadPoints.add(candidatePair);
                    hitRoadPoints.add(new Pair<>(neighborPoint, hexPoint));

                    //Find pixel coordinate between these two points
                    double source_x = hexPoint.point().getCoordinateX();
                    double source_y = hexPoint.point().getCoordinateY();
                    double to_x = neighborPoint.point().getCoordinateX();
                    double to_y = neighborPoint.point().getCoordinateY();
                    Point newPoint = Point.fromPosition(source_x + (to_x - source_x) * 0.5, source_y + (to_y - source_y) * 0.5);

                    //Find shared hexagon neighbors
                    ArrayList<Hexagon<HexagonData>> sharedHexNeighbors = new ArrayList<>(hexPoint.hexagonNeighbors());
                    sharedHexNeighbors.retainAll(neighborPoint.hexagonNeighbors());

                    //Collect Point Neighbors
                    ArrayList<HexagonPoint> roadPointNeighbors = new ArrayList<>();
                    roadPointNeighbors.add(hexPoint);
                    roadPointNeighbors.add(neighborPoint);

                    //Log relations hexagons have to this new point, clockwise
                    sharedHexNeighbors.forEach(adjacentHex -> hexagonClockRelations(adjacentHex, newPoint));

                    //Find Hexagon Owner
                    Hexagon<HexagonData> hexOwner = pointDetermineHexagonOwner(newPoint, sharedHexNeighbors, true);

                    //Create new HexagonPoint and save it in the list
                    HexagonPoint newRoadHexPoint = new HexagonPoint(newPoint, roadPointNeighbors, sharedHexNeighbors, hexOwner, new ArrayList<>(), true);
                    hexRoadPoints.add(newRoadHexPoint);

                    //Log this new point on the hexagon neighbors
                    newRoadHexPoint.hexagonNeighbors().forEach(hexNeighbor -> hexNeighbor.getSatelliteData().get().getHexagonPoints().add(newRoadHexPoint));

                    //Log this new point on the point neighbors
                    roadPointNeighbors.forEach(pairPoint -> pairPoint.pointRoadNeighbors().add(newRoadHexPoint));
                }
            });
        });

        //Find Road Hexagon Point neighbors (roads with a distance of 0 roads to this road)
        hexPoints.forEach(hexPoint -> hexPoint.pointRoadNeighbors().forEach(roadHexPoint -> {
            for (HexagonPoint roadHexPointNeighbor : hexPoint.pointRoadNeighbors()) {
                //If the point isn't yourself and isn't already in the list, note the fellow roadHexPoints as roadHexPointNeighbors
                if (!roadHexPointNeighbor.point().equals(roadHexPoint.point()) && roadHexPoint.pointRoadNeighbors().stream().noneMatch(hexagonPoint -> hexagonPoint.point().equals(roadHexPointNeighbor.point()))) {
                    roadHexPoint.pointRoadNeighbors().add(roadHexPointNeighbor);
                }
            }
        }));

        return hexRoadPoints;
    }
}
