package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.service.*;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class MapEditorController implements Controller {

    final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final ResourceBundle resourceBundle;

    private final Provider<MapMenuController> mapMenuController;

    private final App app;
    private final MapService mapService;
    private final StringToKeyCodeService stringToKeyCodeService;
    private final PrefService prefService;
    private final UserService userService;
    private final MapEditorService mapEditorService;
    private final AnimationService animationService;
    @FXML
    public Button saveMapButton;
    @FXML
    public Pane root;
    @FXML
    public Button leaveEditorButton;
    @FXML
    public CheckBox tilesRandomToggle;
    @FXML
    public CheckBox numbersRandomToggle;
    @FXML
    public CheckBox harborsRandomToggle;
    @FXML
    public CheckBox tilesRandomToggleType;
    @FXML
    public CheckBox numbersRandomToggleType;
    @FXML
    public CheckBox harborsRandomToggleType;
    @FXML
    public TextField mapName_field;
    @FXML
    public TextArea mapDesc_field;
    @FXML
    public Button save_button;
    @FXML
    public Button cancel_button;
    @FXML
    public Label error_Label;
    @FXML
    public Pane mapPane;            //The pane where the actual map transformations happen
    @FXML
    public ImageView tilesPasture;
    @FXML
    public ImageView tilesHills;
    @FXML
    public ImageView tilesMountains;
    @FXML
    public ImageView tilesDesert;
    @FXML
    public ImageView tilesFields;
    @FXML
    public ImageView tilesForest;
    @FXML
    public StackPane numbers2;
    @FXML
    public StackPane numbers3;
    @FXML
    public StackPane numbers4;
    @FXML
    public StackPane numbers5;
    @FXML
    public StackPane numbers6;
    @FXML
    public StackPane numbers8;
    @FXML
    public StackPane numbers9;
    @FXML
    public StackPane numbers10;
    @FXML
    public StackPane numbers11;
    @FXML
    public StackPane numbers12;
    @FXML
    public ImageView harborsLumber;
    @FXML
    public ImageView harborsClay;
    @FXML
    public ImageView harborsWool;
    @FXML
    public ImageView harborsGrain;
    @FXML
    public ImageView harborsOre;
    @FXML
    public ImageView harborsGeneric;
    @FXML
    public Label loadingLabel;
    public ArrayList<ImageView> UItiles = new ArrayList<>();      //List of UI Tiles
    public ArrayList<ImageView> UIharbors = new ArrayList<>();      //List of UI Harbors
    public ArrayList<StackPane> UInumbers = new ArrayList<>();      //List of UI Numbers
    private PioneersMapController mapController;
    private MapTemplate mapToPatch;          //The map to patch. If this is null, a new map is created.

    @Inject
    public MapEditorController(ResourceBundle resourceBundle, Provider<MapMenuController> mapMenuController, App app, MapService mapService,
                               StringToKeyCodeService stringToKeyCodeService, PrefService prefService, UserService userService, MapEditorService mapEditorService, AnimationService animationService) {
        this.resourceBundle = resourceBundle;
        this.mapMenuController = mapMenuController;
        this.app = app;
        this.mapService = mapService;
        this.stringToKeyCodeService = stringToKeyCodeService;
        this.prefService = prefService;
        this.userService = userService;
        this.mapEditorService = mapEditorService;
        this.animationService = animationService;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {
        if (compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
        }
        if(mapController != null){
            mapController.destroy();
        }
    }

    @Override
    public Parent render() {
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/mapEditorScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        app.setScreenTitle(resourceBundle.getString(Constants.MAP_EDITOR_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());


        //Init map controller
        mapController = new PioneersMapController(true, mapPane, null, mapService, stringToKeyCodeService,
                null, prefService, resourceBundle, null, null, null,
                null, null, this, animationService);
        mapController.setEditorMapToPatch(mapToPatch);
        mapController.init();
        mapController.render();

        //Scoop up the relevant UI Elements in a list for ez transformations
        Collections.addAll(UItiles, tilesDesert, tilesFields, tilesForest, tilesHills, tilesMountains, tilesPasture);
        Collections.addAll(UIharbors, harborsClay, harborsGeneric, harborsGrain, harborsLumber, harborsOre, harborsWool);
        Collections.addAll(UInumbers, numbers2, numbers3, numbers4, numbers5, numbers6, numbers8, numbers9, numbers10, numbers11, numbers12);

        return parent;
    }

    @FXML
    private void leaveEditor() {
        app.show(mapMenuController.get());
    }

    @FXML
    private void saveMap() {
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/saveMapDialog.fxml"), resourceBundle);
        fxmlLoader.setControllerFactory(c -> this);
        try {
            //load Dialog Window
            DialogPane loader = fxmlLoader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader);
            dialog.setTitle(resourceBundle.getString("savemap"));
            Window window = dialog.getDialogPane().getScene().getWindow();
            dialog.initOwner(app.getPrimaryStage());
            dialog.show();
            window.setOnCloseRequest(event -> {
                dialog.setResult(ButtonType.CLOSE);
                root.setEffect(null);
            });
            root.setEffect(new BoxBlur(3, 3, 3));

            if (mapToPatch != null) {
                //Prefill desc/name with info available if we are just updating the map
                mapName_field.setText(mapToPatch.name());
                mapDesc_field.setText(mapToPatch.description());
            }

            save_button.setOnAction(event -> {
                //Upload map to server
                //Snapshot of mapPane (Shot is taken with current zoom, user responsibility to make it a good one)
                mapService.hideEditorGrid();
                String base64Snapshot = mapEditorService.generateMapThumbnail(mapPane);
                mapService.showEditorGrid();

                if (mapToPatch == null) {
                    //New Map!
                    compositeDisposable.add(mapEditorService.uploadMap(mapName_field.getText(), base64Snapshot, mapDesc_field.getText(),
                                    mapService.editorGenerateTileTemplates(mapController.getHexagonGrid()),
                                    mapService.editorGenerateHarborTemplates(mapController.getHexagonGrid()))
                            .observeOn(Constants.FX_SCHEDULER)
                            .subscribe(result -> {
                                if (result.name() != null) {
                                    //On success, show alert
                                    createMapOperationAlert(window, dialog);
                                } else {
                                    error_Label.setText(result.createdAt());
                                }
                            }));
                } else {
                    //Old Map, update it!
                    compositeDisposable.add(mapEditorService.updateMap(mapToPatch._id(), mapName_field.getText(), base64Snapshot, mapDesc_field.getText(),
                                    mapService.editorGenerateTileTemplates(mapController.getHexagonGrid()),
                                    mapService.editorGenerateHarborTemplates(mapController.getHexagonGrid()))
                            .observeOn(Constants.FX_SCHEDULER)
                            .subscribe(result -> {
                                if (result.name() != null) {
                                    //On success, show alert
                                    createMapOperationAlert(window, dialog);
                                } else {
                                    error_Label.setText(result.createdAt());
                                }
                            }));
                }
            });
            cancel_button.setOnAction(event -> {
                dialog.setResult(ButtonType.CLOSE);
                root.setEffect(null);
            });
            dialog.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMapOperationAlert(Window window, Dialog<ButtonType> dialog) {
        //On success, show alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION, resourceBundle.getString("map.upload.success"));
        alert.initOwner(window);
        alert.setHeaderText(resourceBundle.getString("map.status"));
        ButtonType continueButton = new ButtonType(resourceBundle.getString("map.continue"), ButtonBar.ButtonData.YES);
        ButtonType exitButton = new ButtonType(resourceBundle.getString("exit"), ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(exitButton, continueButton);
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == continueButton) {
                //Simply close the alert and dialog pane
                dialog.setResult(ButtonType.CLOSE);
                root.setEffect(null);
                alert.close();
            } else {
                //Leave the editor
                leaveEditor();
                dialog.setResult(ButtonType.CLOSE);
            }
        });
    }

    private Constants.MAP_ELEMENTS translateElementId(String elementId) {
        Constants.MAP_ELEMENTS element = null;
        switch (elementId) {
            case "tilesDesert" -> element = Constants.MAP_ELEMENTS.DESERT;
            case "tilesFields" -> element = Constants.MAP_ELEMENTS.FIELDS;
            case "tilesForest" -> element = Constants.MAP_ELEMENTS.FOREST;
            case "tilesHills" -> element = Constants.MAP_ELEMENTS.HILLS;
            case "tilesMountains" -> element = Constants.MAP_ELEMENTS.MOUNTAINS;
            case "tilesPasture" -> element = Constants.MAP_ELEMENTS.PASTURE;

            case "numbers2" -> element = Constants.MAP_ELEMENTS.NUMBER_2;
            case "numbers3" -> element = Constants.MAP_ELEMENTS.NUMBER_3;
            case "numbers4" -> element = Constants.MAP_ELEMENTS.NUMBER_4;
            case "numbers5" -> element = Constants.MAP_ELEMENTS.NUMBER_5;
            case "numbers6" -> element = Constants.MAP_ELEMENTS.NUMBER_6;
            case "numbers8" -> element = Constants.MAP_ELEMENTS.NUMBER_8;
            case "numbers9" -> element = Constants.MAP_ELEMENTS.NUMBER_9;
            case "numbers10" -> element = Constants.MAP_ELEMENTS.NUMBER_10;
            case "numbers11" -> element = Constants.MAP_ELEMENTS.NUMBER_11;
            case "numbers12" -> element = Constants.MAP_ELEMENTS.NUMBER_12;

            case "harborsLumber" -> element = Constants.MAP_ELEMENTS.HARBOR_LUMBER;
            case "harborsClay" -> element = Constants.MAP_ELEMENTS.HARBOR_BRICK;
            case "harborsWool" -> element = Constants.MAP_ELEMENTS.HARBOR_WOOL;
            case "harborsGrain" -> element = Constants.MAP_ELEMENTS.HARBOR_GRAIN;
            case "harborsOre" -> element = Constants.MAP_ELEMENTS.HARBOR_ORE;
            case "harborsGeneric" -> element = Constants.MAP_ELEMENTS.HARBOR_GENERIC;
        }
        return element;
    }

    public void tileClicked(MouseEvent mouseEvent) {
        ImageView tile = (ImageView) mouseEvent.getSource();
        String tileId = tile.getId();
        selectUIElement(tile);
        Constants.MAP_ELEMENT_TYPE type = Constants.MAP_ELEMENT_TYPE.TILE;
        Constants.MAP_ELEMENTS element = translateElementId(tileId);
        mapController.setLoadedMapElement(element, type);
    }

    public void numberClicked(MouseEvent mouseEvent) {
        StackPane number = (StackPane) mouseEvent.getSource();
        String numberId = number.getId();
        selectUIElement(number);
        Constants.MAP_ELEMENT_TYPE type = Constants.MAP_ELEMENT_TYPE.NUMBER;
        Constants.MAP_ELEMENTS element = translateElementId(numberId);
        mapController.setLoadedMapElement(element, type);
    }

    public void harborClicked(MouseEvent mouseEvent) {
        ImageView harbor = (ImageView) mouseEvent.getSource();
        String harborId = harbor.getId();
        selectUIElement(harbor);
        Constants.MAP_ELEMENT_TYPE type = Constants.MAP_ELEMENT_TYPE.HARBOR;
        Constants.MAP_ELEMENTS element = translateElementId(harborId);
        mapController.setLoadedMapElement(element, type);
    }

    private void selectUIElement(Node element) {
        //Lower everything
        resetUIElements();

        //Raise Element to show its selected
        element.setTranslateY(-16);
    }

    private void resetUIElements() {
        UIharbors.forEach(UIharbor -> UIharbor.setTranslateY(0));
        UInumbers.forEach(UInumber -> UInumber.setTranslateY(0));
        UItiles.forEach(UItile -> UItile.setTranslateY(0));
        tilesRandomToggle.setSelected(false);
        harborsRandomToggle.setSelected(false);
        numbersRandomToggle.setSelected(false);
        tilesRandomToggleType.setSelected(false);
        numbersRandomToggleType.setSelected(false);
        harborsRandomToggleType.setSelected(false);
        mapController.setEditorRandomElement(false);
    }

    public void hideLoadingLabel() {
        loadingLabel.setVisible(false);
    }

    @FXML
    public void toggleRandomTiles(ActionEvent actionEvent) {
        resetUIElements();
        tilesRandomToggle.setSelected(true);
        actionEvent.consume();
        mapController.setEditorRandomElement(true);
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.DESERT, Constants.MAP_ELEMENT_TYPE.TILE);
    }

    @FXML
    public void toggleRandomNumbers(ActionEvent actionEvent) {
        resetUIElements();
        numbersRandomToggle.setSelected(true);
        actionEvent.consume();
        mapController.setEditorRandomElement(true);
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.NUMBER_10, Constants.MAP_ELEMENT_TYPE.NUMBER);
    }

    @FXML
    public void toggleRandomHarbors(ActionEvent actionEvent) {
        resetUIElements();
        harborsRandomToggle.setSelected(true);
        actionEvent.consume();
        mapController.setEditorRandomElement(true);
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.HARBOR_BRICK, Constants.MAP_ELEMENT_TYPE.HARBOR);
    }

    @FXML
    public void toggleRandomTilesType(ActionEvent actionEvent) {
        resetUIElements();
        tilesRandomToggleType.setSelected(true);
        actionEvent.consume();
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.RANDOM_TILE, Constants.MAP_ELEMENT_TYPE.TILE);
    }

    @FXML
    public void toggleRandomNumbersType(ActionEvent actionEvent) {
        resetUIElements();
        numbersRandomToggleType.setSelected(true);
        actionEvent.consume();
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.RANDOM_NUMBER, Constants.MAP_ELEMENT_TYPE.NUMBER);
    }

    @FXML
    public void toggleRandomHarborsType(ActionEvent actionEvent) {
        resetUIElements();
        harborsRandomToggleType.setSelected(true);
        actionEvent.consume();
        mapController.setLoadedMapElement(Constants.MAP_ELEMENTS.HARBOR_RANDOM, Constants.MAP_ELEMENT_TYPE.HARBOR);
    }

    public void setMapToPatch(MapTemplate mapToPatch) {
        this.mapToPatch = mapToPatch;
    }
}
