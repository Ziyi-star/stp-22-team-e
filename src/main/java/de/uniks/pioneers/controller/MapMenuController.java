package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.model.Vote;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class MapMenuController implements Controller {

    private final ResourceBundle resourceBundle;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    private final App app;
    private final MapTemplateService mapTemplateService;
    private final LoginResultStorage loginResultStorage;
    private final ObservableList<MapTemplate> mapTemplates = FXCollections.observableArrayList();
    private final EventListener eventListener;
    private final Map<String, HBox> mapToHBox = new HashMap<>();
    private final Map<String, User> userIdToUserInfo = new HashMap<>();
    private final Provider<MapEditorController> mapEditorController;

    private final Map<String, Vote> mapIdToVote = new HashMap<>();

    private final CompositeDisposable voteDisposable = new CompositeDisposable();

    private final MusicService musicService;

    private final UserService userService;
    private final MapVoteService mapVoteService;
    @FXML
    public Label loading_label;
    @FXML
    public Pane blurBackground;
    @FXML
    public ListView<HBox> publicMapsList;
    @FXML
    public ListView<HBox> ownMapsList;
    @FXML
    public Label ownLabel;
    @FXML
    public Label publicLabel;
    @FXML
    public Pane renameMapsPane;
    @FXML
    public ListView<String> likeList;
    @FXML
    public ListView<String> dislikeList;
    @FXML
    public Pane votesPane;
    private Disposable mapDisposable;
    private Disposable userDisposable;
    @FXML
    private RenameController renameController;
    @FXML
    public AnchorPane root;
    @FXML
    public Pane imagePane;
    @FXML
    public ImageView mapPopup;
    @FXML
    public StackPane paneMapPopup;
    @FXML
    public Pane paneMapDescription;
    @FXML
    public Pane paneCreatorName;
    @FXML
    public Label mapDescriptionLabel;
    @FXML
    public Label creatorNameLabel;
    @FXML
    public Label creatorLabel;
    @FXML
    public Label descriptionLabel;

    @Inject
    public MapMenuController(App app, ResourceBundle resourceBundle, Provider<LobbySelectController> lobbySelectControllerProvider,
                             MapTemplateService mapTemplateService, LoginResultStorage loginResultStorage, EventListener eventListener,
                             UserService userService, MapVoteService mapVoteService, Provider<MapEditorController> mapEditorController, MusicService musicService) {
        this.app = app;
        this.resourceBundle = resourceBundle;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
        this.mapTemplateService = mapTemplateService;
        this.loginResultStorage = loginResultStorage;
        this.eventListener = eventListener;
        this.mapEditorController = mapEditorController;
        this.userService = userService;
        this.mapVoteService = mapVoteService;
        this.musicService = musicService;
    }

    @Override
    public void init() {
        // First get all votes from user and put it in hashmap
        // Next get all maps
        voteDisposable.add(userService.getAllUsers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> result.forEach(user -> userIdToUserInfo.put(user._id(), user))));
        voteDisposable.add(mapVoteService.getAllVotesFromUser(loginResultStorage.getLoginResult()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
            res.forEach(vote -> mapIdToVote.put(vote.mapId(), vote));
            voteDisposable.add(mapTemplateService.getAllMaps().observeOn(Constants.FX_SCHEDULER).subscribe(col -> {
                this.mapTemplates.setAll(col);
                for (MapTemplate mapTemplate : mapTemplates) {
                    renderItem(mapTemplate);
                }
                loading_label.setVisible(false);
            }));
        }));

        mapDisposable = eventListener
                .listen("maps.*.*", MapTemplate.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(event -> {
                    MapTemplate mapTemplate = event.data();
                    if (event.event().endsWith(".created")) {
                        if (!mapTemplates.contains(mapTemplate)) {
                            mapTemplates.add(mapTemplate);
                            renderItem(mapTemplate);
                        }
                    } else if (event.event().endsWith(".deleted")) {
                        HBox element_to_remove = mapToHBox.get(mapTemplate._id());
                        // Remove all elements from HBox
                        element_to_remove.getChildren().clear();
                        // Remove HBox from list view
                        if (publicMapsList.getItems().contains(element_to_remove)) {
                            publicMapsList.getItems().remove(element_to_remove);
                        } else {
                            ownMapsList.getItems().remove(element_to_remove);
                        }
                    } else if (event.event().endsWith(".updated")) {
                        // Update HBox white new information
                        HBox element = mapToHBox.get(mapTemplate._id());
                        ((Label) element.getChildren().get(1)).setText(mapTemplate.name());
                        Image iconImage;
                        if (mapTemplate.icon() == null) {
                            iconImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/no_map_icon.png")),
                                    128, 128, false, false);
                        } else {
                            iconImage = new Image(mapTemplate.icon(),
                                    128, 128, false, false);
                        }
                        ((ImageView) element.getChildren().get(0)).setImage(iconImage);
                        ((Label) ((VBox) element.getChildren().get(4)).getChildren().get(1)).setText(mapTemplate.votes().toString());

                        for (int i = 0; i < mapTemplates.size(); i++) {
                            //Update mapTemplates list element
                            if (mapTemplates.get(i)._id().equals(mapTemplate._id())) {
                                mapTemplates.set(i, mapTemplate);
                            }
                        }
                    }
                });

        voteDisposable.add(eventListener.listen("maps.*.votes." + loginResultStorage.getLoginResult()._id() + ".*", Vote.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(voteEvent -> {
                    Vote vote = voteEvent.data();
                    if (voteEvent.event().endsWith("created")) {
                        // If new vote is created put it in hashmap
                        if (!mapIdToVote.containsKey(vote.mapId())) {
                            mapIdToVote.put(vote.mapId(), vote);
                        }
                    } else if (voteEvent.event().endsWith("deleted")) {
                        // If vote is deleted remove it from hashmap
                        mapIdToVote.remove(vote.mapId());
                    } else if (voteEvent.event().endsWith("updated")) {
                        // If vote is updated replace old vote in hashmap
                        mapIdToVote.replace(vote.mapId(), vote);
                    }
                }));

        userDisposable = eventListener.listen("users.*.*", User.class).observeOn(Constants.FX_SCHEDULER).subscribe(userEvent -> {
            User user = userEvent.data();
            if (userEvent.event().endsWith(".created")) {
                userIdToUserInfo.put(user._id(), user);
            }
        });

        if(musicService.getMusicContext() == null || !musicService.getMusicContext().equals(Constants.MUSIC_CONTEXT.EDITOR)){
            musicService.playMusic(Constants.MUSIC_CONTEXT.EDITOR);
        }

    }

    @Override
    public void destroy() {
        if (this.mapDisposable != null) {
            mapDisposable.dispose();
        }

        if (voteDisposable.size() > 0) {
            voteDisposable.dispose();
        }
        if (this.renameController != null) {
            renameController.destroy();
        }
        if (this.userDisposable != null) {
            userDisposable.dispose();
        }
        mapToHBox.clear();
        userIdToUserInfo.clear();
        mapIdToVote.clear();
        mapTemplates.clear();
    }

    @Override
    public Parent render() {
        // load map menu screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/mapMenuScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> {
            if (c == MapMenuController.class) {
                return this;
            }
            if (c == RenameController.class) {
                return new RenameController(mapTemplateService);
            }
            return null;
        });
        final Parent parent;
        try {
            parent = loader.load();
        } catch (
                IOException e) {
            e.printStackTrace();
            return null;
        }

        app.setScreenTitle(resourceBundle.getString(Constants.MAP_MENU_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());

        ownLabel.setTextFill(Color.GRAY);

        // add a single HBox for Create Map

        final ImageView createImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/add.png"))), 128, 128);
        Label createLabel = new Label(resourceBundle.getString("create.map"));
        createLabel.setFont(new Font(32));
        createLabel.setPadding(new Insets(0, 0, 0, 50));
        createLabel.setAlignment(Pos.CENTER);

        HBox creatMapBox = new HBox(30, createImage, createLabel);
        creatMapBox.setPadding(new Insets(10));
        creatMapBox.setAlignment(Pos.CENTER_LEFT);
        creatMapBox.getStyleClass().add("hBoxOpacity");
        // create new maps
        creatMapBox.setOnMouseClicked(click -> app.show(mapEditorController.get()));
        ownMapsList.getItems().add(creatMapBox);

        renameController.setRenamePane(renameMapsPane);
        renameController.setBlur(blurBackground);


        return parent;
    }

    public void renderItem(MapTemplate map) {

        final Label nameLabel = new Label();
        nameLabel.setText(map.name());
        nameLabel.setPrefWidth(550);
        nameLabel.setFont(new Font(32));
        nameLabel.setPadding(new Insets(0, 50, 0, 50));
        nameLabel.setAlignment(Pos.CENTER);

        Image iconImage;
        if (map.icon() == null) {
            iconImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/no_map_icon.png")));
        } else {
            iconImage = new Image(map.icon());
        }

        final ImageView mapImage = getImageView(iconImage, 128, 128);
        mapImage.setSmooth(false);
        mapImage.setPickOnBounds(true);
        // map icon in list is clicked and map picture pops out on screen
        mapImage.setOnMouseClicked(click -> {
            // list image is clicked, image pane and map popup are visible and clickable and background is blurred
            imagePane.setVisible(true);
            imagePane.setMouseTransparent(false);
            blurBackground.setEffect(new BoxBlur(3, 3, 3));
            // show map image
            mapPopup.setFitHeight(Math.min(448,iconImage.getHeight()*1.5));
            mapPopup.setFitWidth(Math.min(448,iconImage.getWidth()*1.5));
            mapPopup.setImage(iconImage);
            creatorLabel.setText(resourceBundle.getString("creator"));
            descriptionLabel.setText(resourceBundle.getString("mapdesc"));
            if (userIdToUserInfo.get(map.createdBy()) != null) {
                creatorNameLabel.setText(userIdToUserInfo.get(map.createdBy()).name());
            } else {
                creatorNameLabel.setText(resourceBundle.getString("name.unavailable"));
            }
            if (map.description() != null) {
                mapDescriptionLabel.setText(map.description());
            } else {
                mapDescriptionLabel.setText(resourceBundle.getString("map.description.text"));
            }
        });
        final ImageView thumbUpImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/thumb_up.png"))), 82, 82);
        thumbUpImage.getStyleClass().add("thumbsUpStyle");
        final ImageView thumbDownImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/thumb_down.png"))), 82, 82);
        thumbDownImage.getStyleClass().add("thumbsDownStyle");
        thumbUpImage.setSmooth(false);
        thumbDownImage.setSmooth(false);
        // Set style if a +1 vote exists
        if (mapIdToVote.containsKey(map._id()) && mapIdToVote.get(map._id()).score().equals(1)) {
            thumbUpImage.getStyleClass().add("thumbsUpVoteStyle");
        }
        // Set style if a -1 vote exists
        if (mapIdToVote.containsKey(map._id()) && mapIdToVote.get(map._id()).score().equals(-1)) {
            thumbDownImage.getStyleClass().add("thumbsDownVoteStyle");
        }
        thumbUpImage.setOnMouseClicked(click -> {
            // Check if hashmap contains a vote for this map
            if (mapIdToVote.containsKey(map._id())) {
                if (mapIdToVote.get(map._id()).score().equals(1)) {
                    // Delete vote if it is +1
                    voteDisposable.add(mapVoteService.deleteVote(map._id(), loginResultStorage.getLoginResult()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                        if (res.score() != null) {
                            // Remove thump up style if vote is deleted
                            thumbUpImage.getStyleClass().removeAll("thumbsUpVoteStyle");
                        }
                    }));
                } else {
                    // Update vote to +1
                    voteDisposable.add(mapVoteService.updateVote(map._id(), loginResultStorage.getLoginResult()._id(), 1).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                        if (res.score() != null) {
                            // Remove thumps down style and add thumps up vote style to highlight vote
                            // If vote is updated from -1 to +1
                            thumbDownImage.getStyleClass().removeAll("thumbsDownVoteStyle");
                            thumbUpImage.getStyleClass().add("thumbsUpVoteStyle");
                        }
                    }));
                }
            } else {
                // Send a new +1 vote for this map
                voteDisposable.add(mapVoteService.sendVote(map._id(), 1).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                    if (res.score() != null) {
                        // Add thumps up style to highlight vote
                        thumbUpImage.getStyleClass().add("thumbsUpVoteStyle");
                    }
                }));
            }
        });

        thumbDownImage.setOnMouseClicked(click -> {
            // Check if hashmap contains a vote for this map
            if (mapIdToVote.containsKey(map._id())) {
                if (mapIdToVote.get(map._id()).score().equals(-1)) {
                    // Delete vote if it is -1
                    voteDisposable.add(mapVoteService.deleteVote(map._id(), loginResultStorage.getLoginResult()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                        if (res.score() != null) {
                            // Remove thump down style if vote is deleted
                            thumbDownImage.getStyleClass().removeAll("thumbsDownVoteStyle");
                        }
                    }));
                } else {
                    // Update vote to -1
                    voteDisposable.add(mapVoteService.updateVote(map._id(), loginResultStorage.getLoginResult()._id(), -1).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                        if (res.score() != null) {
                            // Remove thumps up style and add thumps down vote style to highlight vote
                            // If vote is updated from +1 to -1
                            thumbUpImage.getStyleClass().removeAll("thumbsUpVoteStyle");
                            thumbDownImage.getStyleClass().add("thumbsDownVoteStyle");
                        }
                    }));
                }
            } else {
                // Send a new -1 vote for this map
                voteDisposable.add(mapVoteService.sendVote(map._id(), -1).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                    if (res.score() != null) {
                        // Add thumps down style to highlight vote
                        thumbDownImage.getStyleClass().add("thumbsDownVoteStyle");
                    }
                }));
            }
        });

        //score elements in vbox
        final ImageView scoreImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/score.png"))), 155, 55);
        scoreImage.setSmooth(false);
        final Label scoreLabel = new Label();
        scoreLabel.setText(map.votes().toString());
        scoreLabel.setFont(new Font(26));
        VBox vBox = new VBox(10, scoreImage, scoreLabel);
        vBox.setPadding(new Insets(10));
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setPadding(new Insets(0, 0, 0, 150));
        vBox.getStyleClass().add("hoverStyle");
        vBox.setOnMouseClicked(click -> {
            // show rating list
            blurBackground.setEffect(new BoxBlur(3, 3, 3));
            votesPane.setVisible(true);
            votesPane.setMouseTransparent(false);
            likeList.getItems().clear();
            dislikeList.getItems().clear();

            voteDisposable.add(mapVoteService.getVotes(map._id()).observeOn(Constants.FX_SCHEDULER).subscribe(votes -> {
                for (Vote vote : votes) {
                    if (vote.score().intValue() == 1) {
                        likeList.getItems().add(userIdToUserInfo.get(vote.userId()).name());
                    } else if (vote.score().intValue() == -1) {
                        dislikeList.getItems().add(userIdToUserInfo.get(vote.userId()).name());
                    }
                }
            }));
        });

        final ImageView editImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/edit_square.png"))), 82, 82);
        editImage.setSmooth(false);
        editImage.getStyleClass().add("hoverStyle");
        editImage.setPickOnBounds(true);
        editImage.setOnMouseClicked(click -> {
            //Create Controller
            MapEditorController mapEditor = mapEditorController.get();
            mapEditor.setMapToPatch(map);
            //Show new Screen
            app.show(mapEditor);
        });
        final ImageView deleteImage = getImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/delete_icon.png"))), 82, 82);
        deleteImage.setSmooth(false);
        deleteImage.getStyleClass().add("hoverStyle");
        deleteImage.setPickOnBounds(true);
        // delete map
        deleteImage.setOnMouseClicked(click -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.initOwner(app.getPrimaryStage());
            alert.setResizable(false);
            alert.setContentText(resourceBundle.getString("deleteMap"));
            ButtonType yesButton = new ButtonType(resourceBundle.getString("yes"), ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType(resourceBundle.getString("no"), ButtonBar.ButtonData.NO);
            alert.getButtonTypes().setAll(yesButton, noButton);
            alert.showAndWait().ifPresent(type -> {
                if (type == yesButton) {
                    mapTemplateService.deleteMap(map._id()).observeOn(Constants.FX_SCHEDULER).subscribe();
                }
            });
        });

        //all elements in HBox
        HBox hBox;
        if (map.createdBy().equals(loginResultStorage.getLoginResult()._id())) {
            //add delete and edit icon for own maps
            nameLabel.setPrefWidth(450);
            nameLabel.setPickOnBounds(true);
            nameLabel.getStyleClass().add("nameHoverStyle");
            // rename map
            nameLabel.setOnMouseClicked(click -> {
                blurBackground.setEffect(new BoxBlur(3, 3, 3));
                renameMapsPane.setMouseTransparent(false);
                renameController.show(map);
                blurBackground.setMouseTransparent(true);
            });
            nameLabel.setPadding(new Insets(0, 40, 0, 40));
            vBox.setPadding(new Insets(0, 50, 0, 50));
            hBox = new HBox(30, mapImage, nameLabel, thumbUpImage, thumbDownImage, vBox, editImage, deleteImage);
            ownMapsList.getItems().add(hBox);
        } else {
            hBox = new HBox(30, mapImage, nameLabel, thumbUpImage, thumbDownImage, vBox);
            publicMapsList.getItems().add(hBox);
        }

        hBox.setPadding(new Insets(10));
        hBox.setAlignment(Pos.CENTER_LEFT);

        mapToHBox.put(map._id(), hBox);
    }

    @NotNull
    private ImageView getImageView(Image image, int valueX, int valueY) {
        final ImageView imageView = new ImageView(image);
        imageView.setFitWidth(valueX);
        imageView.setFitHeight(valueY);
        imageView.setSmooth(false);
        return imageView;
    }

    private void setVisible(boolean value, boolean value1, Color black, Color gray) {
        publicMapsList.setVisible(value);
        ownMapsList.setVisible(value1);
        ownLabel.setTextFill(black);
        publicLabel.setTextFill(gray);
    }

    public void showOwnMaps(MouseEvent mouseEvent) {
        setVisible(false, true, Color.BLACK, Color.GRAY);
        mouseEvent.consume();
    }

    public void showPublicMaps(MouseEvent mouseEvent) {
        setVisible(true, false, Color.GRAY, Color.BLACK);
        renameController.close(null);
        mouseEvent.consume();
    }

    public void hideListPane(MouseEvent mouseEvent) {
        votesPane.setVisible(false);
        votesPane.setMouseTransparent(true);
        imagePane.setVisible(false);
        imagePane.setMouseTransparent(true);
        renameMapsPane.setVisible(false);
        renameMapsPane.setVisible(true);
        renameController.hide();
        blurBackground.setEffect(null);
        mouseEvent.consume();
    }

    public void exitMapMenu(ActionEvent mouseEvent) {
        app.show(lobbySelectControllerProvider.get());
        mouseEvent.consume();
    }
}
