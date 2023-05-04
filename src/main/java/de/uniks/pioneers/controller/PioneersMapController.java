package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.animation.Interpolator;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.hexworks.mixite.core.api.CubeCoordinate;
import org.hexworks.mixite.core.api.Hexagon;
import org.hexworks.mixite.core.api.HexagonalGrid;
import org.hexworks.mixite.core.api.Point;
import org.hexworks.mixite.core.vendor.Maybe;

import javax.inject.Inject;
import java.util.Map;
import java.util.*;

public class PioneersMapController implements Controller {

    public final HashMap<Point, HexagonPointSubController> pointToSubCon = new HashMap<>();
    final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Pane inGamePane;                            //The Pane upon which the map is drawn.
    private final AnchorPane inGameAnchorPane;
    private final MapService mapService;
    private final StringToKeyCodeService stringToKeyCodeService;
    private final PioneersService pioneersService;
    private final PrefService prefService;
    private final ResourceBundle resourceBundle;
    private final EventListener eventListener;
    private final LoginResultStorage loginResultStorage;
    private final Group UImapGroup = new Group();           //This Group contains all the UI Hexagons for easy transformations.
    private final ArrayList<HexagonPointSubController> subCons = new ArrayList<>();
    private final RobSubController robMenuController;
    private final Map<String, Player> userIdToPlayer;
    private final Map<String, String> colorToUserID;
    private final Controller parentController;
    private final boolean editorMode;
    private final AnimationService animationService;
    private double mapCurrentDragX = 0;
    private double mapCurrentDragY = 0;
    private double mapStartingDragX = 0;
    private double mapStartingDragY = 0;
    private boolean mapDraggingActive = false;
    private double mapMaxDragX = 800;
    private double mapMaxDragY = 450;

    private double mapMaxZoomIn = 3;
    private double mapMaxZoomOut = 0.15;
    private double mapDefaultZoom = 1;
    private HexagonalGrid<HexagonData> hexagonGrid;
    private HexagonPointSubController recentSubController;     //Reference to the last subController that was interacted (e.g. build on) with
    private Point3D robberPosition = null;                      //Server coordinates, not local
    private boolean rejoin = false;
    private boolean robberMode = false;
    private String color;
    private MapTemplate editorMapToPatch = null;                //The map to patch. Information in here will be loaded shortly after the editor map is initialized.
    private Constants.MAP_ELEMENTS editorMapElement;            //The Hexagon Element "To be loaded", set externally.
    private Constants.MAP_ELEMENT_TYPE editorMapElementType;
    private Maybe<HexagonData> editorOldHexData;                  //The "Old" Hexagon Element. Relevant for hover-over stuff.
    private Hexagon<HexagonData> editorOldHarborHex;
    private boolean editorRandomElement;                        //Toggle which determines if the map element to be loaded should be random each time.
    private boolean editorClicked = false;
    private int editorMouseToHexClockRelation;

    @Inject
    public PioneersMapController(Boolean editorMode, Pane drawPane, AnchorPane inGameAnchorPane, MapService mapService, StringToKeyCodeService stringToKeyCodeService,
                                 PioneersService pioneersService, PrefService prefService, ResourceBundle resourceBundle, EventListener eventListener,
                                 LoginResultStorage loginResultStorage, RobSubController robMenuController, Map<String, Player> userIdToPlayer, Map<String,
            String> colorToUserID, Controller parentController, AnimationService animationService) {
        this.editorMode = editorMode;
        this.inGamePane = drawPane;
        this.inGameAnchorPane = inGameAnchorPane;
        this.mapService = mapService;
        this.stringToKeyCodeService = stringToKeyCodeService;
        this.pioneersService = pioneersService;
        this.prefService = prefService;
        this.resourceBundle = resourceBundle;
        this.eventListener = eventListener;
        this.loginResultStorage = loginResultStorage;
        this.robMenuController = robMenuController;
        this.userIdToPlayer = userIdToPlayer;
        this.colorToUserID = colorToUserID;
        this.parentController = parentController;
        this.animationService = animationService;
    }

    @Override
    public void init() {
        inGamePane.setOnMouseDragged(this::panMap);
        inGamePane.setOnMousePressed(this::panMapSetup);
        inGamePane.setOnScroll(this::zoomMap);

        inGamePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(stringToKeyCodeService.stringToKeyCode(prefService.getMapCenter()))) {
                resetMap();
            } else {
                try {
                    KeyCombination keyCombination = (KeyCombination) stringToKeyCodeService.stringToKeyCode(prefService.getMapCenter());
                    if (keyCombination.match(event)) {
                        resetMap();
                    }
                } catch (Exception ignored) {

                }
            }
        });

        //Get Map Information
        if (!editorMode) {
            //Online Map Setup
            compositeDisposable.add(pioneersService.getMap(pioneersService.getStoredGame()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(map -> {
                if (map.tiles() == null) {
                    //Error Case
                    showAlert(resourceBundle.getString(Constants.MAP_GET_ERROR), map.gameId());
                    return;
                }

                //Generate hexagon grid using map information and save it
                hexagonGrid = mapService.generateHexagonalGrid(map);

                //Generate map graphics
                mapService.generateMapGraphics(hexagonGrid, UImapGroup, false);

                //Add map graphics to game pane
                inGamePane.getChildren().add(UImapGroup);

                //Generate HexagonPoints and their SubControllers, initialize and log them in the translation map
                ArrayList<HexagonPoint> hexPoints = mapService.generateHexagonPoints(hexagonGrid);
                ArrayList<HexagonPoint> roadHexPoints = mapService.generateRoadHexagonPoints(hexPoints);
                hexPoints.addAll(roadHexPoints);

                int id = 0;
                for (HexagonPoint hexPoint : hexPoints) {
                    HexagonPointSubController hpCon = new HexagonPointSubController(hexPoint, pioneersService, hexPoint.isRoadHexagonPoint() ? Constants.HEX_SUBCON_TYPE.ROAD : Constants.HEX_SUBCON_TYPE.POINT, mapService, resourceBundle, id++);
                    hpCon.init();
                    subCons.add(hpCon);
                    pointToSubCon.put(hexPoint.point(), hpCon);
                }

                //Generate Robber Points
                for (Hexagon<HexagonData> hex : hexagonGrid.getHexagons()) {
                    Tile tile = hex.getSatelliteData().get().getTile();
                    if (!tile.type().equals(Constants.TILE_TYPE_HARBOR) && !tile.type().equals(Constants.TILE_TYPE_DUMMY)) {
                        HexagonRobberSubController hCon = new HexagonRobberSubController(hex, pioneersService,
                                Constants.HEX_SUBCON_TYPE.ROBBER, mapService, resourceBundle, id++, pointToSubCon, colorToUserID, userIdToPlayer, robMenuController);
                        hCon.init();
                        subCons.add(hCon);
                    }
                }

                //Tell HexagonPointSubControllers what their neighbor Controllers are
                mapService.hexagonPointsDetermineNeighbors(hexPoints, pointToSubCon);

                //Place Robber if there is a desert tile
                subCons.forEach(subCon -> {
                    if (subCon.getType().equals(Constants.HEX_SUBCON_TYPE.ROBBER) && robberPosition == null) {
                        HexagonRobberSubController robCon = (HexagonRobberSubController) subCon;
                        Tile tile = robCon.getHexagon().getSatelliteData().get().getTile();
                        if (tile.type().equals("desert")) {
                            robCon.setBuildingImage(Constants.ROBBER_IMAGE_PATH, null, null, null);
                            robberPosition = new Point3D(tile.x(), tile.y(), tile.z());
                        }
                    }
                });

                //Pull UI to the top
                inGameAnchorPane.toFront();

                //Remove loading label
                if (parentController != null) {
                    ((PioneersController) parentController).hideLoadingLabel();
                }

                //clip mapping pane
                clipPane();

                // Wait for finishing map building and then check if you rejoined the game to set the buildings
                if (rejoin) {
                    setBuildings();
                }

                //Determine zoom levels based on map size
                mapDefaultZoom = 1 / (hexagonGrid.getGridData().getGridHeight() / 8.0);
                mapMaxZoomOut = mapDefaultZoom * 0.85;
                mapMaxZoomIn = mapDefaultZoom + 2.5;
                UImapGroup.setScaleX(mapDefaultZoom);
                UImapGroup.setScaleY(mapDefaultZoom);

            }));
        } else {
            //Editor Setup
            mapMaxDragX = 406.5;
            mapMaxDragY = 375;

            //Generate Dummy Map
            hexagonGrid = mapService.generateEditorHexagonalGrid();

            //Generate Graphics
            mapService.generateMapGraphics(hexagonGrid, UImapGroup, true);

            //Add map graphics to game pane
            inGamePane.setVisible(false);
            inGamePane.getChildren().add(UImapGroup);

            //Set EditorEvents for Hexagon ImageViews
            mapService.getDrawLayer(Constants.DRAW_LAYER.HEXAGON_TILES).getChildren().forEach(hexGraphic -> {
                hexGraphic.setOnMouseEntered(this::editorOnMouseHoverEnter);
                hexGraphic.setOnMouseExited(this::editorOnMouseHoverExit);
                hexGraphic.setOnMouseClicked(this::editorOnMouseClick);
            });

            //Fill map with predefined information, if available
            if (editorMapToPatch != null) {
                mapService.populateEditorMap(hexagonGrid, editorMapToPatch);
                hexagonGrid.getHexagons().forEach(hex -> {
                    mapService.setHexagonGraphic(hex, true, true);
                });
            }

            UImapGroup.setScaleX(0.8);
            UImapGroup.setScaleY(0.8);

            //Remove loading label
            if (parentController != null) {
                ((MapEditorController) parentController).hideLoadingLabel();
            }

            inGamePane.setVisible(true);
            clipPane();
        }

        if (!editorMode) {
            //Websocket for buildings - created event places buildings on local player map
            compositeDisposable.add(eventListener.listen("games." + pioneersService.getStoredGame()._id() + ".buildings.*.*", Building.class)
                    .observeOn(Constants.FX_SCHEDULER)
                    .subscribe(event -> {
                        Building building = event.data();
                        if (event.event().endsWith(".created")) {
                            showCreatedBuildingOnMap(building);

                        } else if (event.event().endsWith(".updated")) {
                            showCreatedBuildingOnMap(building);
                        }
                    }));
        }
    }

    private boolean getPreviewCond(HexagonData hexData) {
        boolean previewCond = false;
        switch (editorMapElementType) {
            case TILE -> previewCond = hexData.getTile().type().equals(Constants.TILE_TYPE_DUMMY);
            case NUMBER -> previewCond = hexData.getTile().numberToken() == 0;
        }
        return previewCond;
    }

    private boolean getPreviewCond(Hexagon<HexagonData> hex, int mouseToHexClockRelation) {
        if (editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.HARBOR)) {
            //Get Neighbor at the side of the mouseToHexClockRelation
            Hexagon<HexagonData> targetHex = findHexToThisSide(hex, mouseToHexClockRelation);
            //If source hex is set and there is a dummy Hex at the side, place preview harbor aimed at this particular side
            return !hex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_DUMMY) && !hex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_HARBOR) &&
                    targetHex != null && targetHex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_DUMMY);
        } else {
            return getPreviewCond(hex.getSatelliteData().get());
        }
    }

    private Hexagon<HexagonData> findHexToThisSide(Hexagon<HexagonData> hex, int side) {
        //Returns the hexagon next to this one, towards the given side.
        Collection<Hexagon<HexagonData>> HexagonRegion = hexagonGrid.getNeighborsOf(hex);
        Hexagon<HexagonData> targetHex = null;
        for (Hexagon<HexagonData> candidateHex : HexagonRegion) {
            if (mapService.getClockRelation(hex.getCenterX(), hex.getCenterY(), candidateHex.getCenterX(), candidateHex.getCenterY(), 30) == side) {
                targetHex = candidateHex;
                break;
            }
        }
        return targetHex;
    }

    private void editorOnMouseHoverEnter(MouseEvent mouseEvent) {
        //Show a "Preview" if the field is a dummy tile and also log the previous field content.
        editorClicked = false;
        if (editorMapElement != null && editorMapElementType != null) {
            ImageView element = (ImageView) mouseEvent.getSource();
            Hexagon<HexagonData> associatedHex = mapService.imageViewToHex.get(element);
            editorOldHexData = associatedHex.getSatelliteData();

            //The "raw" angle from the mouse to the hexagon center
            double mouseToHexCenterAngle = mapService.getAngle(associatedHex.getCenterX(), associatedHex.getCenterY(),
                    associatedHex.getCenterX() + mouseEvent.getX(), associatedHex.getCenterY() + mouseEvent.getY());

            //The "normalized" angle from mouse to hexagon center to multiples of 60 (corresponds to sides of the hex)
            int mouseToHexCenterAngleNormalized = (int) ((Math.round((mouseToHexCenterAngle) / 60.0)) * 60) % 360;

            //At last, the clock relation of the normalized angle to the hex
            editorMouseToHexClockRelation = ((mouseToHexCenterAngleNormalized / 30) + 3) % 12;

            if (getPreviewCond(associatedHex, editorMouseToHexClockRelation)) {
                //If we are in harbor mode, move associated Hex to harbor dir
                if (editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.HARBOR)) {
                    associatedHex = findHexToThisSide(associatedHex, editorMouseToHexClockRelation);
                    editorOldHarborHex = associatedHex;
                    editorOldHexData = associatedHex.getSatelliteData();
                }
                loadHexagon(associatedHex, editorMouseToHexClockRelation);
            } else {
                if (editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.HARBOR)) {
                    associatedHex = findHexToThisSide(associatedHex, editorMouseToHexClockRelation);
                    if (associatedHex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_HARBOR)) {
                        editorOldHarborHex = associatedHex;
                    } else {
                        editorOldHarborHex = null;
                    }
                    editorOldHexData = associatedHex.getSatelliteData();
                }
            }
        }
    }

    private void editorOnMouseHoverExit(MouseEvent mouseEvent) {
        //Cancel the preview - doesn't do anything if there wasn't a preview to begin with.
        if (editorMapElement != null && editorMapElementType != null) {
            ImageView element = (ImageView) mouseEvent.getSource();
            Hexagon<HexagonData> associatedHex = mapService.imageViewToHex.get(element);
            if (editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.HARBOR)) {
                if (editorOldHarborHex != null && editorOldHexData.get().getTile().type().equals(Constants.TILE_TYPE_DUMMY)) {
                    clearHexagon(editorOldHarborHex, editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.NUMBER));
                }
            } else {
                if (getPreviewCond(editorOldHexData.get())) {
                    clearHexagon(associatedHex, editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.NUMBER));
                }
            }
        }
    }

    private void loadHexagon(Hexagon<HexagonData> targetHex, int mouseToHexClockRelation) {
        //Loads the loaded map element into the targetHex
        if (editorMapElement != null && editorMapElementType != null) {
            boolean overwriteOldImage = true;
            String mapElementToLoad = editorRandomElement ? mapService.editorGetRandomMapElement(editorMapElementType).toString() : editorMapElement.toString();
            switch (editorMapElementType) {
                case TILE -> {
                    mapService.setHexagonContent(targetHex, mapElementToLoad, -1, -1, false, -1);
                    overwriteOldImage = !editorMapElement.toString().equals(editorOldHexData.get().getTile().type());
                }
                case NUMBER -> {
                    mapService.setHexagonContent(targetHex, null, Integer.parseInt(mapElementToLoad), -1, false, -1);
                    overwriteOldImage = false;
                }
                case HARBOR -> {
                    mapService.setHexagonContent(targetHex, mapElementToLoad, -1, -1, true, mouseToHexClockRelation);
                }
            }

            mapService.setHexagonGraphic(targetHex, true, overwriteOldImage);
        }
    }

    private void clearHexagon(Hexagon<HexagonData> targetHex, boolean onlyClearNumber) {
        //Clears the target hex of map element content, resetting it to its basic state.
        if (editorMapElement != null && editorMapElementType != null) {
            //Check for surrounding harbors
            Collection<Hexagon<HexagonData>> HexagonRegion = hexagonGrid.getNeighborsOf(targetHex);
            Tile targetTile = targetHex.getSatelliteData().get().getTile();

            List<Hexagon<HexagonData>> toClear = new ArrayList<>();
            toClear.add(targetHex);

            //yoink all neighbor harbors
            if (editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.TILE)) {
                for (Hexagon<HexagonData> candidateHex : HexagonRegion) {
                    if (candidateHex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_HARBOR)) {
                        Tile candidateTile = candidateHex.getSatelliteData().get().getTile();
                        Harbor harbor = mapService.getEquivalentHarbor(candidateTile);
                        if (Objects.equals(targetTile.x(), harbor.x()) && Objects.equals(targetTile.z(), harbor.z()) && Objects.equals(targetTile.y(), harbor.y())) {
                            toClear.add(candidateHex);
                        }
                    }
                }
            }

            toClear.forEach(hex -> {
                mapService.setHexagonContent(hex, onlyClearNumber ? null : Constants.TILE_TYPE_DUMMY, 0, -1, false, -1);
                mapService.setHexagonGraphic(hex, true, !editorMapElementType.equals(Constants.MAP_ELEMENT_TYPE.NUMBER));
            });
        }
    }

    public HexagonalGrid<HexagonData> getHexagonGrid() {
        return hexagonGrid;
    }

    private void editorOnMouseClick(MouseEvent mouseEvent) {
        if (editorMapElement != null && editorMapElementType != null) {
            ImageView element = (ImageView) mouseEvent.getSource();
            Hexagon<HexagonData> associatedHex = mapService.imageViewToHex.get(element);

            switch (editorMapElementType) {
                case TILE -> {
                    //Delete if this tile previously was not a dummy tile.
                    if (!editorOldHexData.get().getTile().type().equals(Constants.TILE_TYPE_DUMMY)) {
                        clearHexagon(associatedHex, false);
                    } else {
                        //Place tile again in case we just deleted it
                        if (editorClicked) {
                            loadHexagon(associatedHex, editorMouseToHexClockRelation);
                        }
                    }
                }
                case NUMBER -> {
                    //Delete if this tile previously was not a dummy tile.
                    if (!(!editorOldHexData.get().getTile().type().equals(Constants.TILE_TYPE_DUMMY)
                            && !editorOldHexData.get().getTile().type().equals("desert") && editorOldHexData.get().getTile().numberToken() == 0)) {
                        clearHexagon(associatedHex, true);
                    } else {
                        //Place number again in case we just deleted it
                        if (editorClicked) {
                            loadHexagon(associatedHex, editorMouseToHexClockRelation);
                        }
                    }
                }
                case HARBOR -> {
                    if (editorOldHarborHex != null) {
                        //If the hex was a dummy tile previously, retain the harbor from the preview
                        if (editorOldHexData.get().getTile().type().equals(Constants.TILE_TYPE_DUMMY)) {
                            editorOldHarborHex = null;
                        } else {
                            //otherwise, clear the hex if the other tile is a harbor
                            if (editorOldHexData.get().getTile().type().equals(Constants.TILE_TYPE_HARBOR)
                                    && editorOldHarborHex.getSatelliteData().get().getTile().type().equals(Constants.TILE_TYPE_HARBOR)) {
                                clearHexagon(editorOldHarborHex, false);
                                editorOldHarborHex = null;
                            }
                        }
                    }
                }
            }

            //Retain Preview
            editorOldHexData = associatedHex.getSatelliteData();
            editorClicked = true;
        }
    }

    @Override
    public void destroy() {
        if (compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
        }
        if (subCons.size() > 0) {
            subCons.clear();
        }
    }

    @Override
    public Parent render() {
        return null;
    }

    public void setLoadedMapElement(Constants.MAP_ELEMENTS mapElement, Constants.MAP_ELEMENT_TYPE mapElementType) {
        //The "Loaded" Map Element - determines what happens on an editor event.
        editorMapElement = mapElement;
        editorMapElementType = mapElementType;
    }

    private void showCreatedBuildingOnMap(Building building) {
        HexagonPointSubController subCon = findPointSubConFromBuilding(building);
        if (subCon != null) {
            //Log this subController as recently interacted with
            recentSubController = subCon;
            String owner = building.owner();
            Color color = Color.valueOf(userIdToPlayer.get(owner).color());
            subCon.setBuildingImage(mapService.buildingTypeToResource(building, false), mapService.buildingTypeToResource(building, true), color, building.type());
            //Reset Field Mode
            this.setFieldMode(Constants.FIELD_MODE.OFF, "", "", null);
        }
    }

    public HexagonPointSubController findPointSubConFromBuilding(Building building) {
        //Finds the related HexagonPointSubController based on the passed building coordinates.
        //Translate Server coords to grid coords
        CubeCoordinate coords = mapService.coordsServerToHexagonGrid((Integer) building.x(), (Integer) building.z());
        //Find affected hexPoint
        Maybe<Hexagon<HexagonData>> affectedHex = hexagonGrid.getByCubeCoordinate(coords);
        Collection<HexagonPoint> affectedHexPoints = affectedHex.get().getSatelliteData().get().getHexagonPoints();
        HashMap<Point, Integer> map = affectedHex.get().getSatelliteData().get().getHexPointToClock();

        for (HexagonPoint hexPoint : affectedHexPoints) {
            //Find associated side
            if (map.get(hexPoint.point()).equals(building.side())) {
                //Point found!
                return pointToSubCon.get(hexPoint.point());
            }
        }
        return null;
    }

    public boolean setFieldMode(Constants.FIELD_MODE mode, String intendedAction, String buildingType, Color color) {
        //Sets the Field Mode + the intendedAction string within the subControllers that is passed.
        //Example of intendedAction would be 'founding-settlement-1'
        //The "Field Mode" determines which points are clickable, and what action is "loaded" on Click of the controllers.

        boolean[] bubbleShow = {false};

        switch (mode) {
            case PLACE_FOUNDING_SETTLEMENT, PLACE_SETTLEMENT ->
                // Makes all Point SubControllers which are not set and have no set neighbor interactable
                    subCons.forEach(subCon -> {
                        if (subCon.canBuildSettlement(mode == Constants.FIELD_MODE.PLACE_FOUNDING_SETTLEMENT, color)) {
                            bubbleShow[0] = true;
                            subCon.setControllerState(true, intendedAction, buildingType);
                        }
                    });
            case PLACE_FOUNDING_ROAD, PLACE_ROAD ->
                // Makes all Road SubControllers which are not set interactable
                    subCons.forEach(subCon -> {
                        if (subCon.canBuildRoad(mode == Constants.FIELD_MODE.PLACE_FOUNDING_ROAD ? recentSubController.getId() : -1, color)) {
                            bubbleShow[0] = true;
                            subCon.setControllerState(true, intendedAction, buildingType);
                        }
                    });
            case PLACE_CITY ->
                // Make all Point SubController where you can build a city interactable
                    subCons.forEach(subCon -> {
                        if (subCon.canBuildCity(color)) {
                            bubbleShow[0] = true;
                            subCon.setControllerState(true, intendedAction, buildingType);
                        }
                    });
            case PLACE_ROBBER ->
                //Make all RobberSubControllers interactable
                    subCons.forEach(subCon -> {
                        if (subCon.getType().equals(Constants.HEX_SUBCON_TYPE.ROBBER)) {
                            HexagonRobberSubController robSubCon = (HexagonRobberSubController) subCon;
                            if (!robSubCon.hasSetBuilding()) {
                                Player player = userIdToPlayer.get(loginResultStorage.getLoginResult()._id());
                                bubbleShow[0] = true;
                                robSubCon.setControllerState(true, intendedAction, player.color());
                            }
                        }
                    });
            //Turn board "off" and reset values
            case OFF -> subCons.forEach(subCon -> subCon.setControllerState(false, "", ""));
        }
        return bubbleShow[0];
    }

    private void zoomMap(ScrollEvent scrollEvent) {
        double zoomFactor = 1.05;
        double deltaY = scrollEvent.getDeltaY();

        if (deltaY < 0) {
            zoomFactor = 0.95;
        }

        //Scale hexagon UI - only if it isn't breaching the max/min zoom values
        if (UImapGroup.getScaleX() * zoomFactor < mapMaxZoomIn && UImapGroup.getScaleX() * zoomFactor > mapMaxZoomOut) {
            UImapGroup.setScaleX(UImapGroup.getScaleX() * zoomFactor);
            UImapGroup.setScaleY(UImapGroup.getScaleY() * zoomFactor);
        }
        scrollEvent.consume();
    }

    public ArrayList<HexagonPointSubController> getSubCons() {
        return subCons;
    }

    private void panMapSetup(MouseEvent mouseEvent) {
        //Reset Drag Distance
        mapCurrentDragX = mouseEvent.getX();
        mapCurrentDragY = mouseEvent.getY();
        mapStartingDragX = mouseEvent.getX();
        mapStartingDragY = mouseEvent.getY();
        mapDraggingActive = false;
        mouseEvent.consume();
    }

    public void clipPane() {
        //Clips the pane, so you may not drag things over the letterBoxing
        final Rectangle outputClip = new Rectangle();
        outputClip.setWidth(inGamePane.getPrefWidth());
        outputClip.setHeight(inGamePane.getPrefHeight());
        inGamePane.setClip(outputClip);
    }

    private void panMap(MouseEvent mouseEvent) {
        //Only move if a minimum distance has been dragged
        if (mapDraggingActive || ((Math.abs(mapStartingDragX - mapCurrentDragX) + Math.abs(mapStartingDragY - mapCurrentDragY)) > Constants.MAP_DRAG_MINIMUM_DIST)) {
            //Get distance moved
            double distToMoveX = mouseEvent.getX() - mapCurrentDragX;
            double distToMoveY = mouseEvent.getY() - mapCurrentDragY;

            //Apply transformation to map elements if it doesnt go over the max dragging
            if (!editorMode || Math.abs(UImapGroup.getTranslateX() + distToMoveX) < mapMaxDragX && Math.abs(UImapGroup.getTranslateY() + distToMoveY) < mapMaxDragY) {
                UImapGroup.setTranslateX(UImapGroup.getTranslateX() + distToMoveX);
                UImapGroup.setTranslateY(UImapGroup.getTranslateY() + distToMoveY);

                mapDraggingActive = true;
            }
        }

        //Reset distance
        mapCurrentDragX = mouseEvent.getX();
        mapCurrentDragY = mouseEvent.getY();
        mouseEvent.consume();
    }

    private void resetMap() {
        //Reset any transformations performed on the map, putting it back at its default size and place
        UImapGroup.setScaleX(mapDefaultZoom);
        UImapGroup.setScaleY(mapDefaultZoom);
        UImapGroup.setTranslateX(0.0);
        UImapGroup.setTranslateY(0.0);
    }

    private void showAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, context);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public void updateRobberPosition(Point3D newRobberPos) {
        //Updates robber position based on what is in the currentState
        //Returns TRUE if robber position needed to be updated, FALSE otherwise
        if (newRobberPos != null && !newRobberPos.equals(robberPosition)) {
            //Find old and new robberSubCon that aligns with the positions
            HexagonRobberSubController oldSubCon = null;
            HexagonRobberSubController newSubCon = null;
            for (HexagonPointSubController subCon : subCons) {
                if (subCon.getType().equals(Constants.HEX_SUBCON_TYPE.ROBBER)) {
                    HexagonRobberSubController robSubCon = (HexagonRobberSubController) subCon;
                    Tile tile = robSubCon.getHexagon().getSatelliteData().get().getTile();

                    //Check for new position
                    if (tile.x().intValue() == newRobberPos.x().intValue() && tile.z().intValue() == newRobberPos.z().intValue()) {
                        newSubCon = robSubCon;
                    } else {
                        //Check for old position
                        if (robberPosition != null && tile.x().intValue() == robberPosition.x().intValue() && tile.z().intValue() == robberPosition.z().intValue()) {
                            oldSubCon = robSubCon;
                        }
                    }
                }
            }

            //Update Position of Robber and remove old one
            boolean old = oldSubCon != null;
            //oldSubCon.clearBuildingImage();
            if (newSubCon != null) {
                if (old) {
                    ImageView imageViewOld = oldSubCon.getBuildingImage();
                    ImageView imageViewNew = newSubCon.getBuildingImage();
                    HexagonRobberSubController finalNewSubCon = newSubCon;
                    HexagonRobberSubController finalOldSubCon = oldSubCon;
                    animationService.moveNode(imageViewOld, (float) (imageViewNew.getLayoutX() - imageViewOld.getLayoutX()), (float) (imageViewNew.getLayoutY() - imageViewOld.getLayoutY()), 1000, Interpolator.LINEAR).setOnFinished(e -> {
                        finalNewSubCon.setBuildingImage(Constants.ROBBER_IMAGE_PATH, null, null, null);
                        finalOldSubCon.clearBuildingImage();
                        animationService.moveNode(imageViewOld, 0, 0, 1, Interpolator.LINEAR);
                    });
                } else {
                    newSubCon.setBuildingImage(Constants.ROBBER_IMAGE_PATH, null, null, null);
                }
            }
            robberPosition = newRobberPos;
        }
    }

    public void setBuildings() {
        compositeDisposable.add(pioneersService.getBuildings().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            result.forEach(this::showCreatedBuildingOnMap);
            // If user rejoined in robber phase start the field mode
            if (robberMode) {
                setFieldMode(Constants.FIELD_MODE.PLACE_ROBBER, Constants.ACTION.ROB.toString(), null, Color.valueOf(color));
            }
        }));
    }

    public void setRejoin(boolean rejoin) {
        this.rejoin = rejoin;
    }

    public void setRobberMode(boolean robberMode, String color) {
        this.robberMode = robberMode;
        this.color = color;
    }

    public void setEditorRandomElement(boolean editorRandomElement) {
        this.editorRandomElement = editorRandomElement;
    }

    public void setEditorMapToPatch(MapTemplate editorMapToPatch) {
        this.editorMapToPatch = editorMapToPatch;
    }
}
