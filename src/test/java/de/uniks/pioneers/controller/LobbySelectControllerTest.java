package de.uniks.pioneers.controller;


import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.model.Game;
import de.uniks.pioneers.model.GameSettings;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
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
import org.testfx.matcher.control.TableViewMatchers;
import org.testfx.matcher.control.TextInputControlMatchers;

import javax.inject.Provider;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class LobbySelectControllerTest extends ApplicationTest {


    @Mock
    App app;

    @Mock
    UserService userService;

    @Mock
    GameService gameService;

    @Mock
    ChatService chatService;

    @Mock
    AuthenticationService authenticationService;

    @Mock
    GameMemberService gameMemberService;

    @Mock
    EventListener eventListener;

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    PrefService prefService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    TimerService timerService;

    @Mock
    Provider<ChatController> chatTabControllerProvider;
    @Mock
    ChatController chatController;
    @Mock
    MusicService musicService;
    @InjectMocks
    LobbySelectController lobbySelectController;

    private PublishSubject<Event<Game>> gameEventSubject;

    private PublishSubject<Event<User>> userEventSubject;

    @Override
    public void start(Stage stage) {
        // Get online users
        when(userService.findAllOnlineUsers()).thenReturn(Observable.just(List.of(new User("2", "Rick", "online", null, null), new User("3", "Leo", "online", null, null))));

        // Websocket event always empty
        userEventSubject = PublishSubject.create();

        when(eventListener.listen("users.*.*", User.class)).thenReturn(userEventSubject);

        // Get a game that you want to show in lobby list
        when(gameService.findAllGames()).thenReturn(Observable.just(List.of(new Game("2", "2", "1", "Lobby", "3", 1, false, null))));

        // Create games subject
        gameEventSubject = PublishSubject.create();
        when(eventListener.listen("games.*.*", Game.class)).thenReturn(gameEventSubject);

        when(chatTabControllerProvider.get()).thenReturn(chatController);

        new App(lobbySelectController).start(stage);
        // Stage width and height change for test
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        lobbySelectController = null;
        chatController = null;
        chatTabControllerProvider = null;
        userEventSubject.onComplete();
        gameEventSubject.onComplete();
    }

    @Test
    void show() {
        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));

        verify(userService).findAllOnlineUsers();
        verify(gameService).findAllGames();

    }

    @Test
    void createdGameShownInList() {
        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(1));

        // Trigger user event so a new user is online
        Event<User> userEvent = new Event<>(".updated", (new User("42", "Rick", "online", null, null)));
        userEventSubject.onNext(userEvent);
        waitForFxEvents();

        // Trigger game event and assert that is shown in list
        Event<Game> gameEvent = new Event<>(".created", new Game("2", "2", "42", "Rick", "42", 1, false, null));
        gameEventSubject.onNext(gameEvent);
        waitForFxEvents();

        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Rick", "Rick"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(2));
    }

    @Test
    void gameStartedNotShownInList() {
        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(1));

        // Trigger game event and assert that is not shown in list
        Event<Game> gameEvent = new Event<>(".updated", new Game("2", "2", "1", "Lobby", "3", 1, true, null));
        gameEventSubject.onNext(gameEvent);
        waitForFxEvents();

        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(0));
    }

    @Test
    void deletedGameNotShownInList() {
        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(1));

        // Trigger game event and assert that is not shown in list
        Event<Game> gameEvent = new Event<>(".deleted", new Game("2", "2", "1", "Lobby", "3", 1, true, null));
        gameEventSubject.onNext(gameEvent);
        waitForFxEvents();

        FxAssert.verifyThat("#lobby_list", TableViewMatchers.hasNumRows(0));
    }

    @Test
    void openProfileSettings() {
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "3", "Rick", "online", Constants.AVATAR_LIST.get(0), null, "4", "5"));

        // Open Profile settings menu
        clickOn("#imageview_settings");

        // Verify that settings are open
        FxAssert.verifyThat("#btn_apply", NodeMatchers.isVisible());

        // Close profile settings without alert

        clickOn("#btn_apply");
    }

    @Test
    void errorWhileCreatingLobby() {
        // Try to create a game and get an error
        when(gameService.createdLobby(anyString(), anyString(), any())).thenReturn(Observable.just(Constants.CUSTOM_ERROR));

        // Get to create lobby button
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Create a lobby
        write("Rickerus\t");
        write("123\t");
        clickOn("#create_button");

        // Verify that error label has text and dialog is open
        FxAssert.verifyThat("#error_Label", LabeledMatchers.hasText(Constants.CUSTOM_ERROR));
        FxAssert.verifyThat("#cancel_button", NodeMatchers.isVisible());

        // Close dialog
        write("\t");
        type(KeyCode.ENTER);

        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
    }

    @Test
    void errorWhileJoiningLobby() {
        // Get the current game
        when(gameService.getStoredGame()).thenReturn(new Game("2", "2", "1", "Lobby", "3", 1, false, null));

        // Try to join a game and get an error
        when(gameMemberService.joinLobby(anyString(), anyString())).thenReturn(Observable.just(Constants.JOIN_LOBBY_ERROR));

        // Get focus on the first cell in game lobby list
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.SPACE);

        // Go to join button and enter the password to join the game
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);
        write("a\t");
        type(KeyCode.ENTER);

        // Verify that error label has text and dialog is open
        FxAssert.verifyThat("#LobbyJoin_Password_Incorrect", LabeledMatchers.hasText(Constants.JOIN_LOBBY_ERROR));
        FxAssert.verifyThat("#joinLobbyCancel_button", NodeMatchers.isVisible());

        // Close dialog
        write("\t");
        type(KeyCode.ENTER);

        // Check that user lobby list has a lobby
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));
    }

    @Test
    void errorWhileLogOut() {
        // Try to log out and get an error
        when(authenticationService.logout()).thenReturn(Observable.just(Constants.LOGOUT_ERROR));

        // Get to log out button
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Assert alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.LOGOUT_ERROR), LabeledMatchers.hasText(Constants.LOGOUT_ERROR));

        // Click on ok and check lobby list
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));

        // Verify that method was used
        verify(authenticationService).logout();
    }

    @Test
    void errorWhileChangeStatus() {
        // Log out
        when(authenticationService.logout()).thenReturn(Observable.just(Constants.LOGOUT_SUCCESS));

        // Try to change your status and get an error
        when(userService.setUserOffline()).thenReturn(Observable.just(Constants.CUSTOM_ERROR));

        // Get to log out button
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Assert alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(Constants.CUSTOM_ERROR), LabeledMatchers.hasText(Constants.CUSTOM_ERROR));

        // Click on ok and check lobby list
        type(KeyCode.ENTER);
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("Lobby", "Leo"));

        // Verify that method was used
        verify(userService).setUserOffline();
    }

    @Test
    void createLobby() {
        clickOn("#createLobby_button");
        clickOn("#lobbyName_field");
        write("test");
        clickOn("#lobbyPassword_field");
        write("test");
        FxAssert.verifyThat(lookup("#map_size_field"), TextInputControlMatchers.hasText("2"));
        FxAssert.verifyThat(lookup("#victory_points_field"), TextInputControlMatchers.hasText("10"));

        clickOn("#map_size_slider");

        clickOn("#victory_point_slider");

        clickOn("#start_resources");
        write("1");

        clickOn("#roll7");

        FxAssert.verifyThat(lookup("#map_size_field"), TextInputControlMatchers.hasText("5"));
        FxAssert.verifyThat(lookup("#victory_points_field"), TextInputControlMatchers.hasText("9"));
        FxAssert.verifyThat(lookup("#start_resources"), TextInputControlMatchers.hasText("10"));


        TextField textField = lookup("#map_size_field").query();
        TextField victoryPointsField = lookup("#victory_points_field").query();
        TextField field = lookup("#start_resources").query();
        CheckBox box = lookup("#roll7").query();

        assertFalse(box.isSelected());

        int mapRadius = Integer.parseInt(textField.getText());
        int victoryPoints = Integer.parseInt(victoryPointsField.getText());
        int resources = Integer.parseInt(field.getText());

        when(gameService.createdLobby(anyString(), anyString(), any())).thenReturn(Observable.just(""));
        clickOn("#create_button");
        verify(gameService).createdLobby("test", "test", new GameSettings(mapRadius, victoryPoints, null, box.isSelected(), resources));
    }
}