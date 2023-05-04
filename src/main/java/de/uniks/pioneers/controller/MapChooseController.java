package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.GameSettings;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.service.GameService;
import de.uniks.pioneers.service.MapTemplateService;
import de.uniks.pioneers.service.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class MapChooseController implements Controller {

    private final App app;
    private final ResourceBundle resourceBundle;
    private final MapTemplateService mapTemplateService;
    private final UserService userService;
    private final GameService gameService;
    @FXML
    public TableView<MapTemplate> tableView_maps;
    @FXML
    public TableColumn<MapTemplate, ImageView> tableColumn_icon;
    @FXML
    public TableColumn<MapTemplate, String> tableColumn_name;
    @FXML
    public TableColumn<MapTemplate, String> tableColumn_creator;
    private Node blurBox;
    private final ObservableList<MapTemplate> mapTemplates = FXCollections.observableArrayList();
    private final Map<String, String> userIdToName = new HashMap<>();
    private MapTemplate selectedMap;


    @Inject
    public MapChooseController(App app, ResourceBundle resourceBundle, MapTemplateService mapTemplateService, UserService userService, GameService gameService) {
        this.app = app;
        this.resourceBundle = resourceBundle;
        this.mapTemplateService = mapTemplateService;
        this.userService = userService;
        this.gameService = gameService;
    }

    @Override
    public void init() {
        userService.getAllUsers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            result.forEach(user -> userIdToName.put(user._id(), user.name()));
            mapTemplateService.getAllMaps().observeOn(Constants.FX_SCHEDULER).subscribe(mapTemplates::setAll);
        });
    }

    @Override
    public void destroy() {
        if (blurBox != null) {
            blurBox.setEffect(null);
        }
    }

    @Override
    public Parent render() {
        String fxmlPath = "views/mapChoose.fxml";
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlPath), resourceBundle);
        fxmlLoader.setControllerFactory(c -> this);
        Parent parent = null;
        try {
            BoxBlur blur = new BoxBlur(3, 3, 3);
            if (blurBox != null) {
                blurBox.setEffect(blur);
            }

            //load Dialog Window
            DialogPane loader = fxmlLoader.load();
            parent = loader;
            if (blurBox != null) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader);
                dialog.setTitle(resourceBundle.getString("choose.map"));
                dialog.initOwner(app.getPrimaryStage());
                dialog.show();
                Window window = dialog.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> {
                    dialog.setResult(ButtonType.CLOSE);
                    this.destroy();
                });
            }

            tableColumn_name.setCellValueFactory(gameName -> new SimpleStringProperty(gameName.getValue().name()));
            tableColumn_creator.setCellValueFactory(ownerId -> {
                        if (userIdToName.get(ownerId.getValue().createdBy()) != null) {
                            return new SimpleStringProperty(userIdToName.get(ownerId.getValue().createdBy()));
                        } else {
                            return new SimpleStringProperty(resourceBundle.getString("name.unavailable"));
                        }
                    }
            );
            tableColumn_icon.setCellValueFactory(icon -> new ObservableValueBase<>() {
                @Override
                public ImageView getValue() {
                    MapTemplate map = icon.getValue();
                    String image = map.icon();
                    if (image != null) {
                        if (image.startsWith("data:;")) {
                            image = map.icon().substring(0, 5) + "image/png;" + map.icon().substring(6);
                        }
                    }
                    ImageView imageView;
                    if (image == null) {
                        imageView = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/no_map_icon.png"))));
                    } else if (image.startsWith("data:")) {
                        imageView = new ImageView(image);
                    } else {
                        imageView = new ImageView(new Image(image, true));
                    }
                    imageView.setFitWidth(64);
                    imageView.setPreserveRatio(true);
                    String tooltipText = map.description() != null && mapTemplateService.checkEmptyString(map.description()) ? map.description() : resourceBundle.getString("no.description");
                    imageView.setUserData(tooltipText);
                    imageView.setId(map._id());
                    return imageView;
                }
            });

            //adds tooltips that show the icon in original size as well as the description
            tableColumn_icon.setCellFactory(column ->
                    new TableCell<>() {
                        @Override
                        protected void updateItem(ImageView item, boolean empty) {
                            super.updateItem(item, empty);
                            if (item != null) {
                                setGraphic(item);
                                setTooltip(mapTemplateService.addMapTooltip(item.getImage(), item.getUserData().toString()));
                            }
                        }
                    });

            tableView_maps.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selectedMap = newValue);

            mapTemplates.addListener((ListChangeListener<? super MapTemplate>) c -> tableView_maps.getItems().setAll(c.getList().stream().toList()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        tableView_maps.setPlaceholder(new Label("Loading..."));
        return parent;
    }

    public void setBlurBox(Node blurBox) {
        this.blurBox = blurBox;
    }

    public void chooseMap(ActionEvent event) {
        Button button = (Button) event.getSource();

        if (selectedMap != null) {
            GameSettings old = gameService.getStoredGame().settings();
            GameSettings newSetting = new GameSettings(old.mapRadius(), old.victoryPoints(), selectedMap._id(), old.roll7(), old.startingResources());
            gameService.updateGame(null, null, false, newSetting, null).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                this.destroy();
                // get a handle to the stage
                Stage stage = (Stage) button.getScene().getWindow();
                // close profile settings menu
                stage.close();
            });
        }
    }

    public void resetDefault(ActionEvent event) {
        Button button = (Button) event.getSource();
        GameSettings old = gameService.getStoredGame().settings();
        GameSettings newSetting = new GameSettings(old.mapRadius(), old.victoryPoints(), null, old.roll7(), old.startingResources());
        gameService.updateGame(null, null, false, newSetting, null).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            this.destroy();
            // get a handle to the stage
            Stage stage = (Stage) button.getScene().getWindow();
            // close profile settings menu
            stage.close();
        });
    }
}
