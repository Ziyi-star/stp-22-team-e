package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Map;
import java.util.*;

public class PioneersController implements Controller {
    final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final List<PlayerListViewCell> playerListViewCells = new ArrayList<>();
    private final PioneersService pioneersService;
    private final ArrayList<Controller> mechanicSubControllers = new ArrayList<>();
    private final PioneersUIService pioneersUIService;
    private final EventListener eventListener;
    private final LoginResultStorage loginResultStorage;
    private final ResourceBundle resourceBundle;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    private final Provider<ChatController> chatControllerProvider;
    private final MusicService musicService;
    private final AnimationService animationService;
    private final Provider<VictoryController> victoryControllerProvider;
    private final App app;
    private final Map<String, Player> userIdToPlayer = new HashMap<>();
    private final Map<String, User> userIdToUserInfo = new HashMap<>();
    private final Map<String, String> colorToUserID = new HashMap<>();
    private final MapService mapService;
    private final UserService userService;
    private final GameMemberService memberService;
    private final ArrayList<String> myHarbors = new ArrayList<>();
    private final ObservableList<Member> spectators = FXCollections.observableArrayList();
    private final PrefService prefService;
    private final StringToKeyCodeService stringToKeyCodeService;
    private final AuthenticationService authenticationService;
    @FXML
    public Pane inGamePane;
    @FXML
    public AnchorPane inGameAnchorPane;
    @FXML
    public Pane player_spectator_list;
    @FXML
    public Pane chat_board;
    @FXML
    public ImageView image_dice;
    @FXML
    public ImageView image_dice1;
    @FXML
    public Button button_endTurn;
    @FXML
    public Button button_settlement;
    @FXML
    public Button button_city;
    @FXML
    public Button button_road;
    @FXML
    public Label label_lumber;
    @FXML
    public Label label_wool;
    @FXML
    public Label label_ore;
    @FXML
    public Label label_brick;
    @FXML
    public Label label_grain;
    @FXML
    public Label label_development;
    @FXML
    public Label label_turn;
    @FXML
    public Label label_knight_own;
    @FXML
    public ImageView imageview_settlement;
    @FXML
    public ImageView imageview_city;
    @FXML
    public ImageView imageview_settlement_roof;
    @FXML
    public ImageView imageview_city_roof;
    @FXML
    public ImageView imageview_road;
    @FXML
    public Button exit_button;
    @FXML
    public Label label_insufficient_res;
    @FXML
    public Label currentplayer_label;
    @FXML
    public Pane buildMenu;
    @FXML
    public Pane offer_menu;
    @FXML
    public Button button_trade;
    @FXML
    public Label victory_points;
    @FXML
    public ImageView currentplayer_image;
    @FXML
    public Label unknown_resource;
    public ListView<Player> players_listview;
    @FXML
    public HBox cost_settlement_pop;
    @FXML
    public HBox cost_city_pop;
    @FXML
    public HBox cost_road_pop;
    @FXML
    public Pane own_player;
    @FXML
    public ImageView eye;
    @FXML
    public ImageView spectator_image;
    @FXML
    public Label playerLabel;
    @FXML
    public ListView<Member> spectators_listview;
    @FXML
    public Label longest_road;
    @FXML
    public ImageView book;
    @FXML
    public Button button_development_card;
    @FXML
    public ImageView development_menu;
    @FXML
    public HBox cost_development_card;
    @FXML
    public Label own_development_cards;
    @FXML
    private Label loading_label;
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
    @FXML
    private ChatController chatController;
    @FXML
    private TradeAndDropSubController tradeMenuController;
    @FXML
    private LongestRoadController longestRoadController;
    @FXML
    private KeyboardShortcutController keyboardShortcutController;
    @FXML
    private RobSubController robMenuController;
    @FXML
    private TradeForOthersSubController offerMenuController;
    @FXML
    private MusicController musicMenuController;
    @FXML
    private TradingPartnerSubController tradingPartnerController;
    @FXML
    private PlentyMonopolySubController plentyMonopolyController;
    @FXML
    private MiniPopController miniPopController;
    @FXML
    private DevelopmentCardsSubController developmentCardsController;


    private PioneersMapController mapController;
    private ObservableList<Player> playerObservableList;
    private State currentState;
    private State previousState;
    private Player thisPlayer;
    private Boolean spectatorList = false;
    private Boolean buildingAllowed = true;
    private Constants.FIELD_MODE currentFieldMode;
    private Player oldPlayer;
    private int currentActionCount;
    private int currentDropSize;
    private boolean rejoin = false;
    private boolean startPhase = false;

    @Inject
    public PioneersController(Provider<LobbySelectController> lobbySelectControllerProvider, PioneersService pioneersService, PioneersUIService pioneersUIService, EventListener eventListener, LoginResultStorage loginResultStorage, ResourceBundle resourceBundle, App app, MapService mapService, UserService userService, Provider<ChatController> chatControllerProvider, GameMemberService memberService, PrefService prefService, StringToKeyCodeService stringToKeyCodeService, Provider<VictoryController> victoryControllerProvider, MusicService musicService, AnimationService animationService, AuthenticationService authenticationService) {
        this.pioneersService = pioneersService;
        this.pioneersUIService = pioneersUIService;
        this.eventListener = eventListener;
        this.loginResultStorage = loginResultStorage;
        this.resourceBundle = resourceBundle;
        this.app = app;
        this.mapService = mapService;
        this.userService = userService;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
        this.chatControllerProvider = chatControllerProvider;
        this.musicService = musicService;
        this.memberService = memberService;
        this.prefService = prefService;
        this.stringToKeyCodeService = stringToKeyCodeService;
        this.victoryControllerProvider = victoryControllerProvider;
        this.animationService = animationService;
        this.authenticationService = authenticationService;
    }

    @Override
    public void init() {
        if (rejoin) {
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        playerObservableList = FXCollections.observableArrayList();
        compositeDisposable.add(memberService.findAllGamesMembers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            for (Member member : result) {
                if (member.spectator() && (!(member.userId()).equals(loginResultStorage.getLoginResult()._id()))) {
                    spectators.add(member);
                }
            }

        }));

        //Collect Player Information
        Disposable getGamePlayersDisposable = pioneersService.getGamePlayers().observeOn(Constants.FX_SCHEDULER)
                .subscribe(result -> {
                    result.forEach(player -> {
                        userIdToPlayer.put(player.userId(), player);
                        colorToUserID.put(player.color(), player.userId());
                        if (!player.userId().equals(loginResultStorage.getLoginResult()._id())) {
                            playerObservableList.add(player);
                        } else {
                            thisPlayer = player;
                            //Setup work based on players
                            pioneersUIService.generatePlayerPortrait(currentplayer_image, loginResultStorage.getLoginResult().avatar(), Color.valueOf(thisPlayer.color()), 80);
                            tradingPartnerController.setPlayerPortrait(player);
                            offerMenuController.setPlayerPortrait(player);
                            tradeMenuController.setPlayerPortrait(player);
                            if (rejoin) {
                                // If player rejoined the game update map and UI elements
                                oldPlayer = thisPlayer;
                                updateDevelopmentCards();
                                updateVictoryPoints();
                                updateResources();
                                refreshKnights(thisPlayer.userId(), true);
                            }
                        }
                    });

                    // dummy player for spectator
                    if (thisPlayer == null) {
                        thisPlayer = new Player("", "", "ffffff", false, -1, null, null, -1, -1, null);
                        pioneersUIService.generatePlayerPortrait(currentplayer_image, loginResultStorage.getLoginResult().avatar(), Color.valueOf(thisPlayer.color()), 80);
                    }

                    //Collect game state
                    compositeDisposable.add(pioneersService.getGameState().observeOn(Constants.FX_SCHEDULER)
                            .subscribe(result1 -> {
                                previousState = currentState;
                                currentState = result1;
                                String action = currentState.expectedMoves().get(0).action();
                                // Check if the user change to ingame in which state the game is
                                if (currentState.expectedMoves().get(0) != null && !action.equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                                    label_turn.setText(resourceBundle.getString(currentState.expectedMoves().get(0).action()));
                                }
                                if (result1.expectedMoves().get(0).action().equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                                    label_turn.setText(resourceBundle.getString("founding.roll") + "\n" + "0/" + userIdToPlayer.size());
                                    setUIElements(Constants.ACTION.FOUNDING_ROLL.toString(), true);
                                } else if (result1.expectedMoves().get(0).action().equals(Constants.ACTION.ROB.toString())
                                        && result1.expectedMoves().get(0).players().contains(loginResultStorage.getLoginResult()._id())) {
                                    // Player has rejoined in robber phase
                                    setUIElements(Constants.ACTION.ROB.toString(), true);
                                    // Not allowed to buy development cards or build during rob phase
                                    buildingAllowed = false;
                                    // Set card allowed false, so you can't play development cards
                                    developmentCardsController.setPlayCardAllowed(false);
                                    // Set robber mode is true so after all buildings are placed the robe mode starts in map controller
                                    mapController.setRobberMode(true, thisPlayer.color());

                                } else if (pioneersService.expectedMovesContainsAction(result1.expectedMoves(), Constants.ACTION.OFFER)
                                        && result1.expectedMoves().get(0).players().contains(loginResultStorage.getLoginResult()._id())) {
                                    // Player has rejoined in offer phase
                                    // Make an empty offer move to cancel
                                    pioneersService.makeMoveAction(Constants.ACTION.OFFER.toString()).observeOn(Constants.FX_SCHEDULER).subscribe();

                                } else if (pioneersService.expectedMovesContainsAction(result1.expectedMoves(), Constants.ACTION.ACCEPT)
                                        && result1.expectedMoves().get(0).players().contains(loginResultStorage.getLoginResult()._id())) {
                                    // Player has rejoined in accept phase
                                    // Make an empty accept move to cancel
                                    pioneersService.makeMoveTrade(Constants.ACTION.ACCEPT.toString(), null, null).observeOn(Constants.FX_SCHEDULER).subscribe();

                                } else if (pioneersService.expectedMovesContainsAction(result1.expectedMoves(), Constants.ACTION.DROP)
                                        && result1.expectedMoves().get(0).players().contains(loginResultStorage.getLoginResult()._id())) {
                                    // Player has rejoined in drop phase
                                    // Set up for drop controller and show menu if he has more than 7 resources
                                    tradeMenuController.setCurrentResources(thisPlayer.resources());
                                    if (tradeMenuController.getResourcesCount() > 7) {
                                        developmentCardsController.hideMenu();
                                        tradeMenuController.showMenu(false);
                                    }
                                } else {
                                    // Player has rejoined in some other phase so set the UI elements
                                    setUIElements(result1.expectedMoves().get(0).action(),
                                            result1.expectedMoves().get(0).players().contains(loginResultStorage.getLoginResult()._id()));
                                }
                            }));

                    compositeDisposable.add(userService.findAllOnlineUsers().observeOn(Constants.FX_SCHEDULER).subscribe(result2 -> {
                        result2.forEach(user -> userIdToUserInfo.put(user._id(), user));
                        //load listview with playerlist
                        players_listview.setItems(playerObservableList);

                        //build items step by step
                        players_listview.setCellFactory(param -> {
                            PlayerListViewCell playerListViewCell = new PlayerListViewCell(resourceBundle, pioneersUIService);
                            playerListViewCell.setUserList(userIdToUserInfo);
                            playerListViewCells.add(playerListViewCell);

                            return playerListViewCell;
                        });
                        spectators_listview.setItems(spectators);
                        spectators_listview.setCellFactory(re -> {
                            SpectatorListViewCell spectatorListViewCell = new SpectatorListViewCell(resourceBundle);
                            spectatorListViewCell.setUserList(userIdToUserInfo);

                            return spectatorListViewCell;
                        });
                    }));
                });

        //Websocket for buildings - update relations based on building placements
        Disposable buildingDisposable = eventListener.listen("games." + pioneersService.getStoredGame()._id() + ".buildings.*.*", Building.class).observeOn(Constants.FX_SCHEDULER).subscribe(event -> {
            currentFieldMode = null;
            Building building = event.data();
            if (event.event().endsWith(".created")) {
                //Perform UI/Harbor logging operations if building is related to player
                if (building.owner().equals(loginResultStorage.getLoginResult()._id())) {
                    musicService.playBuildingSound();
                    //update UI after a building is placed to check whether enough resources are left to build settlements/roads/cities and disable buttons based off that
                    setUIElements(currentState.expectedMoves().get(0).action(), true);

                    // Update trade relations based on new harbor bases, if they are related to the player.
                    if (building.owner().equals(loginResultStorage.getLoginResult()._id())) {
                        HexagonPointSubController subCon = mapController.findPointSubConFromBuilding(building);
                        if (subCon != null) {
                            for (var harbor : mapService.getNeighborHarbors(subCon.getMyHexagonPoint())) {
                                if (harbor.type() == null && !myHarbors.contains("null")) {
                                    myHarbors.add("null");
                                }
                                if (!myHarbors.contains(harbor.type())) {
                                    myHarbors.add(harbor.type());
                                }
                            }
                        }
                    }
                }
            }
        });

        //Websocket for game state - implements turn logic and updates UI accordingly - also implements robber positioning
        Disposable stateDisposable = eventListener.listen(Constants.GAMES + "." + pioneersService.getStoredGame()._id() + ".state" + ".*", State.class)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(event -> {
                    State state = event.data();
                    if (event.event().endsWith(".updated")) {
                        if (state.expectedMoves().size() > 0) {
                            ExpectedMove nextMove = state.expectedMoves().get(0);
                            if (!nextMove.action().equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                                if (nextMove.action().equals(Constants.ACTION.OFFER.toString())) {
                                    int maxOfferCount = -1;
                                    for (Map.Entry<String, Player> entry : userIdToPlayer.entrySet()) {
                                        if (entry.getValue().active()) {
                                            maxOfferCount++;
                                        }
                                    }
                                    label_turn.setText(resourceBundle.getString(nextMove.action()) + "\n" + currentActionCount + "/" + maxOfferCount);
                                } else if (nextMove.action().equals(Constants.ACTION.DROP.toString())) {
                                    // Set current drop size dependent on next move drop players size
                                    if (currentDropSize == 0) {
                                        currentDropSize = nextMove.players().size();
                                    }
                                    label_turn.setText(resourceBundle.getString(nextMove.action()) + "\n" + currentActionCount + "/" + currentDropSize);
                                } else {
                                    currentActionCount = 0;
                                    currentDropSize = 0;
                                    label_turn.setText(resourceBundle.getString(nextMove.action()) + "\n" + userIdToUserInfo.get(nextMove.players().get(0)).name());

                                    // show whose turn it is
                                    if (nextMove.action().equals(Constants.ACTION.ROLL.toString()) || nextMove.action().equals(Constants.ACTION.FOUNDING_SETTLEMENT1.toString()) || nextMove.action().equals(Constants.ACTION.FOUNDING_SETTLEMENT2.toString())) {
                                        for (PlayerListViewCell playerListViewCell : playerListViewCells) {
                                            if (playerListViewCell.getPlayer() != null) {
                                                pioneersUIService.outline(playerListViewCell.player_grid, playerListViewCell.getPlayer(), nextMove);
                                            }
                                        }
                                    }

                                }
                            }
                            if (nextMove.action().equals(Constants.ACTION.ROLL.toString()) && nextMove.players().contains(loginResultStorage.getLoginResult()._id())) {
                                musicService.playRoundStartSound();
                            }

                            boolean myTurn = nextMove.players().contains(loginResultStorage.getLoginResult()._id());
                            setUIElements(state.expectedMoves().get(0).action(), myTurn);

                            this.previousState = this.currentState;
                            this.currentState = state;

                            if (nextMove.action().equals(Constants.ACTION.ACCEPT.toString())) {
                                tradingPartnerController.checkForOffer();
                            }

                            // player used build road card
                            if (nextMove.action().equals(Constants.ACTION.BUILD_ROAD.toString()) &&
                                    nextMove.players().contains(loginResultStorage.getLoginResult()._id())) {

                                // set field off if already existing
                                this.mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                                // open road field mode
                                this.mapController.setFieldMode(Constants.FIELD_MODE.PLACE_ROAD, Constants.ACTION.BUILD_ROAD.toString(), Constants.ROAD, Color.valueOf(thisPlayer.color()));
                            }

                            //Check for Specific Events (Note that Drop -> Rob is the order on 7 roll) if they are related to thisPlayer
                            if (pioneersService.expectedMovesContainsPlayer(currentState.expectedMoves(), thisPlayer)) {
                                //Expected Move is Drop with this particular player
                                if (pioneersService.expectedMovesContainsExpectedMove(currentState.expectedMoves(), thisPlayer, Constants.ACTION.DROP)) {
                                    //If the menu isn't already open, open it.
                                    if (!tradeMenuController.isVisible()) {
                                        tradeMenuController.setCurrentResources(thisPlayer.resources());
                                        developmentCardsController.hideMenu();
                                        tradeMenuController.showMenu(false);
                                    }
                                } else {
                                    //Expected Move is Rob (and no other players are left dropping) -> set FieldMode
                                    if (!pioneersService.expectedMovesContainsAction(currentState.expectedMoves(), Constants.ACTION.DROP) && pioneersService.expectedMovesContainsAction(currentState.expectedMoves(), Constants.ACTION.ROB)) {
                                        // Not allowed to buy development cards or build during rob phase
                                        buildingAllowed = false;
                                        // Set card allowed false, so you can't play development cards
                                        developmentCardsController.setPlayCardAllowed(false);

                                        mapController.setFieldMode(Constants.FIELD_MODE.PLACE_ROBBER, Constants.ACTION.ROB.toString(), null, Color.valueOf(thisPlayer.color()));
                                    }
                                }
                            }

                            //If the previous State was "rob", update robber position and turn off field mode if thisPlayer robbed someone
                            if (previousState != null && pioneersService.moveWasDone(previousState, currentState, Constants.ACTION.ROB)) {
                                mapController.updateRobberPosition(state.robber());
                                if (pioneersService.expectedMovesContainsPlayer(currentState.expectedMoves(), thisPlayer)) {
                                    mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                                    // Set card allowed true, so you can play development cards
                                    developmentCardsController.setPlayCardAllowed(true);
                                    // Allowed to buy development cards or build after rob phase
                                    buildingAllowed = true;
                                }

                            }
                        }
                    }

                    //change to victory screen
                    if (state.winner() != null) {
                        pioneersUIService.showWinner(state.winner(), loginResultStorage.getLoginResult()._id(), thisPlayer.gameId(), resourceBundle, inGamePane, inGameAnchorPane);

                        Timer timer = new Timer();
                        TimerTask timerTask = new TimerTask() {
                            @Override
                            public void run() {
                                Platform.runLater(() -> {
                                    final VictoryController victorycontroller = victoryControllerProvider.get();
                                    victorycontroller.setUserList(userIdToUserInfo);
                                    app.show(victorycontroller);
                                });
                            }
                        };
                        timer.schedule(timerTask, 4000);
                    }
                });

        //Websocket for player status - updates UI resources
        Disposable playerDisposable = eventListener.listen(Constants.GAMES + "." + pioneersService.getStoredGame()._id() + ".players.*.*", Player.class).observeOn(Constants.FX_SCHEDULER).subscribe(event -> {
            Player player = event.data();
            if (event.event().endsWith(".updated")) {

                userIdToPlayer.replace(player.userId(), player);
                refreshLongestRoad();
                if (player.userId().equals(loginResultStorage.getLoginResult()._id())) {
                    oldPlayer = thisPlayer;
                    thisPlayer = player;
                    // Check you have gained a new development card
                    // If you gained one get the last one because this card was added
                    if (player.developmentCards().size() > oldPlayer.developmentCards().size()) {
                        pioneersUIService.animateDevelopmentCard(player.developmentCards().get(player.developmentCards().size() - 1).type(), buildMenu, development_menu);
                    }
                    updateResources();
                    updateVictoryPoints();
                    updateDevelopmentCards();
                    setUIElements(currentState.expectedMoves().get(0).action(), buildingAllowed);
                    if (tradeMenuController.isVisible()) {
                        //If Menu is open, update the resources within
                        tradeMenuController.setCurrentResources(player.resources());
                    }
                } else {
                    for (PlayerListViewCell playerListViewCell : playerListViewCells) {
                        if (playerListViewCell.getPlayer() != null && playerListViewCell.getPlayer().userId().equals(player.userId())) {
                            playerListViewCell.updatePlayerListLabels(player);
                            if (rejoin) {
                                refreshKnights(thisPlayer.userId(), true);
                                rejoin = false;
                            }
                        }
                    }
                }
            }
        });

        //Websocket for dice roll
        Disposable moveDisposable = eventListener.listen(Constants.GAMES + "." + pioneersService.getStoredGame()._id() + ".moves.*.*", Move.class).observeOn(Constants.FX_SCHEDULER).subscribe(event -> {
            Move move = event.data();
            if (event.event().endsWith(".created")) {
                // check that move is a roll but not the founding roll
                if (event.data().roll() != 0 && !event.data().action().equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                    pioneersUIService.setDiceImage(event.data(), image_dice, image_dice1);
                    image_dice1.setVisible(true);
                    image_dice1.setDisable(false);
                }

                // close trade menu for spectator after trade is finished
                if (move.action().equals(Constants.ACTION.ACCEPT.toString()) && thisPlayer.gameId().equals("")) {
                    tradingPartnerController.hideMenu();
                }

                if (!move.userId().equals(loginResultStorage.getLoginResult()._id())) {
                    if (move.action().equals(Constants.ACTION.BUILD.toString()) && move.resources() != null
                            && move.partner() == null) {

                        // normal player
                        if (!thisPlayer.gameId().equals("")) {
                            offerMenuController.showMenuThings(move);
                            // spectator
                        } else {
                            Player offer = userIdToPlayer.get(move.userId());
                            User offerUser = userIdToUserInfo.get(move.userId());
                            tradingPartnerController.setSpectator(true, offerUser);
                            tradingPartnerController.setResourcesDto(move.resources());
                            tradingPartnerController.setPlayerPortrait(offer);
                            tradingPartnerController.showMenu();
                        }
                    }
                }
                if (move.action().equals(Constants.ACTION.OFFER.toString())) {
                    tradingPartnerController.setMove(move);
                    currentActionCount++;
                }

                if (move.action().equals(Constants.ACTION.DROP.toString())) {
                    currentActionCount++;
                }
                if (move.action().equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                    currentActionCount++;
                    label_turn.setText(resourceBundle.getString("founding.roll") + "\n" + currentActionCount + "/" + userIdToPlayer.size());
                }

                // show mini pop ups
                if (move.developmentCard() != null && !move.userId().equals(loginResultStorage.getLoginResult()._id())) {
                    miniPopController.show(move.developmentCard(), userIdToUserInfo.get(move.userId()).name());
                }

                if (move.developmentCard() != null && move.developmentCard().equals(Constants.DEVELOPMENT_CARDS.KNIGHT.toString())) {
                    // refresh knight count in player list
                    refreshKnights(move.userId(), false);
                }
            }
        });


        //Collect Disposables
        compositeDisposable.addAll(playerDisposable, buildingDisposable, stateDisposable, getGamePlayersDisposable, moveDisposable);

        if(musicService.getMusicContext() == null || !musicService.getMusicContext().equals(Constants.MUSIC_CONTEXT.INGAME)){
            musicService.playMusic(Constants.MUSIC_CONTEXT.INGAME);
        }
    }

    private void updateDevelopmentCards() {
        int notRevealed = 0;
        for (DevelopmentCard card : thisPlayer.developmentCards()) {
            if (!card.revealed()) {
                notRevealed += 1;
            }
        }
        this.label_development.setText(Integer.toString(notRevealed));
        this.own_development_cards.setText(Integer.toString(notRevealed));
        // Set list of development cards from player to controller
        this.developmentCardsController.setPlayer(thisPlayer);
    }

    private void updateResources() {
        Resources ownResources = thisPlayer.resources();
        pioneersUIService.updateResourceLabels(label_lumber, label_wool, label_ore, label_brick, label_grain, unknown_resource, ownResources);
        pioneersUIService.animateCards(oldPlayer, thisPlayer, buildMenu);
    }

    private void updateVictoryPoints() {
        Number victoryPoints = thisPlayer.victoryPoints();
        victory_points.setText(victoryPoints.toString());
    }

    public void refreshLongestRoad() {

        Player candidate = pioneersService.determineLongestRoad(userIdToPlayer);

        // set labels
        pioneersUIService.updateLabel(longest_road, thisPlayer, candidate, null);

        for (PlayerListViewCell playerListViewCell : playerListViewCells) {
            if (playerListViewCell.getPlayer() != null) {
                Player player = userIdToPlayer.get(playerListViewCell.getPlayer().userId());
                //Update for other players
                pioneersUIService.updateLabel(playerListViewCell.longest_road, player, candidate, null);

            }
        }

        //Consider candidate
        if (candidate != null) {
            if (longestRoadController.getLongestPlayerId() == null || !longestRoadController.getLongestPlayerId().equals(candidate.userId())) {
                longestRoadController.setLongestPlayerId(candidate.userId());
                longestRoadController.show(userIdToUserInfo.get(candidate.userId()), userIdToPlayer.get(candidate.userId()), false);
            }
        } else {
            longestRoadController.setLongestPlayerId(null);
        }


    }

    public void refreshKnights(String id, boolean rejoin) {

        // get player with the highest knight count
        Player candidate = pioneersService.determineKnights(userIdToPlayer, id, rejoin);

        // update own knight count and color
        pioneersUIService.updateLabel(label_knight_own, thisPlayer, candidate, pioneersService.getArmies());

        // update other player knight count and color
        for (PlayerListViewCell playerListViewCell : playerListViewCells) {
            if (playerListViewCell.getPlayer() != null) {
                Player player = userIdToPlayer.get(playerListViewCell.getPlayer().userId());

                // update knight count and color for other players
                pioneersUIService.updateLabel(playerListViewCell.label_knight_counter, player, candidate, pioneersService.getArmies());
            }
        }

        //Consider candidate

        if (!rejoin) {
            if (candidate != null && candidate.hasLargestArmy()) {
                if (longestRoadController.getLargestPlayerId() == null || !longestRoadController.getLargestPlayerId().equals(candidate.userId())) {
                    longestRoadController.setLargestPlayerId(candidate.userId());
                    longestRoadController.show(userIdToUserInfo.get(candidate.userId()), userIdToPlayer.get(candidate.userId()), true);
                }
            } else {
                longestRoadController.setLargestPlayerId(null);
            }
        }
    }

    private void disableUIButtons() {
        button_development_card.setOpacity(0.3f);
        button_settlement.setOpacity(0.3f);
        button_city.setOpacity(0.3f);
        button_road.setOpacity(0.3f);
        button_trade.setDisable(true);
        button_endTurn.setDisable(true);
    }

    public void hideLoadingLabel() {
        loading_label.setVisible(false);
    }

    private void showAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, context);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private void setUIElements(String action, boolean myTurn) {
        Constants.ACTION currentAction = Constants.ACTION.BUILD;
        for (Constants.ACTION act : Constants.ACTION.values()) {
            if (Objects.equals(act.toString(), action)) {
                currentAction = act;
                break;
            }
        }

        if (myTurn && !thisPlayer.gameId().equals("")) {
            switch (currentAction) {
                case FOUNDING_ROLL, ROLL -> {
                    startPhase = true;
                    mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                    pioneersUIService.enableUIDice(true, image_dice, image_dice1);
                    setDevelopmentCardAllowed(false);
                    disableUIButtons();
                }
                case FOUNDING_SETTLEMENT1, FOUNDING_SETTLEMENT2 -> {
                    mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                    mapController.setFieldMode(Constants.FIELD_MODE.PLACE_FOUNDING_SETTLEMENT, action, "settlement", Color.valueOf(thisPlayer.color()));
                    pioneersUIService.enableUIDice(false, image_dice, image_dice1);
                    disableUIButtons();
                }
                case FOUNDING_ROAD1, FOUNDING_ROAD2 -> {
                    mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                    mapController.setFieldMode(Constants.FIELD_MODE.PLACE_FOUNDING_ROAD, action, "road", Color.valueOf(thisPlayer.color()));
                    pioneersUIService.enableUIDice(false, image_dice, image_dice1);
                    disableUIButtons();
                }
                case BUILD -> {
                    startPhase = false;
                    pioneersUIService.enableUIDice(false, image_dice, image_dice1);
                    button_endTurn.setDisable(false);
                    button_trade.setDisable(false);

                    button_settlement.setDisable(false);
                    button_city.setDisable(false);
                    button_road.setDisable(false);

                    // Can play development cards
                    setDevelopmentCardAllowed(true);

                    // reduces opacity
                    float opacitySettlement = pioneersService.checkSettlementResources(thisPlayer) ? 1 : 0.3f;
                    button_settlement.setOpacity(opacitySettlement);
                    float opacityCity = pioneersService.checkCityResources(thisPlayer) ? 1 : 0.3f;
                    button_city.setOpacity(opacityCity);
                    float opacityRoad = pioneersService.checkRoadResources(thisPlayer) ? 1 : 0.3f;
                    button_road.setOpacity(opacityRoad);
                    // Check first if there are no development cards available if false, check for your
                    // resources and set the opacity of this button
                    button_development_card.setOpacity(pioneersService.checkNoDevelopmentCardsAvailable(userIdToPlayer) ?
                            0.3f : pioneersService.checkDevelopmentResources(thisPlayer) ? 1 : 0.3f);
                }
                case ROB -> {
                    pioneersUIService.enableUIDice(false, image_dice, image_dice1);
                    disableUIButtons();
                }
            }
        } else {
            pioneersUIService.enableUIDice(false, image_dice, image_dice1);
            disableUIButtons();
        }
    }

    @Override
    public void destroy() {
        if (compositeDisposable.size() > 0) {
            compositeDisposable.dispose();
        }
        //Destroy all mechanic sub controllers
        mechanicSubControllers.forEach(Controller::destroy);
    }

    @Override
    public Parent render() {
        // load inGame Screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/inGameScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> {
            if (c == PioneersController.class) {
                return this;
            }
            if (c == ChatController.class) {
                return chatControllerProvider.get();
            }
            if (c == TradeAndDropSubController.class) {
                return new TradeAndDropSubController(pioneersService, pioneersUIService, loginResultStorage, this, tradingPartnerController, app, resourceBundle, userIdToPlayer);
            }
            if (c == TradeForOthersSubController.class) {
                return new TradeForOthersSubController(pioneersService, pioneersUIService, loginResultStorage, tradeMenuController, this);
            }
            if (c == TradingPartnerSubController.class) {
                return new TradingPartnerSubController(pioneersUIService, pioneersService, loginResultStorage, resourceBundle, animationService, this);
            }
            if (c == RobSubController.class) {
                return new RobSubController(resourceBundle, userIdToUserInfo, pioneersUIService);
            }
            if (c == LongestRoadController.class) {
                return new LongestRoadController(resourceBundle, pioneersUIService, animationService);
            }
            if (c == KeyboardShortcutController.class) {
                return new KeyboardShortcutController(resourceBundle, prefService);
            }
            if (c == MusicController.class) {
                return new MusicController(musicService, resourceBundle);
            }
            if (c == PlentyMonopolySubController.class) {
                return new PlentyMonopolySubController(resourceBundle, pioneersService);
            }
            if (c == DevelopmentCardsSubController.class) {
                return new DevelopmentCardsSubController(resourceBundle, plentyMonopolyController, pioneersService);
            }
            if (c == MiniPopController.class) {
                return new MiniPopController(resourceBundle, animationService);
            }
            return null;
        });
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Initialize buttons state when screen is loading
        disableUIButtons();

        //Set window title
        app.setScreenTitle(resourceBundle.getString(Constants.INGAME_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());

        //set current name and image from avatar
        currentplayer_label.setText(loginResultStorage.getLoginResult().name());

        compositeDisposable.add(memberService.getGameMember().subscribe(res -> {
            victory_points.setVisible(false);
            if (!res.spectator()) {
                own_player.setVisible(true);
                victory_points.setVisible(true);
                eye.setVisible(false);
                victory_points.setVisible(true);

                compositeDisposable.add(pioneersService.getGamePlayers().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                    Color color = Color.valueOf(thisPlayer.color());
                    //Blend image with given color
                    Blend tint = new Blend();
                    tint.setTopInput(new ColorInput(0, 0, imageview_city_roof.getImage().getWidth(), imageview_city_roof.getImage().getHeight(), color));
                    tint.setMode(BlendMode.SRC_ATOP);
                    tint.setOpacity(0.75);
                    imageview_city_roof.setEffect(tint);
                    imageview_settlement_roof.setEffect(tint);
                    imageview_road.setEffect(tint);
                }));
            }
        }));

        mapController = new PioneersMapController(false, inGamePane, inGameAnchorPane, mapService, stringToKeyCodeService, pioneersService, prefService, resourceBundle, eventListener, loginResultStorage, robMenuController, userIdToPlayer, colorToUserID, this, animationService);

        Collections.addAll(mechanicSubControllers, mapController, chatController, tradeMenuController, longestRoadController, keyboardShortcutController, offerMenuController, tradingPartnerController, robMenuController, musicMenuController, plentyMonopolyController, developmentCardsController, miniPopController);


        //Preparation
        chatController.setIsInGameChat(true);
        chatController.setParent(parent);
        chatController.setEmojiMenu(rootEmojiMenu, searchScrollPane, searchFlowPane, tabPane, txtSearch, boxTone);
        chatController.setSpectators(spectators);
        tradeMenuController.setParent(parent);
        longestRoadController.setParent(parent);
        tradeMenuController.setBuildMenu(buildMenu);
        keyboardShortcutController.setParent(parent);
        offerMenuController.setParent(parent);
        offerMenuController.setBuildMenu(offer_menu);
        tradingPartnerController.setParent(parent);
        robMenuController.setParent(parent);
        musicMenuController.setParent(parent);
        plentyMonopolyController.setParent(parent);
        developmentCardsController.setParent(parent);
        mapController.setRejoin(rejoin);
        //Init + render
        mechanicSubControllers.forEach(msc -> {
            msc.init();
            msc.render();
        });
        //Post
        developmentCardsController.setMapController(mapController);
        tradeMenuController.setTradingPartnerController(tradingPartnerController);
        offerMenuController.setListPlayers(userIdToPlayer);
        offerMenuController.setListUsers(userIdToUserInfo);
        tradingPartnerController.setPlayerList(userIdToPlayer);
        tradingPartnerController.setUserList(userIdToUserInfo);

        pioneersUIService.setAnimationService(animationService);
        this.chatController.createLobbyController(pioneersService.getStoredGame()._id(), userIdToUserInfo);

        inGamePane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.W) && image_dice.isVisible() && !image_dice.isDisabled()) {
                throwDice(null);
            } else if (event.getCode().equals(KeyCode.E) && button_endTurn.isVisible() && !button_endTurn.isDisabled()) {
                endTurn(new ActionEvent());
            }
        });
        return parent;
    }

    @FXML
    private void throwDice(MouseEvent mouseEvent) {
        //Function that's called when clicking either of the dice
        String action = currentState.expectedMoves().get(0).action();
        compositeDisposable.add(pioneersService.makeMoveAction(action).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            buildingAllowed = true;
            if (result._id().equals(Constants.CUSTOM_ERROR)) {
                showAlert(resourceBundle.getString("error"), result._id() + "\n" + resourceBundle.getString("try.again"));
            } else {
                if (result.action().equals(Constants.ACTION.FOUNDING_ROLL.toString())) {
                    image_dice.setImage(pioneersUIService.numberToImage(result.roll()));
                }
                musicService.playDiceSound();
            }
        }));

        if (mouseEvent != null) {
            mouseEvent.consume();
        }
    }

    public void endTurn(ActionEvent event) {
        compositeDisposable.add(pioneersService.makeMoveAction("build").observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            if (!result._id().equals("")) {
                buildingAllowed = false;
                mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                developmentCardsController.hideMenu();
                setDevelopmentCardAllowed(false);
                currentFieldMode = null;
            }
        }));
        event.consume();
    }

    public void buildBuildingView(String buildingType) {
        //Generic build Building function - activated when building buttons are pressed.
        //Note that we don't care about founding stuff here, since this is only triggered by the UI Buttons.
        Player player = userIdToPlayer.get(loginResultStorage.getLoginResult()._id());
        boolean canBuild = false;
        boolean enoughBuildings = false;
        Constants.FIELD_MODE modeToSet = Constants.FIELD_MODE.OFF;

        switch (buildingType) {
            case Constants.SETTLEMENT -> {
                canBuild = pioneersService.checkSettlementResources(player);
                enoughBuildings = player.remainingBuildings().settlement() >= 1;
                modeToSet = Constants.FIELD_MODE.PLACE_SETTLEMENT;
            }
            case Constants.ROAD -> {
                canBuild = pioneersService.checkRoadResources(player);
                enoughBuildings = player.remainingBuildings().road() >= 1;
                modeToSet = Constants.FIELD_MODE.PLACE_ROAD;
            }
            case Constants.CITY -> {
                canBuild = pioneersService.checkCityResources(player);
                enoughBuildings = player.remainingBuildings().city() >= 1;
                modeToSet = Constants.FIELD_MODE.PLACE_CITY;
            }
            default -> System.out.println("BUILDING ERROR: PASSED UNKNOWN BUILDING PARAMETER");
        }

        Timer timer = new Timer();

        if (buildingAllowed) {
            // player has enough resources and settlement figure to build settlement
            if (canBuild) {
                mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                if (currentFieldMode != modeToSet) {
                    if (!mapController.setFieldMode(modeToSet, "build", buildingType, Color.valueOf(thisPlayer.color()))) {
                        //If setting the mode turned out with no visible select Circles, show error message.
                        if (modeToSet == Constants.FIELD_MODE.PLACE_SETTLEMENT) {
                            // show error message that house building needs two streets apart
                            label_insufficient_res.setText(resourceBundle.getString(Constants.SETTLEMENT_TWO_ROADS));
                        } else {
                            if(modeToSet == Constants.FIELD_MODE.PLACE_CITY){
                                label_insufficient_res.setText(resourceBundle.getString(Constants.NO_CITIES_TO_UPGRADE));
                            }
                            else {
                                label_insufficient_res.setText(Constants.CUSTOM_ERROR);
                            }
                        }
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // clear label after 4 seconds
                                Platform.runLater(() -> label_insufficient_res.setText(""));
                            }
                        }, 4000);
                    } else {
                        //If setting the mode turned out with visible select Circles, log field mode.
                        currentFieldMode = modeToSet;
                    }
                } else {
                    //If currentFieldMode is the mode to set, toggle field mode off and reset currentFieldMode.
                    currentFieldMode = null;
                }

            } else {
                Constants.GAME_NOTICE type = enoughBuildings ? Constants.GAME_NOTICE.NOT_ENOUGH_RESOURCES : Constants.GAME_NOTICE.NOT_ENOUGH_FIGURES;
                pioneersUIService.showInGameNotice(type, label_insufficient_res, resourceBundle);
            }
        }
    }

    public void getDevelopmentCard(ActionEvent actionEvent) {
        if (thisPlayer.userId().equals("")) {
            return;
        }
        if (this.buildingAllowed) {
            if (this.pioneersService.checkDevelopmentResources(thisPlayer)) {
                // Turn off field mode and hide development card menu when you buy a card
                this.mapController.setFieldMode(Constants.FIELD_MODE.OFF, "", "", Color.valueOf(thisPlayer.color()));
                this.developmentCardsController.hideMenu();
                this.currentFieldMode = null;
                // Get a development card
                this.pioneersService.makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(), Constants.DEVELOPMENT_CARDS.NEW_CARD.toString())
                        .observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                            if (result.createdAt().equals(Constants.DEVELOPMENT_CARD_ERROR)) {
                                pioneersUIService.showInGameNotice(Constants.GAME_NOTICE.NO_DEVELOPMENT_CARDS_LEFT, label_insufficient_res, resourceBundle);
                            }
                        });
            } else {
                // Show error message if you don't have enough resources
                this.pioneersUIService.showInGameNotice(Constants.GAME_NOTICE.NOT_ENOUGH_RESOURCES, label_insufficient_res, resourceBundle);
            }
        }
        actionEvent.consume();
    }

    public void buildSettlement(ActionEvent event) {
        if (!thisPlayer.userId().equals("") && !startPhase) {
            buildBuildingView(Constants.SETTLEMENT);
        }
        event.consume();
    }

    public void buildRoad(ActionEvent event) {
        if (!thisPlayer.userId().equals("") && !startPhase) {
            buildBuildingView(Constants.ROAD);
        }
        event.consume();
    }

    public void buildCity(ActionEvent event) {
        if (!thisPlayer.userId().equals("") && !startPhase) {
            buildBuildingView(Constants.CITY);
        }
        event.consume();
    }

    public void exit(ActionEvent actionEvent) {
        //on exit click
        if (actionEvent == null) {
            pioneersService.setPlayerInactive(false).observeOn(Constants.FX_SCHEDULER).subscribe();
            userService.setUserOffline().observeOn(Constants.FX_SCHEDULER).subscribe();
            authenticationService.logout().observeOn(Constants.FX_SCHEDULER).subscribe();
            if (userIdToPlayer.size() < 2) {
                pioneersService.deleteGame().observeOn(Constants.FX_SCHEDULER).subscribe();
            }
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(app.getPrimaryStage());
        alert.setResizable(false);
        alert.setContentText(resourceBundle.getString("leaveGame"));
        ButtonType yesButton = new ButtonType(resourceBundle.getString("yes"), ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(resourceBundle.getString("no"), ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        alert.showAndWait().ifPresent(type -> {
            if (type == yesButton) {
                if (thisPlayer == null || !thisPlayer.active()) {
                    showLobbySelect();
                } else {
                    if (userIdToPlayer.size() < 2) {
                        // Delete game if it has less than 2 players
                        compositeDisposable.add(pioneersService.deleteGame().observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                            showLobbySelect();
                        }));
                    } else {
                        // set Player inactive
                        compositeDisposable.add(pioneersService.setPlayerInactive(false).observeOn(Constants.FX_SCHEDULER).subscribe(result1 -> {
                            if (result1.userId().equals(Constants.CUSTOM_ERROR)) {
                                Alert error = new Alert(Alert.AlertType.ERROR);
                                error.initOwner(app.getPrimaryStage());
                                error.setResizable(false);
                                error.setContentText(resourceBundle.getString("error"));
                                error.showAndWait();
                            } else {
                                // change screen
                                showLobbySelect();
                            }
                        }));
                    }
                }
            }
        });
    }

    private void showLobbySelect() {
        final LobbySelectController lobbySelectController = lobbySelectControllerProvider.get();
        app.show(lobbySelectController);

    }


    public void showCostDevelopment(MouseEvent mouseEvent) {
        cost_development_card.setVisible(true);
        mouseEvent.consume();
    }

    public void hideCostDevelopment(MouseEvent mouseEvent) {
        cost_development_card.setVisible(false);
        mouseEvent.consume();
    }

    public void showCostSettlement(MouseEvent mouseEvent) {
        cost_settlement_pop.setVisible(true);
        mouseEvent.consume();
    }

    public void hideCostSettlement(MouseEvent mouseEvent) {
        cost_settlement_pop.setVisible(false);
        mouseEvent.consume();
    }

    public void showCityCost(MouseEvent mouseEvent) {
        cost_city_pop.setVisible(true);
        mouseEvent.consume();
    }

    public void hideCityCost(MouseEvent mouseEvent) {
        cost_city_pop.setVisible(false);
        mouseEvent.consume();
    }

    public void showRoadCost(MouseEvent mouseEvent) {
        cost_road_pop.setVisible(true);
        mouseEvent.consume();
    }

    public void hideRoadCost(MouseEvent mouseEvent) {
        cost_road_pop.setVisible(false);
        mouseEvent.consume();
    }

    public void openTradeMenu(ActionEvent event) {
        // Hide development menu
        this.developmentCardsController.hideMenu();
        // check if just owner hier
        tradeMenuController.showMenu(true);
        tradeMenuController.setCurrentResources(thisPlayer.resources());
        event.consume();
    }


    public ArrayList<String> getMyHarbors() {
        return myHarbors;
    }

    public void toggleChat(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        pioneersUIService.toggleNode(button, chat_board, -1, null);
    }

    public void togglePlayerList(MouseEvent event) {
        ImageView button = (ImageView) event.getSource();
        pioneersUIService.toggleNode(button, player_spectator_list, 1, longestRoadController.root);
    }

    public void openMusicMenu() {
        musicMenuController.toggleMenu();
    }

    public void show_spectator_list(MouseEvent mouseEvent) {
        spectatorList = !spectatorList;
        if (spectatorList) {
            playerLabel.setText(resourceBundle.getString("spectator"));
            spectator_image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/player.png"))));
            spectators_listview.setVisible(true);
            players_listview.setVisible(false);
        } else {
            playerLabel.setText(resourceBundle.getString("player"));
            spectator_image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/eye.png"))));
            spectators_listview.setVisible(false);
            players_listview.setVisible(true);
        }
        mouseEvent.consume();
    }

    public void setBuildingAllowed(Boolean bool) {
        buildingAllowed = bool;
    }

    public Player getThisPlayer() {
        return thisPlayer;
    }

    public void bookClicked(MouseEvent mouseEvent) {
        keyboardShortcutController.toggleMenu();
        mouseEvent.consume();
    }

    public void chatCatchZoom(ScrollEvent scrollEvent) {
        scrollEvent.consume();
    }

    public void tradePartnerCatchZoom(ScrollEvent scrollEvent) {
        scrollEvent.consume();
    }

    public void playerListCatchZoom(ScrollEvent scrollEvent) {
        scrollEvent.consume();
    }

    public void openDevelopmentMenu(MouseEvent mouseEvent) {
        if (!thisPlayer.userId().equals("")) {
            this.developmentCardsController.toggleMenu();
        }
        mouseEvent.consume();
    }

    public void hideDevelopmentMenu() {
        this.developmentCardsController.hideMenu();
    }

    public void setDevelopmentCardAllowed(boolean allowed) {
        this.developmentCardsController.setPlayCardAllowed(allowed);
    }

    public void setRejoin(boolean rejoin) {
        this.rejoin = rejoin;
    }
}