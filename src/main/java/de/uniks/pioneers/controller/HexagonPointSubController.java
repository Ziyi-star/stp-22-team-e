package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.HexagonData;
import de.uniks.pioneers.model.HexagonPoint;
import de.uniks.pioneers.model.Tile;
import de.uniks.pioneers.service.MapService;
import de.uniks.pioneers.service.PioneersService;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.hexworks.mixite.core.api.Hexagon;
import org.hexworks.mixite.core.api.Point;

import javax.inject.Inject;
import java.util.*;

public class HexagonPointSubController implements Controller {

    final PioneersService pioneersService;
    final CompositeDisposable compositeDisposable = new CompositeDisposable();
    final ScaleTransition scaleTransition = new ScaleTransition();
    private final HexagonPoint myHexagonPoint;
    private final ArrayList<HexagonPointSubController> neighborSubCons = new ArrayList<>();
    private final ArrayList<HexagonPointSubController> neighborRoadSubCons = new ArrayList<>();
    private final Constants.HEX_SUBCON_TYPE type;
    private final MapService mapService;
    private final ResourceBundle resourceBundle;
    private final int id;
    private final Group buildingGroup = new Group();                    //building body and roof
    public String intendedAction = "";                   //The action passed to the server on click - "founding-settlement-1", "build" etc.
    public String buildingType = "";                     //settlement, city, road
    private Circle selectCircle;
    private ImageView buildingImage;                //building = settlement, road, city etc.
    private ImageView buildingImageRoof;

    private ImageView spawnAnim;
    private boolean hasSettlement = false;
    private boolean hasCity = false;
    private Color buildingColor = null;                    //building color - this is used to test alliance of the building as well.

    @Inject
    public HexagonPointSubController(HexagonPoint myHexagonPoint, PioneersService pioneersService, Constants.HEX_SUBCON_TYPE type, MapService mapService, ResourceBundle resourceBundle, int id) {

        this.myHexagonPoint = myHexagonPoint;
        this.pioneersService = pioneersService;
        this.type = type;
        this.mapService = mapService;
        this.resourceBundle = resourceBundle;
        this.id = id;
    }

    @Override
    public void init() {
        /* Generate Views for this point + position them on the UI
             For testing purposes, the selectCircle and building has the following ID format:

             SelectCircle: selectCircle_XYZS - where X, Y, Z are the X,Y, Z (Server) Coordinates of the Hexagon Owner and S is the side (clock relation) of the point to the hexagon owner.
             EXAMPLE: the top point of the middle Hexagon would be "selectCircle_0000" while the bottom point would be "selectCircle_0006"

             Buildings follow the same logic except they are called "building_XYZS"

             If the subCon is a RobberSubController, the ID will omit the S (side) parameter in the name.
             EXAMPLE: the selectCircle for the middle Hexagon would be called "selectCircle_000"
         */

        //Selection Circle
        drawSelectionCircle(myHexagonPoint.point());

        //imageView for e.g. buildings
        drawBuilding(myHexagonPoint.point());

        //Spawn Anim for buildings
        drawSpawnAnim(myHexagonPoint.point());
    }

    @Override
    public void destroy() {
        if (compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public Parent render() {
        return null;
    }

    void drawSelectionCircle(Point point) {
        selectCircle = new Circle();
        selectCircle.setRadius(9.5);
        selectCircle.setFill(Color.RED);
        selectCircle.setOpacity(0.66);
        selectCircle.setLayoutX(point.getCoordinateX() + mapService.getViewXOffset());
        selectCircle.setLayoutY(point.getCoordinateY() + mapService.getViewYOffset());
        selectCircle.setId(getUIElementID(Constants.HEX_SUBCON_UI_ELEMENT.SELECT_CIRCLE));
        //By Default disabled
        selectCircle.setVisible(false);
        selectCircle.setMouseTransparent(true);

        this.selectCircle.setOnMouseClicked(this::onSelectCircleClicked);

        mapService.getDrawLayer(Constants.DRAW_LAYER.SELECT_CIRCLES).getChildren().add(selectCircle);
    }

    void drawSpawnAnim(Point point){
        spawnAnim = new ImageView();
        spawnAnim.setMouseTransparent(true);
        spawnAnim.setLayoutX(point.getCoordinateX() + mapService.getViewXOffset());
        spawnAnim.setLayoutY(point.getCoordinateY() + mapService.getViewYOffset());
        spawnAnim.setVisible(false);
        mapService.getDrawLayer(Constants.DRAW_LAYER.SELECT_CIRCLES).getChildren().add(spawnAnim);
    }

    String getUIElementID(Constants.HEX_SUBCON_UI_ELEMENT elementWanted) {

        Tile tile;
        if (this.type.equals(Constants.HEX_SUBCON_TYPE.ROBBER)) {
            HexagonRobberSubController robCon = (HexagonRobberSubController) this;
            tile = robCon.getHexagon().getSatelliteData().get().getTile();
        } else {
            tile = this.myHexagonPoint.hexagonOwner().getSatelliteData().get().getTile();
        }

        String type = elementWanted.equals(Constants.HEX_SUBCON_UI_ELEMENT.SELECT_CIRCLE) ? "selectCircle_" : "building_";

        return type + tile.x().intValue() + tile.y().intValue() + tile.z().intValue() + (!getType().equals(Constants.HEX_SUBCON_TYPE.ROBBER) ?
                myHexagonPoint.hexagonOwner().getSatelliteData().get().getHexPointToClock().get(this.myHexagonPoint.point()) : "");
    }

    void drawBuilding(Point point) {
        buildingImage = new ImageView();
        buildingImage.setMouseTransparent(true);
        buildingImage.setLayoutX(point.getCoordinateX() + mapService.getViewXOffset());
        buildingImage.setLayoutY(point.getCoordinateY() + mapService.getViewYOffset());
        buildingImage.setId(getUIElementID(Constants.HEX_SUBCON_UI_ELEMENT.BUILDING));
        if (type.equals(Constants.HEX_SUBCON_TYPE.POINT)) {
            buildingImageRoof = new ImageView();
            buildingImageRoof.setMouseTransparent(true);
            buildingImageRoof.setLayoutX(point.getCoordinateX() + mapService.getViewXOffset());
            buildingImageRoof.setLayoutY(point.getCoordinateY() + mapService.getViewYOffset());
        }

        Constants.DRAW_LAYER targetLayer;
        if (type.equals(Constants.HEX_SUBCON_TYPE.ROAD)) {
            targetLayer = Constants.DRAW_LAYER.ROADS;
        } else if (type.equals(Constants.HEX_SUBCON_TYPE.POINT)) {
            targetLayer = Constants.DRAW_LAYER.BUILDINGS;
        } else {
            targetLayer = Constants.DRAW_LAYER.ROBBER;
        }

        buildingGroup.getChildren().add(buildingImage);
        if (type.equals(Constants.HEX_SUBCON_TYPE.POINT)) {
            buildingGroup.getChildren().add(buildingImageRoof);
        }
        mapService.getDrawLayer(targetLayer).getChildren().add(buildingGroup);
    }

    public void setBuildingImage(String body_path, String roof_path, Color color, String buildingType) {
        //Function for setting the image of the building + setting the offset automatically based on size
        if(spawnAnim != null) {
            playSpawnAnim();
        }
        Image img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(body_path)));
        buildingImage.setImage(img);

        //set offset accordingly
        buildingImage.setX(-img.getWidth() * 0.5);
        buildingImage.setY(-img.getHeight() * 0.5);


        if (roof_path != null && type.equals(Constants.HEX_SUBCON_TYPE.POINT)) {
            Image roof = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(roof_path)));
            buildingImageRoof.setImage(roof);

            //set offset accordingly
            buildingImageRoof.setX(-roof.getWidth() * 0.5);
            buildingImageRoof.setY(-roof.getHeight() * 0.5);
        }

        //Blend image with given color
        if (color != null) {
            ImageView blendTarget = type.equals(Constants.HEX_SUBCON_TYPE.POINT) ? buildingImageRoof : buildingImage;
            Blend tint = new Blend();
            tint.setTopInput(new ColorInput((-img.getWidth() * 0.5), (-img.getHeight() * 0.5), blendTarget.getImage().getWidth(), blendTarget.getImage().getHeight(), color));
            tint.setMode(BlendMode.SRC_ATOP);
            tint.setOpacity(0.75);
            blendTarget.setEffect(tint);

            buildingColor = color;
        }

        //if it's a road, rotate to match hex edges
        if (type.equals(Constants.HEX_SUBCON_TYPE.ROAD)) {
            //figure out clock relation
            int clockRelation = myHexagonPoint.hexagonOwner().getSatelliteData().get().getHexPointToClock().get(myHexagonPoint.point());
            double rotation = switch (clockRelation) {
                case 1, 7 -> -60;
                case 11, 5 -> 60;
                default -> 0; // Case 3,9 in here
            };
            buildingImage.setRotate(rotation);
        }
    }

    public void clearBuildingImage() {
        buildingImage.setImage(null);
        if (buildingImageRoof != null) buildingImageRoof.setImage(null);
    }

    public Constants.HEX_SUBCON_TYPE getType() {
        return type;
    }

    public boolean hasSettlement() {
        return hasSettlement;
    }

    public boolean hasCity() {
        return hasCity;
    }

    public boolean hasSetBuilding() {
        return buildingImage.getImage() != null;
    }

    public ImageView getBuildingImage() {
        return buildingImage;
    }

    public Circle getSelectCircle() {
        return selectCircle;
    }

    public Color getBuildingColor() {
        return buildingColor;
    }

    public String getBuildingColorString() {
        // Omits the "0x" at the start if you do Color.toString as players etc. usually work w/o the 0x at front
        // Returns NULL if the building is not set.
        if (hasSetBuilding()) {
            return "#" + buildingColor.toString().substring(2);
        } else return null;
    }

    public HexagonPoint getMyHexagonPoint() {
        return this.myHexagonPoint;
    }

    public boolean hasSetPointNeighbors() {
        //Returns TRUE if one of the POINT neighbors has a set building, FALSE otherwise.
        for (HexagonPointSubController subCon : neighborSubCons) {
            if (subCon.hasSetBuilding()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSetRoadNeighbors() {
        //Returns TRUE if one of the ROAD neighbors has a set building, FALSE otherwise.
        for (HexagonPointSubController subCon : neighborRoadSubCons) {
            if (subCon.hasSetBuilding()) {
                return true;
            }
        }
        return false;
    }

    public List<HexagonPointSubController> getSetRoadNeighbors() {
        //Overloaded comfort function which will return all neighbors regardless of color
        return getSetRoadNeighbors(null);
    }

    public List<HexagonPointSubController> getSetRoadNeighbors(String color) {
        //Returns a List of all set Road Neighbors with the specified color.
        ArrayList<HexagonPointSubController> roadNeighbors = new ArrayList<>();
        for (HexagonPointSubController subCon : neighborRoadSubCons) {
            if (subCon.hasSetBuilding() && (color == null || subCon.getBuildingColorString().equals(color))) {
                roadNeighbors.add(subCon);
            }
        }
        return roadNeighbors;
    }

    public boolean pointNeighborsContain(int id) {
        //Returns TRUE if one of the POINT neighbors has a matching id
        for (HexagonPointSubController subCon : neighborSubCons) {
            if (subCon.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean canBuildRoad(int connectedPointNeighborId, Color color) {
        //Returns if a road can be build on this point. A road can be built if it is connected to a
        //settlement/city of the same color or a road of the same color and the point is free.
        // !! Always returns FALSE if the point is not a road point. !!
        //if connectedPointNeighborId is set to an ID, will only allow you to build next to that neighbor, if the other
        //requirements are met.

        if (type.equals(Constants.HEX_SUBCON_TYPE.ROAD) && buildingImage.getImage() == null) {
            //Collect all neighbors
            ArrayList<HexagonPointSubController> subCons = new ArrayList<>();
            subCons.addAll(neighborSubCons);
            subCons.addAll(neighborRoadSubCons);

            //Check if any of those have a building of the same color
            for (HexagonPointSubController subCon : subCons) {
                if (subCon.getBuildingColor() != null && subCon.getBuildingColor().equals(color) && (connectedPointNeighborId == -1 || pointNeighborsContain(connectedPointNeighborId))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canBuildSettlement(boolean isFoundingSettlement, Color color) {
        //Returns if a settlement can be build on this point. A settlement can be built if it is connected
        //to a road of the same color - or if its founding phase - the point is free, and neighbor points have no set buildings.
        if (type.equals(Constants.HEX_SUBCON_TYPE.POINT) && buildingImage.getImage() == null && !this.hasSetPointNeighbors()) {
            //If it's founding phase, no road connection required
            if (isFoundingSettlement) {
                return true;
            } else {
                //If it's not founding phase, look for road connection of the same color.
                for (HexagonPointSubController roadSubCon : neighborRoadSubCons) {
                    if (roadSubCon.hasSetBuilding() && roadSubCon.getBuildingColor() != null && roadSubCon.getBuildingColor().equals(color)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canBuildCity(Color color) {
        // Return true if you can build a city on this point. A city can be build if a settlement with the
        // same color is set on this point
        if (type.equals(Constants.HEX_SUBCON_TYPE.POINT) && buildingImage.getImage() != null && this.hasSettlement() && !this.hasCity()) {
            return this.getBuildingColor().equals(color);
        }
        return false;
    }

    public void setNeighborSubCons(Collection<HexagonPointSubController> neighborSubCons) {
        this.neighborSubCons.addAll(neighborSubCons);
    }

    public void setNeighborRoadSubCons(Collection<HexagonPointSubController> neighborRoadSubCons) {
        this.neighborRoadSubCons.addAll(neighborRoadSubCons);
    }

    public void setControllerState(boolean interactable, String intendedAction, String buildingType) {
        this.selectCircle.setMouseTransparent(!interactable);
        this.selectCircle.setVisible(interactable);

        // Scale animation
        if (interactable) {
            if (this.hasSettlement()) {
                // Animation if you build a city
                this.selectCircle.setFill(Color.TRANSPARENT);
                this.scaleAnimation(true, null);
            } else {
                // Animation for circle
                this.scaleAnimation(true, selectCircle);
            }
        } else {
            // Stop animation and reset node
            this.scaleAnimation(false, null);
        }
        this.intendedAction = intendedAction;
        this.buildingType = buildingType;
    }

    public int getId() {
        return id;
    }

    void scaleAnimation(boolean start, Circle selectCircle) {
        if (start) {
            scaleTransition.setNode(Objects.requireNonNullElse(selectCircle, buildingGroup));
            scaleTransition.setDuration(Duration.millis(700));
            scaleTransition.setCycleCount(ScaleTransition.INDEFINITE);
            scaleTransition.setInterpolator(Interpolator.LINEAR);
            scaleTransition.setByX(0.25);
            scaleTransition.setByY(0.25);
            scaleTransition.setAutoReverse(true);
            scaleTransition.play();
        } else {
            scaleTransition.jumpTo(Duration.ZERO);
            scaleTransition.stop();
        }
    }

    void playSpawnAnim(){
        Image spawnAnimImg = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/GameFXexport/GameFXexport/GIF_Files/LightCast_96.gif")));
        spawnAnim.setImage(spawnAnimImg);
        spawnAnim.setX(-spawnAnimImg.getWidth() * 0.5);
        spawnAnim.setY(-spawnAnimImg.getHeight() * 0.5);
        spawnAnim.setVisible(true);
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                spawnAnim.setImage(null);
                spawnAnim.setVisible(false);
            }
        };
        timer.schedule(task, 400);
    }

    void onSelectCircleClicked(MouseEvent mouseEvent) {
        //Send request to Server to build stuff based on intendedAction and buildingType loaded into subCon
        Hexagon<HexagonData> hexSource = myHexagonPoint.hexagonOwner();
        Tile tile = hexSource.getSatelliteData().get().getTile();

        if (type.equals(Constants.HEX_SUBCON_TYPE.POINT)) {
            if (buildingType.equals(Constants.CITY)) {
                this.hasSettlement = false;
                this.hasCity = true;
            } else {
                this.hasSettlement = true;
            }
        }

        compositeDisposable.add(pioneersService.makeMoveBuilding(intendedAction, tile.x(), tile.y(), tile.z(),
                        hexSource.getSatelliteData().get().getHexPointToClock().get(myHexagonPoint.point()), buildingType)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(result -> {
                    // Set the building for this sub controller
                    // If is not a road controller and building typ is city set has city true
                    // else you have built a settlement
                    if (result._id().equals("")) {
                        // Error show alert
                        showAlert(resourceBundle.getString(Constants.CREATE_BUILDING_ERROR), result._id());
                        if (hasCity) {
                            hasCity = false;
                        } else if (hasSettlement) {
                            hasSettlement = false;
                        }
                    }
                    mouseEvent.consume();
                }));
    }

    void showAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, context);
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
