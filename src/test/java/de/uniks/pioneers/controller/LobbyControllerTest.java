package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
class LobbyControllerTest extends ApplicationTest {

    @Mock
    GameService gameService;

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    UserService userService;

    @Mock
    App app;

    @Mock
    EventListener eventListener;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    GameMemberService gameMemberService;

    @Mock
    Provider<ChatController> chatTabControllerProvider;
    @Mock
    MapChooseController mapChooseController;
    @Mock
    ChatController chatController;
    @InjectMocks
    LobbyController lobbyController;
    @Mock
    MapVoteService mapVoteService;
    @Mock
    MapTemplateService mapTemplateService;

    private Subject<Event<Member>> memberEventSubject;
    private Subject<Event<Vote>> voteEventSubject;
    private Subject<Event<Game>> gameEventSubject;

    @Override
    public void start(Stage stage) {
        when(mapVoteService.getAllVotesFromUser(any())).thenReturn(Observable.empty());

        // Return a list of member in game
        when(gameMemberService.findAllGamesMembers()).thenReturn(Observable.just(List.of(new Member("2", "2", "C-317", "42", false, "#000000", false))));
        //Return Login Result
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("2", "2", "42", "JadaMaar", "online", "", null, "", ""));

        // Websocket event always empty
        when(gameService.getStoredGame()).thenReturn(new Game("1", "2", "3", "C-317", "42", 2, false, new GameSettings(2, 10, null)));
        memberEventSubject = PublishSubject.create();
        when(eventListener.listen("games." + "3" + ".members.*.*", Member.class)).thenReturn(memberEventSubject);
        //when(eventListener.listen("games." + "3" + ".*", Game.class)).thenReturn(Observable.empty());
        voteEventSubject = PublishSubject.create();
        when(eventListener.listen("maps.*.votes.*.*", Vote.class)).thenReturn(voteEventSubject);
        gameEventSubject = PublishSubject.create();
        when(eventListener.listen("games." + "3" + ".*", Game.class)).thenReturn(gameEventSubject);

        // Mock chat controller
        when(chatTabControllerProvider.get()).thenReturn(chatController);

        // Return user you want to show on screen
        when(userService.getUser(anyString())).thenReturn(Observable.just((new User("42", "Rick", Constants.STATUS_ONLINE, null, null))));

        new App(lobbyController).start(stage);
        // Stage width and height change for test
        stage.setWidth(500);
        stage.setHeight(500);

        // Verify that method were used
        verify(gameMemberService).findAllGamesMembers();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        lobbyController = null;
        chatController = null;
        chatTabControllerProvider = null;
        mapChooseController = null;
        memberEventSubject.onComplete();
        voteEventSubject.onComplete();
        gameEventSubject.onComplete();
    }

    @Test
    void showLobbyMembers() {
        // Assert that one member is shown on screen
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        assertEquals(gameMembersList.getItems().size(), 1);

        // First get node where there member is saved
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Cast children to assert the values
        ImageView memberAvatar = (ImageView) member.getChildren().get(0);
        Label memberName = (Label) member.getChildren().get(2);
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        Rectangle memberColor = (Rectangle) member.getChildren().get(5);

        // Assert that member is show with the right values
        assertEquals(memberAvatar.getImage().getUrl(), Constants.AVATAR_LIST.get(0));
        assertEquals(memberName.getText(), "Rick");
        assertFalse(memberReadyBox.isSelected());
        assertEquals(memberColor.getFill(), Color.BLACK);

        // Verify that this method used
        verify(userService).getUser("42");
    }

    @Test
    void newGameMemberJoin() {
        // Assert that one member is shown on screen
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        assertEquals(gameMembersList.getItems().size(), 1);

        // Trigger member event so that 2 members are shown on screen
        Event<Member> memberEvent = new Event<>(".created", new Member("3", "3", "C-317", "0815", false, "#000000", false));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        // Assert that two members are shown
        assertEquals(gameMembersList.getItems().size(), 2);
    }

    @Test
    void gameMembersLeavesLobby() {
        // Trigger member event so that two members are shown on screen
        Event<Member> memberEvent = new Event<>(".created", new Member("3", "3", "C-317", "0815", false, "#000000", false));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        // Assert that two members are shown
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        assertEquals(gameMembersList.getItems().size(), 2);

        // Trigger member event so that 1 members leaves the lobby
        Event<Member> leaveMemberEvent = new Event<>(".deleted", new Member("3", "3", "C-317", "0815", false, "#000000", false));
        memberEventSubject.onNext(leaveMemberEvent);
        waitForFxEvents();

        // Assert that only one member is shown on screen
        assertEquals(gameMembersList.getItems().size(), 1);
    }

    @Test
    void gameMemberReady() {
        // Trigger member event when so that user is ready
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("2", "2", "42", "Rick", "online", Constants.AVATAR_LIST.get(0), null, "4", "5"));

        Event<Member> memberEvent = new Event<>(".updated", new Member("2", "2", "C-317", "42", true, "#000000", false));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        // First get node where there member is saved
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Get Checkbox and assert that is it selected
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        assertTrue(memberReadyBox.isSelected());

        Button ready = lookup("#ready_button").query();
        assertEquals(ready.getText(), resourceBundle.getString("unready"));
    }

    @Test
    void gameMemberUnready() {
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("2", "2", "42", "Rick", "online", Constants.AVATAR_LIST.get(0), null, "4", "5"));

        // Trigger member event when so that user is not ready
        Event<Member> memberEvent = new Event<>(".updated", new Member("2", "2", "C-317", "42", false, "#000000", false));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        // First get node where there member is saved
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Get Checkbox and assert that is it selected
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        assertFalse(memberReadyBox.isSelected());

        Button ready = lookup("#ready_button").query();
        assertEquals(ready.getText(), resourceBundle.getString("ready"));
    }

    @Test
    void gameMemberReadyThroughCheckBox() {
        when(gameMemberService.getGameMember()).thenReturn(Observable.just(new Member("2", "2", "C-317", "42", false, "#000000", false)));

        when(gameMemberService.updateMemberReady(anyBoolean(), anyString())).thenReturn(Observable.just(Constants.CHANGE_MEMBER_SHIP_SUCCESS));

        // First get node where there member is saved
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Get Checkbox and assert that is it selected
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        clickOn(memberReadyBox);
        assertTrue(memberReadyBox.isSelected());
    }

    @Test
    void gameMemberUnreadyThroughCheckBox() {
        when(gameMemberService.getGameMember()).thenReturn(Observable.just(new Member("2", "2", "C-317", "42", true, "#000000", false)));

        when(gameMemberService.updateMemberReady(anyBoolean(), anyString())).thenReturn(Observable.just(Constants.CHANGE_MEMBER_SHIP_SUCCESS));

        // First get node where there member is saved
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Get Checkbox and assert that is it selected
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        clickOn(memberReadyBox);
        assertFalse(memberReadyBox.isSelected());
    }

    @Test
    void leaveLobbyAndSetOwnerToNextMember() {
        // Initialize 2 game members
        when(gameMemberService.findAllGamesMembers())
                .thenReturn(Observable.just(List.of(new Member("3", "3", "3", "0815", false, "#000000", false),
                        new Member("3", "3", "3", "42", false, "#000000", false))));

        lobbyController.init();

        // Check that you are the owner of the game
        when(userService.getUserId()).thenReturn("42");

        // Set the new owner of the game
        when(gameService.updateGame(anyString(), anyString(), anyBoolean())).thenReturn(Observable.just(Constants.UPDATE_GAME_SUCCESS));

        // Show alert that you can't exit lobby
        when(gameMemberService.exitLobby()).thenReturn(Observable.just(new Member(Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, Constants.LOBBY_EXIT_ERROR, false, "#000000", false)));

        // Click on exit button to leave the lobby
        clickOn("#exit_button");

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.LOBBY_EXIT_ERROR), LabeledMatchers.hasText(Constants.LOBBY_EXIT_ERROR));

        // Verify that methods were used
        //verify(userService).getUserId();
        verify(gameService).updateGame("C-317", "0815", false);
    }

    @Test
    void leaveLobbyErrorWhileSetOwner() {
        // Initialize 2 game members
        when(gameMemberService.findAllGamesMembers())
                .thenReturn(Observable.just(List.of(new Member("3", "3", "3", "0815", false, "#000000", false),
                        new Member("3", "3", "3", "42", false, "#000000", false))));

        lobbyController.init();

        // Check that you are the owner of the game
        when(userService.getUserId()).thenReturn("42");

        // Set the new owner of the game and get an error
        // Show alert
        when(gameService.updateGame(anyString(), anyString(), anyBoolean())).thenReturn(Observable.just(Constants.UPDATE_GAME_ERROR));

        // Click on exit button to leave the lobby
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.UPDATE_GAME_ERROR), LabeledMatchers.hasText(Constants.UPDATE_GAME_ERROR));
    }

    @Test
    void leaveLobbyErrorWhileGetGameFromServer() {
        // Initialize 2 game members
        when(gameMemberService.findAllGamesMembers())
                .thenReturn(Observable.just(List.of(new Member("3", "3", "3", "0815", false, "#000000", false),
                        new Member("3", "3", "3", "42", false, "#000000", false))));

        lobbyController.init();

        // Get the current game and get an error
        // Show alert
        when(gameService.getStoredGame()).thenReturn(new Game(Constants.JOIN_LOBBY_ERROR, Constants.JOIN_LOBBY_ERROR, Constants.JOIN_LOBBY_ERROR, Constants.JOIN_LOBBY_ERROR,
                Constants.JOIN_LOBBY_ERROR, 0, false, new GameSettings(2, 10, null)));

        // Click on exit button to leave the lobby
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.JOIN_LOBBY_ERROR), LabeledMatchers.hasText(Constants.JOIN_LOBBY_ERROR));
    }

    @Test
    void readyButtonClickedButNoColor() {
        // Initialize when methods
        when(gameMemberService.getGameMember()).thenReturn(Observable.just(new Member("2", "2", "C-317", "42", false, null, false)));

        // Click on ready button to leave the lobby
        clickOn("#ready_button");

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(resourceBundle.getString(Constants.NO_COLOR_ERROR)), LabeledMatchers.hasText(resourceBundle.getString(Constants.NO_COLOR_ERROR)));
    }

    @Test
    void readyButtonClickedUpdateMemberError() {
        // Initialize when methods
        when(gameMemberService.getGameMember()).thenReturn(Observable.just(new Member("2", "2", "C-317", "42", false, "#00000", false)));
        when(gameMemberService.updateMemberReady(anyBoolean(), anyString())).thenReturn(Observable.just(Constants.CHANGE_MEMBER_SHIP_ERROR));

        // Click on ready button to leave the lobby
        clickOn("#ready_button");

        // Check Alert and verify that methods were used
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.CHANGE_MEMBER_SHIP_ERROR), LabeledMatchers.hasText(Constants.CHANGE_MEMBER_SHIP_ERROR));
        verify(gameMemberService).getGameMember();
        verify(gameMemberService).updateMemberReady(true, "#00000");
    }


    @Test
    void beginButtonClickedNotHostError() {
        // Initialize when methods for not host
        when(userService.getUserId()).thenReturn("42");

        //click on begin button to create a game
        clickOn("#begin_button");
        type(KeyCode.ENTER);

        FxAssert.verifyThat("#playerReady", NodeMatchers.isVisible());

    }

    @Test
    void beginButtonClickedNotAllReadyError() {

        // Initialize when methode with the host
        when(userService.getUserId()).thenReturn("C-317");

        //click on begin button to create a game
        clickOn("#begin_button");
        type(KeyCode.ENTER);

        FxAssert.verifyThat("#playerReady", NodeMatchers.isVisible());
    }

    @Test
    void switchToSpectate() {
        when(gameMemberService.updateMemberSpectator(anyBoolean())).thenReturn(Observable.just(Constants.CHANGE_MEMBER_SHIP_SUCCESS));

        clickOn("#spectator_button");

        verify(gameMemberService).updateMemberSpectator(true);
    }

    @Test
    void switchFromSpectate() {
        when(gameMemberService.updateMemberSpectator(anyBoolean())).thenReturn(Observable.just(Constants.CHANGE_MEMBER_SHIP_SUCCESS));

        clickOn("#spectator_button");
        clickOn("#spectator_button");

        verify(gameMemberService).updateMemberSpectator(false);
    }

    @Test
    void spectateReadySymbol() {
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("2", "2", "42", "Rick", "online", Constants.AVATAR_LIST.get(0), null, "4", "5"));

        Event<Member> memberEvent = new Event<>(".updated", new Member("2", "2", "C-317", "42", true, "#000000", true));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        // First get node where there member is saved
        ListView<Node> gameMembersList = lookup("#playerReady").query();
        HBox member = (HBox) gameMembersList.getItems().get(0);

        // Get Checkbox and assert that is it selected
        CheckBox memberReadyBox = (CheckBox) member.getChildren().get(4);
        assertTrue(memberReadyBox.isSelected());
    }

    @Test
    void chooseMapOpen() {
        when(userService.getAllUsers()).thenReturn(Observable.just(List.of(new User("42", "Rick", Constants.STATUS_ONLINE, null, null))));
        when(mapTemplateService.getAllMaps()).thenReturn(Observable.empty());
        clickOn("#button_maps");
        Button button = lookup("#default").queryButton();
        assertTrue(button.isVisible());
    }

    @Test
    void voteMap() {
        // Trigger member event so that two members are shown on screen
        Event<Member> memberEvent = new Event<>(".created", new Member("3", "3", "C-317", "1", false, "#000000", false));
        memberEventSubject.onNext(memberEvent);
        waitForFxEvents();

        when(mapTemplateService.getMap(any())).thenReturn(Observable.just(new MapTemplate("", "", "123", "TestMap", Constants.AVATAR_LIST.get(0), "This is a test map", "1", 100, null, null)));
        when(userService.getUserId()).thenReturn("42");
        when(mapVoteService.deleteMyVote(any())).thenReturn(Observable.empty());
        Event<Game> gameEvent = new Event<>(".updated", new Game("1", "2", "3", "C-317", "1", 2, false, new GameSettings(2, 10, "123")));
        gameEventSubject.onNext(gameEvent);
        waitForFxEvents();
        when(mapVoteService.updateMyVote("123", 1)).thenReturn(Observable.just(new Vote("1", "1", "123", "42", 1)));
        clickOn("#button_like");
        when(mapVoteService.updateMyVote("123", -1)).thenReturn(Observable.just(new Vote("1", "1", "123", "42", -1)));
        clickOn("#button_dislike");
        Event<Vote> voteEvent = new Event<>(".updated", new Vote("1", "2", "123", "42", 1));
        voteEventSubject.onNext(voteEvent);
        waitForFxEvents();
        verify(mapVoteService).updateMyVote("123", 1);

    }
}