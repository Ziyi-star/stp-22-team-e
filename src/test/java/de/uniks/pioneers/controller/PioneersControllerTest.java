package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.dto.RobDto;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import javax.inject.Provider;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
public class PioneersControllerTest extends ApplicationTest {

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    PrefService prefService;

    @Mock
    GameStorage gameStorage;

    @Mock
    PioneersService pioneersService;

    @Spy
    PioneersUIService pioneersUIService;

    @Mock
    EventListener eventListener;

    @Mock
    App app;
    @Mock
    GameMemberService memberService;
    @Mock
    MusicService musicService;

    @Mock
    LoginResultStorage loginResultStorage;
    @Mock
    VictoryStorage victoryStorage;

    @Mock
    UserService userService;

    @Spy
    AnimationService animationService;

    @InjectMocks
    PioneersController pioneersController;

    @Spy
    MapService mapService;

    @Spy
    Provider<ChatController> chatControllerProvider;

    @Mock
    ChatController chatController;

    @Mock
    StringToKeyCodeService stringToKeyCodeService;

    private Subject<Event<Building>> buildingEventSubject;      //For Building Websocket

    private Subject<Event<State>> stateEventSubject;      //For State Websocket

    private Subject<Event<Player>> playerEventSubject;      //For Player Websocket

    private Subject<Event<Move>> moveEventSubject;      //For Player Websocket

    @Override
    public void start(Stage stage) {
        //Return Users
        when(userService.findAllOnlineUsers()).thenReturn(Observable.just(List.of(new User("1", "JadaMaar", "online", Constants.AVATAR_LIST.get(0), null),
                new User("2", "Owen", "online", Constants.AVATAR_LIST.get(1), null),
                new User("3", "Adrian", "online", Constants.AVATAR_LIST.get(2), null),
                new User("42", "Owen2", "online", Constants.AVATAR_LIST.get(3), null))));
        //Return Login Result
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("", "", "1", "JadaMaar", "online", null, null, "", ""));
        //Return test game
        when(pioneersService.getStoredGame()).thenReturn(new Game("", "", "3", "test game :)", "1", 2, true, new GameSettings(5, 4)));

        when(memberService.findAllGamesMembers()).thenReturn(Observable.just(List.of(new Member("1", "1", "3", "42", true, "#ffffffff", true))));
        when(memberService.getGameMember()).thenReturn(Observable.just(new Member("1", "1", "3", "42", true, "#ffffffff", true)));

        when(chatControllerProvider.get()).thenReturn(chatController);

        //Return test map -> Check Resources for an image of it.
        when(pioneersService.getMap("3")).thenReturn(Observable.just(new Map("3", List.of(
                new Tile(-2, 0, 2, "hills", 9), new Tile(-2, 1, 1, "forest", 4), new Tile(-2, 2, 0, "desert", 7),
                new Tile(-1, -1, 2, "mountains", 2), new Tile(-1, 0, 1, "forest", 3),
                new Tile(-1, 1, 0, "fields", 8), new Tile(-1, 2, -1, "pasture", 9), new Tile(0, -2, 2, "hills", 6),
                new Tile(0, -1, 1, "forest", 10), new Tile(0, 0, 0, "fields", 11), new Tile(0, 1, -1, "mountains", 5),
                new Tile(0, 2, -2, "pasture", 5), new Tile(1, -2, 1, "pasture", 10), new Tile(1, -1, 0, "fields", 4),
                new Tile(1, 0, -1, "pasture", 8), new Tile(1, 1, -2, "hills", 11), new Tile(2, -2, 0, "forest", 3),
                new Tile(2, -1, -1, "hills", 12), new Tile(2, 0, -2, "mountains", 6)
        ), null)));

        //Return test State: Player 1 is building
        when(pioneersService.getGameState()).thenReturn(
                //Observable.just(new State("0", "3", List.of(new ExpectedMove("founding-roll",List.of("1"))))),
                Observable.just(new State("0", "3", List.of(new ExpectedMove("build", List.of("1"))), null))
        );


        //Return test Player Information - Player 1 has BLACK as their color, Player 2 has WHITE as their color, Player 3 has BLUE as their color!
        when(pioneersService.getGamePlayers()).thenReturn(Observable.just(List.of(
                new Player("3", "1", "#000000ff", true, 2, new Resources(10, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, null, false, false, null,
                        List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))),
                new Player("3", "2", "#ffffffff", true, 6, new Resources(7, 1, 0, 3, 2, 1), new RemainingBuildings(3, 0, 6), 2, null, false, false, null,
                        List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))),
                new Player("3", "3", "#0000ffff", true, 4, new Resources(5, 1, 1, 1, 1, 1), new RemainingBuildings(3, 3, 3), 2, null, false, false, null,
                        List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true)))
        )));

        //Log Websockets
        buildingEventSubject = PublishSubject.create();
        when(eventListener.listen("games.3.buildings.*.*", Building.class)).thenReturn(buildingEventSubject);
        stateEventSubject = PublishSubject.create();
        when(eventListener.listen("games.3.state.*", State.class)).thenReturn(stateEventSubject);
        playerEventSubject = PublishSubject.create();
        when(eventListener.listen("games.3.players.*.*", Player.class)).thenReturn(playerEventSubject);
        moveEventSubject = PublishSubject.create();
        when(eventListener.listen("games.3.moves.*.*", Move.class)).thenReturn(moveEventSubject);

        new App(pioneersController).start(stage);
        // Stage width and height change for test (pixel perfect please don't change)
        stage.setWidth(1167);
        stage.setHeight(785);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        pioneersController = null;
        chatController = null;
        chatControllerProvider = null;
        pioneersService = null;
        mapService = null;
        pioneersUIService = null;
        animationService = null;
        buildingEventSubject.onComplete();
        stateEventSubject.onComplete();
        playerEventSubject.onComplete();
        moveEventSubject.onComplete();
    }

    @Test
    void foundingRoll() {
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("founding-roll", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveAction(anyString())).thenReturn(Observable.just(new Move("1", "1", "3", "1", "founding-roll", 8, null, null, null, null)));
        clickOn("#image_dice");

        Event<State> stateEvent1 = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("founding-roll", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent1);
        waitForFxEvents();

        verify(pioneersService).makeMoveAction("founding-roll");
    }

    @Test
    void foundingSettlement() {
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("founding-settlement-1", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "founding-settlement-1", 0, "settlement", null, null, null)));

        clickOn("#selectCircle_0000");
        sleep(400);

        Event<Building> buildEvent = new Event<>(".created", new Building(0, 0, 0, "1234", 0, "settlement", "3", "1"));
        buildingEventSubject.onNext(buildEvent);
        waitForFxEvents();

        verify(pioneersService).makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void foundingRoad() {
        //place building to attach the road to
        Event<Building> buildEvent = new Event<>(".created", new Building(0, 0, 0, "1234", 0, "settlement", "3", "1"));
        buildingEventSubject.onNext(buildEvent);
        waitForFxEvents();

        //set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("founding-road-1", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "founding-road-1", 0, "road", null, null, null)));

        clickOn("#selectCircle_00011");
        sleep(400);

        Event<Building> buildEvent1 = new Event<>(".created", new Building(0, 0, 0, "12345", 11, "road", "3", "1"));
        buildingEventSubject.onNext(buildEvent1);
        waitForFxEvents();

        verify(pioneersService).makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void buildSettlement() {
        pioneersController.setBuildingAllowed(true);

        //enable buttons
        when(pioneersService.checkSettlementResources(any())).thenReturn(true);

        //set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        //set players resources
        Event<Player> playerEvent = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2),
                new RemainingBuildings(2, 1, 4), 2, null, false, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent);
        waitForFxEvents();

        //create road to attach the settlement to
        Event<Building> buildEvent = new Event<>(".created", new Building(0, 0, 0, "12345", 11, "road", "3", "1"));
        buildingEventSubject.onNext(buildEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "founding-settlement-1", 0, "settlement", null, null, null)));

        //toggle build view on and off
        clickOn("#button_settlement");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        waitForFxEvents();
        clickOn("#selectCircle_0000");
        sleep(400);

        Event<Building> buildEvent1 = new Event<>(".created", new Building(0, 0, 0, "1234", 0, "settlement", "3", "1"));
        buildingEventSubject.onNext(buildEvent1);
        waitForFxEvents();

        verify(pioneersService).makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void buildRoad() {
        pioneersController.setBuildingAllowed(true);

        //enable buttons
        when(pioneersService.checkRoadResources(any())).thenReturn(true);

        //set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        //create settlement to attach the road to
        Event<Building> buildEvent = new Event<>(".created", new Building(0, 0, 0, "1234", 0, "settlement", "3", "1"));
        buildingEventSubject.onNext(buildEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "founding-settlement-1", 0, "settlement", null, null, null)));

        //toggle build view on and off
        clickOn("#button_road");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);
        clickOn("#selectCircle_00011");
        sleep(400);

        Event<Building> buildEvent1 = new Event<>(".created", new Building(0, 0, 0, "1234", 11, "road", "3", "1"));
        buildingEventSubject.onNext(buildEvent1);
        waitForFxEvents();

        verify(pioneersService).makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());
    }

    @Test
    void endTurn() {
        //set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveAction(anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "build", 0, null, null, null, null)));

        clickOn("#button_endTurn");
        verify(pioneersService).makeMoveAction(anyString());
    }

    @Test
    void notYourTurn() {
        //set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("2"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        ImageView dice = lookup("#image_dice").query();
        assertEquals(dice.getOpacity(), 0.3f);
        Button settlement = lookup("#button_settlement").queryButton();
        assertEquals(settlement.getOpacity(), 0.3f);
        Button city = lookup("#button_city").queryButton();
        assertEquals(city.getOpacity(), 0.3f);
        Button road = lookup("#button_road").queryButton();
        assertEquals(road.getOpacity(), 0.3f);
        Button endTurn = lookup("#button_endTurn").queryButton();
        assertTrue(endTurn.isDisable());
        Button trade = lookup("#button_trade").queryButton();
        assertTrue(trade.isDisable());
    }

    void robberTestSetup() {
        when(pioneersService.expectedMovesContainsPlayer(any(), any())).thenReturn(true);
        when(pioneersService.expectedMovesContainsAction(List.of(new ExpectedMove("rob", List.of("1"))), Constants.ACTION.DROP)).thenReturn(false);
        when(pioneersService.expectedMovesContainsAction(List.of(new ExpectedMove("rob", List.of("1"))), Constants.ACTION.ROB)).thenReturn(true);
        when(pioneersService.makeMoveRob(any(), any(), any(), any(), any())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "rob", 0, null, new RobDto(0, 0, 0, null), null, null)));

        //Place Opponent Buildings
        buildingEventSubject.onNext(new Event<>(".created", new Building(1, 1, -2, "1234", 0, "settlement", "3", "2")));
        buildingEventSubject.onNext(new Event<>(".created", new Building(1, 1, -2, "1234", 6, "settlement", "3", "3")));

        // Set game state to rob - p1 robs
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("rob", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();
    }

    void robberPostTest(int x, int y, int z) throws InterruptedException {
        //Send new rob status, including the new robber position
        Event<State> stateRobEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), new Point3D(x, y, z)));
        stateEventSubject.onNext(stateRobEvent);
        waitForFxEvents();
        Thread.sleep(1000);
        //Make sure FieldMode is off and robber is at the proper new position
        Circle circle = lookup("#selectCircle_" + x + y + z).query();
        assertFalse(circle.isVisible());
        ImageView building = lookup("#building_" + x + y + z).query();
        assertNotNull(building.getImage());
        building = null;
        circle = null;
    }

    @Test
    void placeRobberNoTarget() throws InterruptedException {

        //Setup test situation
        robberTestSetup();

        //Place robber somewhere
        clickOn("#selectCircle_000");

        //Confirm that the right request was sent
        verify(pioneersService).makeMoveRob(Constants.ACTION.ROB.toString(), 0, 0, 0, null);

        //moveWasDone for robbing is true
        when(pioneersService.moveWasDone(any(), any(), any())).thenReturn(true);

        //Make sure Robber is at the new position and FIELD MODE is OFF.
        robberPostTest(0, 0, 0);
    }

    @Test
    void placeRobberMenuTarget() throws InterruptedException {

        //Setup test situation
        robberTestSetup();

        //Place robber somewhere
        clickOn("#selectCircle_11-2");

        //select victim in menu + make sure its 2
        clickOn("#victimPortrait0");
        clickOn("#victimPortrait1");

        //confirm victim
        clickOn("#checkmark");
        waitForFxEvents();

        //Confirm that the right request was sent
        verify(pioneersService).makeMoveRob(Constants.ACTION.ROB.toString(), 1, 1, -2, "2");

        //Confirm that the menu is gone
        Pane robMenu = lookup("#robMenuRoot").query();
        assertFalse(robMenu.isVisible());
        robMenu = null;

        //moveWasDone for robbing is true
        when(pioneersService.moveWasDone(any(), any(), any())).thenReturn(true);

        //Make sure Robber is at the new position and FIELD MODE is OFF.
        robberPostTest(1, 1, -2);
    }

    @Test
    void buildCity() {
        pioneersController.setBuildingAllowed(true);
        // Enable buttons
        when(pioneersService.checkSettlementResources(any())).thenReturn(true);

        // Set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        // Set players resources
        Event<Player> playerEvent = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2),
                new RemainingBuildings(2, 1, 4), 2, null, false, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent);
        waitForFxEvents();

        // Create road to attach the settlement to
        Event<Building> buildEvent = new Event<>(".created", new Building(0, 0, 0, "12345", 11, "road", "3", "1"));
        buildingEventSubject.onNext(buildEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString())).thenReturn(Observable.just(new Move("123", "1", "3", "1", "founding-settlement-1", 0, "settlement", null, null, null)));

        clickOn("#button_settlement");
        waitForFxEvents();
        clickOn("#selectCircle_0000");
        sleep(400);

        Event<Building> buildEvent1 = new Event<>(".created", new Building(0, 0, 0, "1234", 0, "settlement", "3", "1"));
        buildingEventSubject.onNext(buildEvent1);
        waitForFxEvents();

        verify(pioneersService).makeMoveBuilding(anyString(), anyInt(), anyInt(), anyInt(), anyInt(), anyString());

        // Enable City button
        when(pioneersService.checkCityResources(any())).thenReturn(true);

        // Set players resources
        Event<Player> playerEvent2 = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 3, 2, 2),
                new RemainingBuildings(2, 1, 4), 2, null, false, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent2);
        waitForFxEvents();

        //toggle build view on and off
        clickOn("#button_city");
        type(KeyCode.ENTER);
        type(KeyCode.ENTER);

        clickOn("#building_0000");

        // Websocket trigger for building a city
        Event<Building> buildEvent2 = new Event<>(".updated", new Building(0, 0, 0, "1234", 0, "city", "3", "1"));
        buildingEventSubject.onNext(buildEvent2);
        waitForFxEvents();

        assertEquals(lookup("#player_grid").query().getStyle(), "-fx-border-color: grey; -fx-border-insets: 9; -fx-background-color: FFFFFF24;");
    }

    @Test
    void openCloseTradeMenu() {
        // Set game state
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        clickOn("#button_trade");

        assertTrue(lookup("#tradeMenu").query().isVisible());
        assertFalse(lookup("#buildMenu").query().isVisible());

        clickOn("#image_close");

        assertFalse(lookup("#tradeMenu").query().isVisible());
        assertTrue(lookup("#buildMenu").query().isVisible());

    }

    @Test
    void openCloseTradingPartnerMenu() {

        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        when(pioneersService.makeMoveResources("build", new ResourcesDto(0, 0, 0, -1, 1))).thenReturn(Observable.empty());

        clickOn("#button_trade");
        clickOn("#image_lumber");
        clickOn("#image_wool_gain");
        clickOn("#image_check");

        assertTrue(lookup("#trade_parter").query().isVisible());

        clickOn("#image_close");

        assertFalse(lookup("#tradeMenu").query().isVisible());
    }

    @Test
    void exitGameError() {
        when(pioneersService.setPlayerInactive(anyBoolean())).thenReturn(Observable.just(new Player("", Constants.CUSTOM_ERROR, "", true, 0, null, null, null, null, null)));
        clickOn("#exit_button");
        clickOn(resourceBundle.getString("yes"));
        verify(pioneersService).setPlayerInactive(false);
        assertTrue(lookup("OK").query().isVisible());
    }

    @Test
    void showLongestRoadPopUp() {
        assertFalse(lookup("#road1").query().isVisible());

        when(pioneersService.determineLongestRoad(anyMap())).thenReturn(new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, 5, true, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        Event<Player> playerEvent = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, 5, true, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent);
        waitForFxEvents();

        assertTrue(lookup("#road1").query().isVisible());

        sleep(5000);
    }

    @Test
    void showLargestArmyPopUp() {
        assertFalse(lookup("#knight").query().isVisible());

        when(pioneersService.determineKnights(anyMap(), anyString(), anyBoolean())).thenReturn(new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, 5, false, true, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));

        Event<Player> playerEvent = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, 5, false, true, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent);
        waitForFxEvents();

        Event<Move> moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, new ResourcesDto(1, -1, 0, 0, 0), null, Constants.DEVELOPMENT_CARDS.KNIGHT.toString()));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        assertTrue(lookup("#knight").query().isVisible());

        sleep(5000);
    }

    @Test
    void currentTurn() {

        assertEquals(lookup("#player_grid").query().getStyle(), "-fx-border-color: grey; -fx-border-insets: 9; -fx-background-color: FFFFFF24;");

        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("roll", List.of("2"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();
    }

    @Test
    void showMiniPopUP() {

        assertFalse(lookup("#miniPop").query().isVisible());

        Event<Move> moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, null, null, Constants.DEVELOPMENT_CARDS.KNIGHT.toString()));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        assertTrue(lookup("#miniPop").query().isVisible());

        sleep(3200);

        assertFalse(lookup("#miniPop").query().isVisible());

        moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, null, null, Constants.DEVELOPMENT_CARDS.MONOPOLY.toString()));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        assertTrue(lookup("#miniPop").query().isVisible());

        sleep(3200);

        assertFalse(lookup("#miniPop").query().isVisible());

        moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, null, null, Constants.DEVELOPMENT_CARDS.YEAR_OF_PLENTY.toString()));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        assertTrue(lookup("#miniPop").query().isVisible());

        sleep(3200);

        assertFalse(lookup("#miniPop").query().isVisible());

        moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, null, null, Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString()));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        assertTrue(lookup("#miniPop").query().isVisible());

        sleep(3200);
    }

    @Test
    void openCloseShortcuts() {
        // test if menu can be opened and closed
        clickOn("#book");
        assertTrue(lookup("#keyboardShortcut").query().isVisible());
        clickOn("#book");
        assertFalse(lookup("#keyboardShortcut").query().isVisible());
    }

    @Test
    void sendCounterOffer() {
        Event<Move> moveEvent = new Event<>(".created", new Move("1", "1", "3", "2", "build", 0, null, null, new ResourcesDto(1, -1, 0, 0, 0), null));
        moveEventSubject.onNext(moveEvent);
        waitForFxEvents();

        clickOn("#imageview_edit");
        clickOn("#image_lumber");
        clickOn("#image_brick_gain");

        when(pioneersService.makeMoveResources("offer", new ResourcesDto(-1, 2, 0, -1, 0))).thenReturn(Observable.empty());

        clickOn("#image_check");
        verify(pioneersService).makeMoveResources("offer", new ResourcesDto(-1, 2, 0, -1, 0));
    }

    @Test
    void openCloseDevelopmentCardMenu() {
        // Change state so that the menu button is enabled
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        // Check if the menu open and close if you click the button
        clickOn("#development_menu");
        FxAssert.verifyThat("#developmentCards", NodeMatchers.isVisible());

        clickOn("#development_menu");
        FxAssert.verifyThat("#developmentCards", NodeMatchers.isInvisible());
    }

    @Test
    void checkDevelopmentCardMenuLabel() {
        // Check that the development card label is updated if you get a card
        Event<Player> playerEvent2 = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2),
                new RemainingBuildings(2, 1, 4), 2, 5, false, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true),
                new DevelopmentCard(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString(), false, true))));
        playerEventSubject.onNext(playerEvent2);
        waitForFxEvents();

        FxAssert.verifyThat("#label_development", LabeledMatchers.hasText("2"));
    }

    @Test
    void getDevelopmentCard() {
        pioneersController.setBuildingAllowed(true);
        // Enable buttons
        when(pioneersService.checkDevelopmentResources(any())).thenReturn(true);
        // Change game state to build
        Event<State> stateEvent = new Event<>(".updated", new State("123", "3", List.of(new ExpectedMove("build", List.of("1"))), null));
        stateEventSubject.onNext(stateEvent);
        waitForFxEvents();

        // Update player with enough resources to buy a card
        Event<Player> playerEvent = new Event<>(".updated", new Player("3", "1", "#000000", true, 2, new Resources(0, 2, 2, 2, 2, 2),
                new RemainingBuildings(2, 1, 4), 2, 5, false, false, null, List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, true))));
        playerEventSubject.onNext(playerEvent);
        waitForFxEvents();

        // Mock the make move methods
        when(pioneersService.makeMoveDevelopmentCard(anyString(), anyString())).thenReturn(Observable.empty());

        // Click on button to get a card
        clickOn("#button_development_card");

        // Verify methods
        verify(pioneersService).makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(), Constants.DEVELOPMENT_CARDS.NEW_CARD.toString());
    }
}
