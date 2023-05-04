package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class VictoryController implements Controller {

    private final App app;
    private final ResourceBundle resourceBundle;
    private final PioneersService pioneersService;
    private final PioneersUIService pioneersUIService;
    private final UserService userService;
    private final GameService gameService;
    private final VictoryStorage victoryStorage;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    private final ObservableList<Player> playerList = FXCollections.observableArrayList();
    @FXML
    public Button exitButton;
    @FXML
    public TableColumn<Player, HBox> placeList;
    @FXML
    public TableColumn<Player, Integer> citiesList;
    @FXML
    public TableColumn<Player, Integer> roadsList;
    @FXML
    public TableColumn<Player, Number> longestRoadList;
    @FXML
    public TableColumn<Player, Integer> settlementsList;
    @FXML
    public TableColumn<Player, HBox> playerProfileList;
    @FXML
    public TableColumn<Player, Number> victoryPointsList;
    @FXML
    public TableView<Player> victoryList;
    @FXML
    public Label gameNameLabel;
    @FXML
    public Label firstLabel;
    @FXML
    public Label secondLabel;
    @FXML
    public Label thirdLabel;
    @FXML
    public ImageView firstImage;
    @FXML
    public ImageView secondImage;
    @FXML
    public ImageView thirdImage;
    @FXML
    public TableColumn<Player, Number> armyList;
    @FXML
    public Label loading_label;
    private int victoryPoints;
    private Map<String, User> userHashMap;


    @Inject
    VictoryController(App app, ResourceBundle resourceBundle, PioneersService pioneersService, PioneersUIService pioneersUIService, UserService userService, GameService gameService, VictoryStorage victoryStorage, Provider<LobbySelectController> lobbySelectControllerProvider) {
        this.app = app;
        this.resourceBundle = resourceBundle;
        this.pioneersService = pioneersService;
        this.pioneersUIService = pioneersUIService;
        this.userService = userService;
        this.gameService = gameService;
        this.victoryStorage = victoryStorage;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
    }

    @Override
    public void init() {

    }

    @Override
    public Parent render() {
        // load victory screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/victoryScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (
                IOException e) {
            e.printStackTrace();
            return null;
        }

        app.setScreenTitle(resourceBundle.getString(Constants.VICTORY_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());

        gameNameLabel.setText(pioneersService.getStoredGame().name());

        victoryPoints = pioneersService.getStoredGame().settings().victoryPoints();

        // sort Player in list
        pioneersService.getGamePlayers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            for (int i = victoryPoints; i >= 0; i--) {
                for (Player player : result) {
                    if (player.victoryPoints().intValue() >= victoryPoints && i == victoryPoints) {
                        playerList.add(player);
                    } else {
                        if (player.victoryPoints().equals(i)) {
                            playerList.add(player);
                        }
                    }
                }
            }

            // create Podium
            setPodium(firstLabel, firstImage, 0);
            setPodium(secondLabel, secondImage, 1);
            setPodium(thirdLabel, thirdImage, 2);

            // fill table
            placeList.setCellValueFactory(place -> {
                Label number = new Label();
                ImageView imageView = new ImageView();
                imageView.setFitHeight(45);
                imageView.setFitWidth(45);
                imageView.getStyleClass().add("cup");

                for (int i = 0; i < playerList.size(); i++) {
                    if (playerList.get(i).userId().equals(place.getValue().userId())) {
                        number.setText(String.valueOf(i + 1));
                        // set cups
                        if (i == 0) {
                            imageView.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/victory_screen/trophy-gold.png"))));
                        } else if (i == 1) {
                            imageView.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/victory_screen/trophy-silver.png"))));
                        } else if (i == 2) {
                            imageView.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/victory_screen/trophy-orange.png"))));
                        }
                        break;
                    }
                }

                HBox playerPlace = new HBox(20, imageView, number);
                playerPlace.setAlignment(Pos.CENTER_LEFT);
                playerPlace.setPadding(new Insets(5));
                return new SimpleObjectProperty<>(playerPlace);
            });
            playerProfileList.setCellValueFactory(name -> {
                Label playerName = new Label(userHashMap.get(name.getValue().userId()).name());
                ImageView imageView = pioneersUIService.generatePlayerPortrait(userHashMap.get(name.getValue().userId()).avatar(), Color.valueOf(name.getValue().color()), 58);
                HBox profile = new HBox(30, imageView, playerName);
                profile.setAlignment(Pos.CENTER_LEFT);
                profile.setPadding(new Insets(5));
                return new SimpleObjectProperty<>(profile);
            });
            victoryPointsList.setCellValueFactory(points -> new SimpleObjectProperty<>(points.getValue().victoryPoints()));
            settlementsList.setCellValueFactory(settlement -> new SimpleObjectProperty<>(victoryStorage.getStartBuildings().settlement() - settlement.getValue().remainingBuildings().settlement()));
            citiesList.setCellValueFactory(cities -> new SimpleObjectProperty<>(victoryStorage.getStartBuildings().city() - cities.getValue().remainingBuildings().city()));
            roadsList.setCellValueFactory(roads -> new SimpleObjectProperty<>(victoryStorage.getStartBuildings().road() - roads.getValue().remainingBuildings().road()));
            longestRoadList.setCellValueFactory(longestRoad -> new SimpleObjectProperty<>(longestRoad.getValue().longestRoad()));
            armyList.setCellValueFactory(army -> {
                if ((victoryStorage.getKnights() == null)){
                    return new SimpleObjectProperty<>( 0);
                }else {
                    return new SimpleObjectProperty<>(victoryStorage.getKnights().getOrDefault(army.getValue().userId(), 0));
                }
            });
            victoryList.setItems(playerList);
            loading_label.setVisible(false);
            victoryList.setVisible(true);
        });


        return parent;
    }

    private void setPodium(Label placeLabel, ImageView placeImage, int i) {
        if (playerList.size() >= i + 1) {
            placeLabel.setText(userHashMap.get(playerList.get(i).userId()).name());
            pioneersUIService.generatePlayerPortrait(placeImage, userHashMap.get(playerList.get(i).userId()).avatar(), Color.valueOf(playerList.get(i).color()), 95);
        }
    }

    @Override
    public void destroy() {
    }

    public void setUserList(Map<String, User> userIdToName) {
        userHashMap = userIdToName;
    }

    public void exit(ActionEvent actionEvent) {
        if (pioneersService.getStoredData()._id().equals(pioneersService.getStoredGame().owner())) {
            gameService.deleteGame(pioneersService.getStoredGame()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                if (!result.createdAt().equals(Constants.LOBBY_DELETE_ERROR)) {
                    app.show(lobbySelectControllerProvider.get());
                }
            });
        } else {
            app.show(lobbySelectControllerProvider.get());
        }
        actionEvent.consume();
    }
}
