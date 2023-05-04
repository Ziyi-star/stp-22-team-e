package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.model.Message;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.ChatService;
import de.uniks.pioneers.service.UserService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TableViewMatchers;

import javax.inject.Provider;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class OnlineUserSubControllerTest extends ApplicationTest {
    @Mock
    ChatService chatService;
    @Mock
    UserService userService;
    @Mock
    EventListener eventListener;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");
    @Spy
    Provider<OnlineUserSubController> onlineUserSubControllerProvider;
    @InjectMocks
    OnlineUserSubController onlineUserSubController;
    @InjectMocks
    ChatController chatController;
    private Subject<Event<User>> userEventSubject;

    @Override
    public void start(Stage stage) {
        // Load empty groups list
        when(chatService.getGroups(any())).thenReturn(Observable.just(List.of()));

        // Return empty observable in chat controller messages event listener
        // Not needed for this tests
        when(eventListener.listen("groups.*." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(Observable.empty());

        // Get chat controller from provider and initialize one online user for list
        when(onlineUserSubControllerProvider.get()).thenReturn(onlineUserSubController);
        when(userService.findAllOnlineUsers()).thenReturn(Observable.just(List.of(new User("42", "Rick", "online", null, null))));

        // Create user event subject for event listener
        userEventSubject = PublishSubject.create();
        when(eventListener.listen("users.*.*", User.class)).thenReturn(userEventSubject);

        new App(chatController).start(stage);
        // Stage width and height change for test
        stage.setWidth(350);
        stage.setHeight(610);

        verify(userService).findAllOnlineUsers();
        verify(chatService).getGroups(null);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        chatController = null;
        onlineUserSubController = null;
        onlineUserSubControllerProvider = null;
        userEventSubject.onComplete();
    }

    @Test
    void showOnlineUser() {
        // Verify that user list has a user that was loaded with user service
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.hasNumRows(1));

        // Initialize objects for test
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Assert that tab pane has only 1 tab
        assertEquals(tabPane.getTabs().size(), 1);
    }

    @Test
    void userLogInStatusOnline() {
        // Show user in online list after he logged in and his status goes online
        // Verify that user list has a user that was loaded with user service
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.hasNumRows(1));

        // Create user event for websocket and trigger it
        // User goes online
        Event<User> userEvent = new Event<>(".updated", (new User("3", "Morty", "online", null, null)));
        userEventSubject.onNext(userEvent);
        waitForFxEvents();

        // Verify that online user list has now 2 user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.hasNumRows(2));
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Morty"));
    }

    @Test
    void userLogOutStatusOffline() {
        // User is not shown in online list after he logged out and his status goes offline
        // Verify that user list has a user that was loaded with user service
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.hasNumRows(1));

        // Create user event for websocket and trigger it
        // User goes offline
        Event<User> userEvent = new Event<>(".updated", (new User("42", "Rick", "offline", null, null)));
        userEventSubject.onNext(userEvent);
        waitForFxEvents();

        // Verify that user list has no user online user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.hasNumRows(0));
    }
}