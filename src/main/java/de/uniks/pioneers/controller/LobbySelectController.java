package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Game;
import de.uniks.pioneers.model.GameSettings;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Duration;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class LobbySelectController implements Controller {

    private final ObservableList<Game> games = FXCollections.observableArrayList();
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final GameService gameService;
    private final UserService userService;
    private final App app;
    private final AuthenticationService authenticationService;
    private final Provider<ChatController> chatControllerProvider;
    private final ResourceBundle resourceBundle;
    private final LoginResultStorage loginResultStorage;
    private final PrefService prefService;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    private final TimerService timerService;
    private final MusicService musicService;
    private final GameMemberService gameMemberService;
    private final Provider<LoginController> loginController;
    private final Provider<LobbyController> lobbyController;
    private final Provider<RulesController> rulesControllerProvider;

    private final Provider<MapMenuController> mapMenuControllerProvider;
    private final PioneersService pioneersService;
    private final EventListener eventListener;
    private final Map<String, String> userIdToName = new HashMap<>();
    @FXML
    public TableView<Game> lobby_list;
    @FXML
    public TableColumn<Game, String> lobbyName_list;
    @FXML
    public TableColumn<Game, String> lobbyCreator_list;
    @FXML
    public Label error_Label;
    @FXML
    public Button logOut_button;
    @FXML
    public Button createLobby_button;
    @FXML
    public Button joinLobby_button;
    @FXML
    public TextField lobbyName_field;
    @FXML
    public PasswordField lobbyPassword_field;
    @FXML
    public Button create_button;
    @FXML
    public Button cancel_button;  //cancel Button in createLobbyDialog
    // all the Buttons in lobbyPasswordDialog.fxml
    @FXML
    public TextField LobbyPasswordInput;
    @FXML
    public Label LobbyJoin_Password_Incorrect;
    @FXML
    public Button joinLobbyPassword_button;
    @FXML
    public Button joinLobbyCancel_button;
    @FXML
    public Label gameNameLabel;
    @FXML
    public VBox chat;
    @FXML
    public ImageView imageview_settings;
    @FXML
    public AnchorPane root;
    @FXML
    public TextField map_size_field;
    @FXML
    public TextField victory_points_field;
    @FXML
    public Slider map_size_slider;
    @FXML
    public Slider victory_point_slider;
    @FXML
    public TextField start_resources;
    @FXML
    public CheckBox roll7;
    @FXML
    public Pane rootEmojiMenu;
    @FXML
    public Label loading_label;
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
    private Disposable userDisposable;
    private Disposable gameDisposable;
    @FXML
    private MusicController musicMenuController;
    @FXML
    private ChatController chatController;

    @Inject
    public LobbySelectController(App app, AuthenticationService authenticationService, GameService gameService, GameMemberService gameMemberService, UserService userService,
                                 Provider<LoginController> loginController, Provider<LobbyController> lobbyController, EventListener eventListener, Provider<ChatController> chatControllerProvider, ResourceBundle resourceBundle,
                                 LoginResultStorage loginResultStorage, PrefService prefService, Provider<LobbySelectController> lobbySelectControllerProvider, TimerService timerService, MusicService musicService, Provider<RulesController> rulesControllerProvider,
                                 Provider<MapEditorController> mapEditorController, Provider<MapMenuController> mapMenuControllerProvider, PioneersService pioneersService
    ) {
        this.app = app;
        this.gameMemberService = gameMemberService;
        this.loginController = loginController;
        this.gameService = gameService;
        this.userService = userService;
        this.lobbyController = lobbyController;
        this.eventListener = eventListener;
        this.authenticationService = authenticationService;
        this.chatControllerProvider = chatControllerProvider;
        this.resourceBundle = resourceBundle;
        this.loginResultStorage = loginResultStorage;
        this.prefService = prefService;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
        this.timerService = timerService;
        this.musicService = musicService;
        this.rulesControllerProvider = rulesControllerProvider;
        this.mapMenuControllerProvider = mapMenuControllerProvider;
        this.pioneersService = pioneersService;
    }

    @Override
    public void init() {
        if (!timerService.isRunning()) {
            timerService.startTimer();
        }

        userService.findAllOnlineUsers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            result.forEach(user -> userIdToName.put(user._id(), user.name()));
            this.users.setAll(result);
            gameService.findAllGames().observeOn(Constants.FX_SCHEDULER).subscribe(this.games::setAll);
        });

        userDisposable = eventListener.listen("users.*.*", User.class).observeOn(Constants.FX_SCHEDULER).subscribe(event -> {
            final User user = event.data();
            if (event.event().endsWith(".deleted")) {
                userIdToName.remove(user._id());
                users.removeIf(u -> u._id().equals(user._id()));
            } else if (event.event().endsWith(".updated")) {
                if (user.status().equals(Constants.STATUS_ONLINE)) {
                    userIdToName.put(user._id(), user.name());
                } else {
                    userIdToName.remove(user._id());
                }
                users.replaceAll(u -> u._id().equals(user._id()) ? user : u);
            }
        });

        // Update Games List with WebSocket
        gameDisposable = eventListener.listen("games.*.*", Game.class).observeOn(Constants.FX_SCHEDULER).subscribe(event -> {
            final Game game = event.data();
            if (event.event().endsWith(".created")) {
                games.add(game);
            } else if (event.event().endsWith(".deleted")) {
                games.removeIf(u -> u._id().equals(game._id()));
            } else if (event.event().endsWith(".updated")) {
                //Remember that game in this context is the updated game, while the one in the list is the old one
                for (int i = 0; i < games.size(); i++) {
                    if (games.get(i)._id().equals(game._id())) {
                        //update game
                        games.set(i, game);
                    }
                }
            }
        });

        if (musicService.getMusicContext() == null || !musicService.getMusicContext().equals(Constants.MUSIC_CONTEXT.LOBBY)) {
            musicService.playMusic(Constants.MUSIC_CONTEXT.LOBBY);
        }
    }

    @Override
    public void destroy() {

        if (this.userDisposable != null) {
            this.userDisposable.dispose();
        }
        else{
            timerService.stopTimer();
        }
        if (this.gameDisposable != null) {
            this.gameDisposable.dispose();
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
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/lobbySelectScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> {
            if (c == LobbySelectController.class) {
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

        //load LobbySelect List
        try {
            parent = loader.load();
            lobbyName_list.setCellValueFactory(gameName -> new SimpleStringProperty(gameName.getValue().name()));
            lobbyCreator_list.setCellValueFactory(ownerId -> new SimpleStringProperty(userIdToName.get(ownerId.getValue().owner()) != null ? userIdToName.get(ownerId.getValue().owner()) : "Offline"));

            // Show only not started games
            games.addListener((ListChangeListener<? super Game>) c -> lobby_list.getItems().setAll(c.getList().stream().filter(game -> !game.started()
                    || game._id().equals(prefService.getCurrentGame())).toList()));
            joinLobby_button.setDisable(true);
            lobby_list.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Disable button if new value is null
                // Enable button if table view is selected
                joinLobby_button.setDisable(newValue == null);
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        lobby_list.setRowFactory(tableRow -> {
            TableRow<Game> gameTableRow = new TableRow<>();
            gameTableRow.itemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue._id().equals(prefService.getCurrentGame())) {
                    if (!gameTableRow.getStyleClass().toString().equals("currentGame")) {
                        gameTableRow.getStyleClass().add("currentGame");
                    }
                } else {
                    gameTableRow.getStyleClass().removeAll("currentGame");
                }
            });
            return gameTableRow;
        });

        imageview_settings.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            openSettings();
            event.consume();
        });

        imageview_settings.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
            RotateTransition rotateTransition = new RotateTransition(Duration.millis(800), imageview_settings);
            rotateTransition.setByAngle(180);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();
            event.consume();
        });

        app.setScreenTitle(resourceBundle.getString(Constants.LOBBY_SELECT_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());

        chatController.setParent(parent);
        chatController.setEmojiMenu(rootEmojiMenu, searchScrollPane, searchFlowPane, tabPane, txtSearch, boxTone);
        chatController.init();
        chatController.render();

        musicMenuController.setParent(parent);
        musicMenuController.init();
        musicMenuController.render();
        musicMenuController.hideMenu();
        return parent;
    }

    public void openSettings() {
        ProfileSettingsController profileSettingsController = new ProfileSettingsController(app, userService, loginResultStorage, prefService, new EncryptionService(), resourceBundle, loginController, lobbySelectControllerProvider);
        profileSettingsController.setFxml("views/profileSettings.fxml");
        profileSettingsController.setBlurBox(root);
        profileSettingsController.render();
    }

    public void createLobby() {
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/createLobbyDialog.fxml"), resourceBundle);
        fxmlLoader.setControllerFactory(c -> this);
        try {
            //load Dialog Window
            DialogPane loader = fxmlLoader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(loader);
            dialog.initOwner(app.getPrimaryStage());
            dialog.setTitle(resourceBundle.getString("create.lobby"));
            dialog.initOwner(app.getPrimaryStage());
            dialog.show();
            Window window = dialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(event -> {
                dialog.setResult(ButtonType.CLOSE);
                root.setEffect(null);
            });
            root.setEffect(new BoxBlur(3, 3, 3));

            create_button.setDefaultButton(true);
            cancel_button.setCancelButton(true);

            // Property Change Listener for sliders
            map_size_slider.valueProperty().addListener((arg0, oldValue, newValue) -> map_size_field.setText(String.valueOf(Math.round((double) newValue))));
            victory_point_slider.valueProperty().addListener((arg0, oldValue, newValue) -> victory_points_field.setText(String.valueOf(Math.round((double) newValue))));

            create_button.setOnAction(event -> {
                //create Lobby on Server
                int size = Integer.parseInt(map_size_field.getText());
                int points = Integer.parseInt(victory_points_field.getText());
                int resources = start_resources.getText().matches("-?\\d+") ? Integer.parseInt(start_resources.getText()) : 0;
                gameService.createdLobby(lobbyName_field.getText(), lobbyPassword_field.getText(), new GameSettings(size, points, null, roll7.isSelected(), resources))
                        .observeOn(Constants.FX_SCHEDULER)
                        .subscribe(result -> {
                            if (result.equals(Constants.LOBBY_CREATION_SUCCESS)) {
                                // Take your private chats to next screen
                                this.chatController.setPrivateChatToNextScreen();

                                final LobbyController controller = lobbyController.get();
                                app.show(controller);
                                dialog.setResult(ButtonType.CLOSE);
                            } else {
                                error_Label.setText((result));
                            }
                        });
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

    public void logOut(ActionEvent actionEvent) {
        if (actionEvent == null) {
            userService.setUserOffline().observeOn(Constants.FX_SCHEDULER).subscribe();
            authenticationService.logout().observeOn(Constants.FX_SCHEDULER).subscribe();
            return;
        }
        authenticationService.logout()
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(res -> {
                    // logout was not successful
                    if (res.contains(Constants.LOGOUT_ERROR)) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setHeaderText(resourceBundle.getString("error"));
                        errorAlert.setContentText(res);
                        errorAlert.showAndWait();
                    } else {
                        timerService.stopTimer();
                        musicService.stopMusic();
                        userService.setUserOffline()
                                .observeOn(Constants.FX_SCHEDULER)
                                .subscribe(result -> {

                                    // could not set user status to offline
                                    if (!result.equals(Constants.STATUS_OFFLINE)) {
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                                        errorAlert.setHeaderText(resourceBundle.getString("error"));
                                        errorAlert.setContentText(Constants.CUSTOM_ERROR);
                                        errorAlert.showAndWait();
                                    } else {
                                        // User status set to offline, go to LoginScreen
                                        this.chatController.clearPrivateChatAfterLogout();
                                        final LoginController controller = loginController.get();
                                        app.show(controller);
                                    }
                                });
                    }
                });
    }

    public void joinLobby() {
        // Store selected game
        this.gameService.setStoredGame(this.lobby_list.getSelectionModel().getSelectedItem());
        // If selected game is in pref service rejoin the game
        if (this.lobby_list.getSelectionModel().getSelectedItem()._id().equals(prefService.getCurrentGame())) {
            loading_label.setVisible(true);
            pioneersService.setPlayerInactive(true).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                PioneersController pioneersController = lobbyController.get().getPioneersController();
                pioneersController.setRejoin(true);
                app.show(pioneersController);
            });
        } else {
            //load Dialog Window
            final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("views/lobbyPasswordDialog.fxml"), resourceBundle);
            fxmlLoader.setControllerFactory(c -> this);
            try {
                DialogPane loader = fxmlLoader.load();
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader);
                dialog.initOwner(app.getPrimaryStage());
                dialog.setTitle(resourceBundle.getString("join.lobby"));
                dialog.show();
                Window window = dialog.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> {
                    dialog.setResult(ButtonType.CLOSE);
                    root.setEffect(null);
                });
                root.setEffect(new BoxBlur(3, 3, 3));
                gameNameLabel.setText(gameService.getStoredGame().name());
                joinLobbyPassword_button.setDefaultButton(true);
                joinLobbyCancel_button.setCancelButton(true);
                joinLobbyPassword_button.setOnAction(event -> {
                    //join Lobby on Server
                    gameMemberService.joinLobby(gameService.getStoredGame()._id(), LobbyPasswordInput.getText())
                            .observeOn(Constants.FX_SCHEDULER)
                            .subscribe(result -> {
                                // login was not successful
                                if (result.equals(Constants.JOIN_LOBBY_SUCCESS)) {
                                    // Take your private chats to next screen
                                    this.chatController.setPrivateChatToNextScreen();

                                    // join Lobby was successful
                                    app.show(lobbyController.get());
                                    dialog.setResult(ButtonType.CLOSE);
                                } else {
                                    // show error on label
                                    LobbyJoin_Password_Incorrect.setText(result);
                                }
                            });
                });

                joinLobbyCancel_button.setOnAction(event -> {
                    dialog.setResult(ButtonType.CLOSE);
                    root.setEffect(null);
                });
                dialog.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void openMusicMenu(MouseEvent mouseEvent) {
        musicMenuController.toggleMenu();
        mouseEvent.consume();
    }

    public void changeToRules(MouseEvent mouseEvent) {
        app.show(rulesControllerProvider.get());
        mouseEvent.consume();
    }

    public void switchToMapMenu(ActionEvent actionEvent) {
        app.show(mapMenuControllerProvider.get());
        actionEvent.consume();
    }
}
