package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;


public class LobbyController implements Controller {

    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private final ObservableList<Vote> votes = FXCollections.observableArrayList();

    private final Map<String, HBox> membersToUI = new HashMap<>();

    private final UserService userService;
    private final GameMemberService gameMemberService;
    private final App app;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    private final Provider<ChatController> chatControllerProvider;

    private final EventListener eventListener;
    private final GameService gameService;
    private final ResourceBundle resourceBundle;
    private final MusicService musicService;

    private final LoginResultStorage loginResultStorage;
    private final Provider<PioneersController> pioneersController;
    private final AuthenticationService authenticationService;
    private final MapVoteService mapVoteService;
    private final MapTemplateService mapTemplateService;
    private PrefService prefService;
    private final Map<String, User> userIdToName = new HashMap<>();
    private MapTemplate selectedMap;
    private boolean voteSetup = true;
    @FXML
    public Button button_maps;
    @FXML
    public Button goodButton;
    @FXML
    public Button badButton;
    @FXML
    public Button exit_button;
    @FXML
    public Button ready_button;
    @FXML
    public Button begin_button;
    @FXML
    public Button spectator_button;
    @FXML
    public ListView<HBox> playerReady;
    @FXML
    public VBox chat;
    @FXML
    public VBox parentVbox;
    @FXML
    public Button color_button_lobby;
    @FXML
    public AnchorPane root;
    @FXML
    public Pane rootEmojiMenu;
    @FXML
    private ScrollPane searchScrollPane;
    @FXML
    private FlowPane searchFlowPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Image> boxTone;
    private Boolean spectatorBoolean = false;

    private Disposable memberDisposable;
    private Disposable gameDisposable;
    @FXML
    private ChatController chatController;
    @FXML
    private MusicController musicMenuController;

    boolean ignoreVotesWebsocket = false;

    @Inject
    public LobbyController(UserService userService,
                           GameMemberService gameMemberService,
                           App app,
                           Provider<LobbySelectController> lobbySelectControllerProvider,
                           Provider<ChatController> chatControllerProvider,
                           EventListener eventListener,
                           GameService gameService,
                           LoginResultStorage loginResultStorage,
                           Provider<PioneersController> pioneersController,
                           ResourceBundle resourceBundle,
                           MusicService musicService, AuthenticationService authenticationService,
                           MapVoteService mapVoteService,
                           MapTemplateService mapTemplateService,
                           PrefService prefService) {
        this.userService = userService;
        this.gameMemberService = gameMemberService;
        this.app = app;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
        this.chatControllerProvider = chatControllerProvider;
        this.eventListener = eventListener;
        this.gameService = gameService;
        this.loginResultStorage = loginResultStorage;
        this.pioneersController = pioneersController;
        this.resourceBundle = resourceBundle;
        this.musicService = musicService;
        this.authenticationService = authenticationService;
        this.mapVoteService = mapVoteService;
        this.mapTemplateService = mapTemplateService;
        this.prefService = prefService;
    }

    @Override
    public void init() {
        gameMemberService.findAllGamesMembers().observeOn(Constants.FX_SCHEDULER).subscribe(this.members::setAll);
        mapVoteService.getAllVotesFromUser(userService.getUserId()).observeOn(Constants.FX_SCHEDULER).subscribe(this.votes::setAll);

        // Update members List with WebSocket
        memberDisposable = eventListener
                .listen("games." + gameService.getStoredGame()._id() + ".members.*.*", Member.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(event -> {
                    Member member = event.data();
                    if (event.event().endsWith(".created")) {
                        if (!members.contains(member)) {
                            members.add(member);
                        }
                    } else if (event.event().endsWith(".deleted")) {
                        // Remove user from chat hashmap
                        userIdToName.remove(member.userId());
                        HBox element_to_remove = membersToUI.get(member.userId());
                        Label memberLabel = (Label) element_to_remove.getChildren().get(1);

                        // If not null user was the old owner of the game
                        if (memberLabel.getGraphic() != null) {
                            HBox newGameOwner = membersToUI.get(gameService.getStoredGame().owner());
                            Label ownerLabel = (Label) newGameOwner.getChildren().get(1);
                            ownerLabel.setGraphic(getImageOwner());
                        }
                        if ((loginResultStorage.getLoginResult()._id().equals(gameService.getStoredGame().owner()))) {
                            begin_button.setDisable(false);
                        }
                        // Remove all elements from HBox
                        element_to_remove.getChildren().clear();
                        // Remove HBox from list view
                        playerReady.getItems().remove(element_to_remove);
                        // Remove user from members list
                        members.removeIf(u -> u.userId().equals(member.userId()));
                        membersToUI.remove(member.userId());
                    } else if (event.event().endsWith(".updated")) {
                        for (int i = 0; i < members.size(); i++) {
                            if (members.get(i).userId().equals(member.userId())) {
                                //Update member with new information
                                members.set(i, member);
                                HBox element = membersToUI.get(member.userId());
                                ((CheckBox) element.getChildren().get(4)).setSelected(member.ready());
                                if (member.userId().equals(loginResultStorage.getLoginResult()._id())) {
                                    if (member.ready()) {
                                        ready_button.setText(resourceBundle.getString("unready"));
                                    }
                                    if (!member.ready()) {
                                        ready_button.setText(resourceBundle.getString("ready"));
                                    }
                                    //Update if spectator
                                    if (member.spectator()) {
                                        ready_button.setDisable(true);
                                        color_button_lobby.setDisable(true);
                                        element.getChildren().get(4).setMouseTransparent(true);
                                    } else {
                                        ready_button.setDisable(false);
                                        color_button_lobby.setDisable(false);
                                        element.getChildren().get(4).setMouseTransparent(false);
                                    }
                                }
                                if (!members.get(i).spectator()) {
                                    if (member.color() != null) {
                                        ((Rectangle) element.getChildren().get(5)).setFill(Color.valueOf(member.color()));
                                    } else {
                                        ((Rectangle) element.getChildren().get(5)).setFill(Color.TRANSPARENT);
                                    }
                                    element.getChildren().get(4).getStyleClass().removeAll("checkEye");
                                } else {
                                    element.getChildren().get(4).getStyleClass().add("checkEye");
                                    ((Rectangle) element.getChildren().get(5)).setFill(Color.TRANSPARENT);
                                }
                            }
                        }
                    }
                });

        gameDisposable = eventListener.listen(Constants.GAMES + "." + gameService.getStoredGame()._id() + ".*", Game.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(event -> {
                    if (event.event().endsWith(".updated")) {
                        Game game = event.data();
                        if (game.started()) {
                            prefService.setCurrentGame(game._id());
                            app.show(pioneersController.get());
                            restoreOldVote();
                        }
                        //map was changed to a non default map
                        if (!Objects.equals(game.settings().mapTemplate(), gameService.getStoredGame().settings().mapTemplate())) {
                            restoreOldVote();
                            enableVote(false);
                            if (!userService.getUserId().equals(gameService.getStoredGame().owner()) && game.settings().mapTemplate() != null) {
                                enableVote(true);
                            }
                            for (HBox hBox : membersToUI.values()) {
                                ImageView imageView = (ImageView) (hBox.getChildren().get(6));
                                imageView.setImage(null);
                            }
                            if (game.settings().mapTemplate() != null) {
                                mapTemplateService.getMap(game.settings().mapTemplate()).observeOn(Constants.FX_SCHEDULER).subscribe(this::setMyMap);
                            } else {
                                //remove tooltip
                                HBox hbox = membersToUI.get(game.owner());
                                ImageView imageview_map = (ImageView) hbox.getChildren().get(6);
                                Tooltip tooltip = (Tooltip) imageview_map.getProperties().get("tooltip");
                                if (tooltip != null) {
                                    Tooltip.uninstall(imageview_map, tooltip);
                                }

                                enableVote(false);
                                setMyMap(new MapTemplate("", "", "", "", "views/images/map assets/hexGrid.png",
                                        "The Default map of Pioneers.", "", 0, null, null));
                            }
                        }
                        //owner changed
                        if (!game.owner().equals(gameService.getStoredGame().owner())) {
                            if (game.settings().mapTemplate() != null) {
                                mapTemplateService.getMap(game.settings().mapTemplate()).observeOn(Constants.FX_SCHEDULER).subscribe(result -> setMyMap(result, game.owner()));
                            }
                            enableVote(!game.owner().equals(loginResultStorage.getLoginResult()._id()));
                            setMapsButtonMode(game.owner().equals(loginResultStorage.getLoginResult()._id()));
                        }
                        gameService.setStoredGame(game);
                    }
                });

        eventListener.listen("maps.*.votes.*.*", Vote.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(event -> {
                    if(!ignoreVotesWebsocket){
                        Vote vote = event.data();
                        String userId = vote.userId();
                        if (membersToUI.containsKey(userId)) {
                            if (event.event().endsWith(".created") || event.event().endsWith(".updated")) {
                                //for voting members
                                ImageView imageview_map = (ImageView) membersToUI.get(userId).getChildren().get(6);
                                if (vote.score().equals(1)) {
                                    imageview_map.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/thumb_up.png"))));
                                } else {
                                    imageview_map.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/thumb_down.png"))));
                                }
                            }
                        }
                    }
                    ignoreVotesWebsocket = false;
                });
    }

    private void enableVote(boolean b) {
        goodButton.setDisable(!b);
        badButton.setDisable(!b);
    }

    private void restoreOldVote() {
        Vote oldVote = null;
        for (Vote vote : votes) {
            if (vote.mapId().equals(gameService.getStoredGame().settings().mapTemplate())) {
                oldVote = vote;
                break;
            }
        }
        if (oldVote != null) {
            ignoreVotesWebsocket = true;
            mapVoteService.updateMyVote(oldVote.mapId(), oldVote.score()).observeOn(Constants.FX_SCHEDULER).subscribe();
        } else {
            mapVoteService.deleteMyVote(gameService.getStoredGame().settings().mapTemplate()).observeOn(Constants.FX_SCHEDULER).subscribe();
        }
    }

    @Override
    public void destroy() {
        if (this.memberDisposable != null) {
            memberDisposable.dispose();
        }
        if (gameDisposable != null) {
            gameDisposable.dispose();
        }
        if (this.chatController != null) {
            this.chatController.destroy();
            this.chatController = null;
        }
        if (this.musicMenuController != null) {
            this.musicMenuController.destroy();
            this.musicMenuController = null;
        }
    }

    @Override
    public Parent render() {
        // load LobbySelect screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/LobbyScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> {
            if (c == LobbyController.class) {
                return this;
            }
            if (c == ChatController.class) {
                return chatControllerProvider.get();
            }
            if (c == MusicController.class) {
                return new MusicController(musicService, resourceBundle);
            }
            return null;
        });
        final Parent parent;
        try {
            parent = loader.load();
            members.addListener((ListChangeListener<? super Member>) c -> {
                //Make new UI elements for new members
                while (c.next()) {
                    if (c.wasAdded() && !c.wasReplaced()) {
                        c.getAddedSubList().forEach(this::renderItem);
                    }
                }
                String map = gameService.getStoredGame().settings().mapTemplate();
                if (map != null && voteSetup) {
                    enableVote(true);
                    mapTemplateService.getMap(map).observeOn(Constants.FX_SCHEDULER).subscribe(this::setMyMap);
                    mapVoteService.getVotes(map).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                        if (result.get(0).updatedAt() != null) {
                            for (Vote vote : result) {
                                if (membersToUI.containsKey(vote.userId()) && vote.updatedAt().compareTo(gameService.getStoredGame().createdAt()) >= 0) {
                                    ImageView imageview_map = (ImageView) membersToUI.get(vote.userId()).getChildren().get(6);
                                    if (vote.score().equals(1)) {
                                        imageview_map.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/check.png"))));
                                    } else {
                                        imageview_map.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/close.png"))));
                                    }
                                }
                            }
                        }
                    });
                }
                voteSetup = false;
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        begin_button.setDisable(!loginResultStorage.getLoginResult()._id().equals(gameService.getStoredGame().owner()));
        setMapsButtonMode(loginResultStorage.getLoginResult()._id().equals(gameService.getStoredGame().owner()));

        app.setScreenTitle(resourceBundle.getString(Constants.LOBBY_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());

        chatController.setParent(parent);
        chatController.setEmojiMenu(rootEmojiMenu, searchScrollPane, searchFlowPane, tabPane, txtSearch, boxTone);
        chatController.init();
        chatController.render();

        musicMenuController.setParent(parent);
        musicMenuController.init();
        musicMenuController.render();
        musicMenuController.hideMenu();

        this.openLobbyChat(userIdToName);

        enableVote(false);

        return parent;
    }

    private void openLobbyChat(Map<String, User> userIdToName) {
        this.chatController.createLobbyController(gameService.getStoredGame()._id(), userIdToName);
    }

    private void setMapsButtonMode(boolean mapsButtonStatus){
        button_maps.setDisable(!mapsButtonStatus);
        button_maps.setVisible(mapsButtonStatus);
        button_maps.setMouseTransparent(!mapsButtonStatus);
        button_maps.setManaged(mapsButtonStatus);

        goodButton.setDisable(mapsButtonStatus);
        goodButton.setVisible(!mapsButtonStatus);
        goodButton.setMouseTransparent(mapsButtonStatus);
        goodButton.setManaged((!mapsButtonStatus));

        badButton.setDisable(mapsButtonStatus);
        badButton.setVisible(!mapsButtonStatus);
        badButton.setMouseTransparent(mapsButtonStatus);
        badButton.setManaged((!mapsButtonStatus));
    }

    private void renderItem(Member member) {

        userService.getUser(member.userId()).observeOn(Constants.FX_SCHEDULER).subscribe(c -> {
                    final double MAX_FONT_SIZE = 25.0;

                    // Add new user to chat hashmap
                    userIdToName.put(c._id(), c);

                    final Label labelName = new Label();
                    labelName.setText(c.name());
                    labelName.setMinSize(50, 50);
                    labelName.setPrefWidth(100);
                    labelName.setFont(new Font(MAX_FONT_SIZE));
                    //crown
                    final Label ownerLabel = new Label();
                    if (member.userId().equals(gameService.getStoredGame().owner())) {
                        ImageView imageOwner = getImageOwner();
                        ownerLabel.setGraphic(imageOwner);
                        ownerLabel.setPrefWidth(64);
                    } else {
                        ownerLabel.setMinSize(50, 50);
                        ownerLabel.setPrefWidth(64);
                    }

                    Rectangle box = new Rectangle(50, 50);
                    box.setFill(Color.TRANSPARENT);

                    //checkbox
                    final CheckBox checkBox = new CheckBox(resourceBundle.getString("ready"));
                    //checkBox.setMinSize(50, 50);
                    checkBox.setPrefWidth(150);
                    checkBox.setFont(new Font(MAX_FONT_SIZE));
                    checkBox.setSelected(member.ready());
                    if (member.spectator()) {
                        checkBox.getStyleClass().add("checkEye");
                    }
                    if (member.userId().equals(loginResultStorage.getLoginResult()._id())) {
                        checkBox.setOnMouseClicked(click -> {
                            if (!spectatorBoolean) {
                                checkBox.setSelected(false);
                            }

                            gameMemberService.getGameMember().observeOn(Constants.FX_SCHEDULER)
                                    .subscribe(res -> {
                                        if (res.color() != null) {
                                            if (click.getClickCount() == 1 && !res.ready()) {
                                                readyChange(true, member);
                                                checkBox.setSelected(true);
                                            }
                                            if (click.getClickCount() == 1 && res.ready()) {
                                                readyChange(false, member);
                                                checkBox.setSelected(false);
                                            }
                                        } else {
                                            showAlert(resourceBundle.getString("error"), resourceBundle.getString(Constants.NO_COLOR_ERROR));
                                        }
                                    });
                        });
                    } else {
                        checkBox.setDisable(true);
                    }

                    String img = c.avatar();
                    if (img == null) {
                        img = Constants.AVATAR_LIST.get(0);
                    }
                    final ImageView imageView = new ImageView(new Image(img));
                    imageView.setFitWidth(64);
                    imageView.setFitHeight(64);

                    final Rectangle color = new Rectangle(64, 64);
                    if (member.color() == null || member.spectator()) {
                        color.setFill(Color.TRANSPARENT);
                    } else {
                        color.setFill(Color.valueOf(member.color()));
                    }

                    final ImageView mapVote = new ImageView();
                    mapVote.setFitWidth(64);
                    mapVote.setFitHeight(64);

                    HBox hBox = new HBox(30, imageView, ownerLabel, labelName, box, checkBox, color, mapVote);
                    hBox.setPadding(new Insets(10));
                    HBox.setHgrow(labelName, Priority.ALWAYS);
                    HBox.setHgrow(checkBox, Priority.ALWAYS);
                    labelName.setPrefWidth(300);
                    checkBox.setPrefWidth(220);

                    playerReady.getItems().add(hBox);
                    //add new UI element to map, with the key being the member id and new UI element being the ID.
                    membersToUI.put(member.userId(), hBox);
                }
        );
    }


    // Get the image for game owner
    @NotNull
    private ImageView getImageOwner() {
        Image image = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/crown.png")));
        ImageView imageOwner = new ImageView(image);
        imageOwner.setFitWidth(50);
        imageOwner.setFitHeight(50);
        return imageOwner;
    }

    public void exit(ActionEvent actionEvent) {
        //exit button is clicked
        if (members.size() <= 1) {

            if (actionEvent == null) {
                gameService.deleteGame(gameService.getStoredGame()._id()).observeOn(Constants.FX_SCHEDULER).subscribe();
                userService.setUserOffline().observeOn(Constants.FX_SCHEDULER).subscribe();
                authenticationService.logout().observeOn(Constants.FX_SCHEDULER).subscribe();
                return;
            }
            //delete Lobby
            gameService.deleteGame(gameService.getStoredGame()._id()).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                if (!result.createdAt().equals(Constants.LOBBY_DELETE_ERROR)) {
                    //In case of no error, change to lobby Select.
                    changeToLobbySelect();
                }
            });
        } else {
            // Get the owner id
            Game game = gameService.getStoredGame();
            if (game.createdAt().equals(Constants.JOIN_LOBBY_ERROR)) {
                // Show alert
                showAlert(game.createdAt(), game._id());
            } else {
                //check if user equals Owner
                if (userService.getUserId().equals(game.owner())) {
                    // Get next owner
                    String new_owner;
                    if (members.get(0).userId().equals(game.owner())) {
                        new_owner = members.get(1).userId();
                    } else {
                        new_owner = members.get(0).userId();
                    }
                    if (actionEvent == null) {
                        gameService.updateGame(gameService.getStoredGame().name(), new_owner, gameService.getStoredGame().started(), null, null).observeOn(Constants.FX_SCHEDULER).subscribe();
                        gameMemberService.exitLobby().observeOn(Constants.FX_SCHEDULER).subscribe();
                        userService.setUserOffline().observeOn(Constants.FX_SCHEDULER).subscribe();
                        authenticationService.logout().observeOn(Constants.FX_SCHEDULER).subscribe();
                        return;
                    }
                    //set new Owner
                    gameService.updateGame(gameService.getStoredGame().name(), new_owner, gameService.getStoredGame().started()).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                        if (result.equals(Constants.UPDATE_GAME_SUCCESS)) {
                            exitLobby();
                        } else {
                            // Show alert
                            showAlert(resourceBundle.getString(Constants.UPDATE_GAME_ERROR), result);
                        }
                    });
                } else {
                    // Exit lobby if you are not the owner
                    exitLobby();
                }
            }
        }
    }

    private void changeToLobbySelect() {
        // Take your private chats to next screen
        this.chatController.setPrivateChatToNextScreen();
        // Change to  lobby select screen
        final LobbySelectController controller = lobbySelectControllerProvider.get();
        app.show(controller);
    }

    private void showAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.ERROR, context);
        alert.initOwner(app.getPrimaryStage());
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private void exitLobby() {
        // Exit lobby
        gameMemberService.exitLobby().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            if (result.createdAt().equals(Constants.LOBBY_EXIT_ERROR)) {
                showAlert(resourceBundle.getString(Constants.LOBBY_EXIT_ERROR), result.updatedAt());
            } else {
                // Change to lobby select screen
                changeToLobbySelect();
                restoreOldVote();
            }
        });
    }

    public void readyPlay() {
        // set status to ready
        gameMemberService.getGameMember().observeOn(Constants.FX_SCHEDULER)
                .subscribe(res -> {
                    if (res.color() != null) {
                        if (!res.ready()) {
                            readyChange(true, res);
                        }
                        if (res.ready()) {
                            readyChange(false, res);
                        }
                    } else {
                        showAlert(resourceBundle.getString("error"), resourceBundle.getString(Constants.NO_COLOR_ERROR));
                    }
                });
    }

    private void readyChange(boolean ready, Member res) {
        gameMemberService.updateMemberReady(ready, res.color())
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(res0 -> {
                    //ready_button.setDisable(true);
                    if (res0.contains(Constants.CHANGE_MEMBER_SHIP_ERROR)) {
                        // something went wrong
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setHeaderText("Error");
                        errorAlert.setContentText(res0);
                        errorAlert.showAndWait();
                    }
                });
    }

    public void beginPlay(ActionEvent actionEvent) {
        //check if user is the host
        Game game = gameService.getStoredGame();
        if (userService.getUserId().equals(game.owner())) {
            //check if the all players ready or not
            int size = playerReady.getItems().size();
            for (int i = 0; i <= size - 1; i++) {
                HBox hBox = playerReady.getItems().get(i);
                CheckBox checkBox = (CheckBox) hBox.getChildren().get(4);
                if (!checkBox.isSelected()) {
                    //when not all players ready
                    showAlert(resourceBundle.getString("error"), resourceBundle.getString("not.all.ready"));
                    return;
                }
            }
            //set game to started, which triggers the listener to switch screens
            gameService.updateGame(gameService.getStoredGame().name(), gameService.getStoredGame().owner(), true)
                    .observeOn(Constants.FX_SCHEDULER)
                    .subscribe();
        } else {
            //when user is not the host
            showAlert(resourceBundle.getString("error"), resourceBundle.getString("not.host"));
        }
        actionEvent.consume();
    }

    public void colorChoose(ActionEvent actionEvent) {
        ColorController colorController = new ColorController(this.gameMemberService, this.resourceBundle, app);
        colorController.setMembersList(members);
        colorController.setFxml("views/colorChooseDialog.fxml");
        colorController.setBlurBox(root);
        colorController.render();
        actionEvent.consume();
    }

    public void spectatorMode(ActionEvent actionEvent) {
        if (spectatorBoolean) {
            gameMemberService.updateMemberSpectator(false).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                if (!res.equals(Constants.CUSTOM_ERROR)) {
                    spectatorBoolean = !spectatorBoolean;
                }
            });
        } else {
            gameMemberService.updateMemberSpectator(true).observeOn(Constants.FX_SCHEDULER).subscribe(res -> {
                if (!res.equals(Constants.CUSTOM_ERROR)) {
                    spectatorBoolean = !spectatorBoolean;
                }
            });
        }
        actionEvent.consume();
    }

    public void openMusicMenu(MouseEvent mouseEvent) {
        musicMenuController.toggleMenu();
        mouseEvent.consume();
    }

    public void chooseMap(ActionEvent actionEvent) {
        MapChooseController mapChooseController = new MapChooseController(app, resourceBundle, mapTemplateService, userService, gameService);
        mapChooseController.setBlurBox(root);
        mapChooseController.init();
        mapChooseController.render();
        actionEvent.consume();
    }

    public void setMyMap(MapTemplate map) {
        setMyMap(map, gameService.getStoredGame().owner());
    }
    public void setMyMap(MapTemplate map,  String owner) {
        HBox hbox = membersToUI.get(owner);
        ImageView imageview_map = (ImageView) hbox.getChildren().get(6);
        String image = map.icon();
        if (image != null) {
            if (image.startsWith("data:;")) {
                image = map.icon().substring(0, 5) + "image/png;" + map.icon().substring(6);
            }
        }
        boolean isDefaultMap = image != null && image.startsWith("views/");
        Image mapImage = image != null ? !isDefaultMap ? new Image(image) : new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image))) :
                new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/no_map_icon.png")));
        imageview_map.setImage(mapImage);
        imageview_map.setPickOnBounds(true);
        //tooltip to show icon in original size as well as the description if available
        mapTemplateService.addMapTooltip(imageview_map, map);
        //adjust image size
        double width = imageview_map.getImage().getWidth();
        double height = imageview_map.getImage().getHeight();
        imageview_map.setFitHeight(height / width * 64);
        imageview_map.setFitWidth(64);
        imageview_map.setTranslateX(9);
        selectedMap = map;
    }

    public void good(ActionEvent event) {
        vote(1);
        event.consume();
    }

    public void bad(ActionEvent event) {
        vote(-1);
        event.consume();
    }

    private void vote(Number score) {
        String mapId = selectedMap._id();
        mapVoteService.updateMyVote(mapId, score).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            //patch failed
            if (result.createdAt().equals("Not Found")) {
                mapVoteService.sendVote(mapId, score).observeOn(Constants.FX_SCHEDULER).subscribe();
            }
        });
    }    
    
    public PioneersController getPioneersController() {
        return pioneersController.get();
    }
}
