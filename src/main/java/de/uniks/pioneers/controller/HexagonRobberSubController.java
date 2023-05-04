package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.HexagonData;
import de.uniks.pioneers.model.HexagonPoint;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.Tile;
import de.uniks.pioneers.service.MapService;
import de.uniks.pioneers.service.PioneersService;
import javafx.scene.input.MouseEvent;
import org.hexworks.mixite.core.api.Hexagon;
import org.hexworks.mixite.core.api.Point;

import java.util.*;

public class HexagonRobberSubController extends HexagonPointSubController {

    private final Hexagon<HexagonData> myHexagon;
    private final HashMap<Point, HexagonPointSubController> pointToSubCon;
    private final Map<String, String> colorToUserID;
    private final Map<String, Player> userIdToPlayer;
    private final RobSubController robSubController;
    private String ignoreColor;      //The Color the Robber will ignore when looking for victims, presumably the Color of the player who put them there

    public HexagonRobberSubController(Hexagon<HexagonData> myHexagon, PioneersService pioneersService, Constants.HEX_SUBCON_TYPE type,
                                      MapService mapService, ResourceBundle resourceBundle, int id,
                                      HashMap<Point, HexagonPointSubController> pointToSubCon, Map<String, String> colorToUserID, Map<String, Player> userIdToPlayer, RobSubController robSubController) {
        super(null, pioneersService, type, mapService, resourceBundle, id);
        this.myHexagon = myHexagon;
        this.pointToSubCon = pointToSubCon;
        this.colorToUserID = colorToUserID;
        this.userIdToPlayer = userIdToPlayer;
        this.robSubController = robSubController;
    }

    @Override
    public void init() {
        Point point = Point.fromPosition(myHexagon.getCenterX(), myHexagon.getCenterY());

        //Selection Circle
        drawSelectionCircle(point);
        getSelectCircle().setRadius(32);

        //imageView for e.g. buildings
        drawBuilding(point);
        //Random robber placements for spice
        Random random = new Random();
        getBuildingImage().setLayoutX(getBuildingImage().getLayoutX() + random.nextInt(20, 26) * (random.nextBoolean() ? 1 : -1));
        getBuildingImage().setLayoutY(getBuildingImage().getLayoutY() + random.nextInt(20, 26) * (random.nextBoolean() ? 1 : -1));
        getBuildingImage().setMouseTransparent(true);
    }

    @Override
    public void setControllerState(boolean interactable, String intendedAction, String ignoreColor) {
        super.setControllerState(interactable, intendedAction, null);
        this.ignoreColor = ignoreColor;
    }

    public Hexagon<HexagonData> getHexagon() {
        return this.myHexagon;
    }

    @Override
    void onSelectCircleClicked(MouseEvent mouseEvent) {
        //Check what players are connected to this tile to get the group of potential victims
        robSubController.clearVictims();

        Collection<HexagonPoint> pointsToInspect = myHexagon.getSatelliteData().get().getHexagonPoints();
        ArrayList<String> victimColors = new ArrayList<>();
        for (HexagonPoint hexPoint : pointsToInspect) {
            HexagonPointSubController subCon = pointToSubCon.get(hexPoint.point());
            if (subCon.getType().equals(Constants.HEX_SUBCON_TYPE.POINT) && subCon.hasSetBuilding() && !subCon.getBuildingColorString().equals(ignoreColor)
                    && !victimColors.contains(subCon.getBuildingColorString())) {
                victimColors.add(subCon.getBuildingColorString());
            }
        }

        String robTarget = null;
        //Based on the amount of victims, do the following things:
        //0 -> Send server call with none as the victim
        //1 -> Send server call with that one person as the victim
        //2 -> Open menu to select victim
        if (victimColors.size() == 1) {
            String candidateID = colorToUserID.get(victimColors.get(0));
            Player candidatePlayer = userIdToPlayer.get(candidateID);
            if (candidatePlayer.resources().unknown() > 0) {
                robTarget = candidateID;
            }
        } else {
            if (victimColors.size() > 1) {
                //Open Menu to select Target (UI Elements controlled and placed via RobberSubController)
                robSubController.setSelectedSubController(this);

                ArrayList<Player> victims = new ArrayList<>();
                for (String victimColor : victimColors) {
                    victims.add(userIdToPlayer.get(colorToUserID.get(victimColor)));
                }

                robSubController.drawVictims(victims);
                robSubController.showMenu();

                //NOTE: aber als solo member kann man doch mit bank traden?

            }
        }

        //Send call if it didnt trigger menu
        if (victimColors.size() <= 1) {
            robSomeone(robTarget);
        }
    }

    public void robSomeone(String robTarget) {
        Tile tile = myHexagon.getSatelliteData().get().getTile();
        super.compositeDisposable.add(pioneersService.makeMoveRob(intendedAction, tile.x().intValue(), tile.y().intValue(), tile.z().intValue(), robTarget)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(result -> {
                    if (result.rob() == null) {
                        showAlert("FATAL ERROR", result.createdAt());
                    } else {
                        robSubController.hideMenu();
                    }
                }));
    }
}
