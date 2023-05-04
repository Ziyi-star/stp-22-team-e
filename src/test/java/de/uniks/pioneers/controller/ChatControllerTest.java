package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.model.Group;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.Message;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.ChatService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.MusicService;
import de.uniks.pioneers.service.UserService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
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
import org.testfx.matcher.control.TableViewMatchers;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest extends ApplicationTest {

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    ChatService chatService;

    @Mock
    UserService userService;

    @Mock
    EventListener eventListener;

    @Mock
    MusicService musicService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Spy
    Provider<OnlineUserSubController> onlineUserSubControllerProvider;

    @InjectMocks
    OnlineUserSubController onlineUserSubController;

    @InjectMocks
    ChatController chatController;

    private Subject<Event<Message>> messageEventSubject;

    @Override
    public void start(Stage stage) {
        when(chatService.getGroups(any())).thenReturn(Observable.just(List.of()));

        // Websocket messages and users return always empty
        when(eventListener.listen("users.*.*", User.class)).thenReturn(Observable.empty());

        // Create message event subject for event listener
        messageEventSubject = PublishSubject.create();
        when(eventListener.listen("groups.*." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(messageEventSubject);

        // Get chat controller from provider
        when(onlineUserSubControllerProvider.get()).thenReturn(onlineUserSubController);

        // Initialize one online user for list
        when(userService.findAllOnlineUsers()).thenReturn(Observable.just(List.of(new User("42", "Rick", "online", null, null))));

        new App(chatController).start(stage);
        // Stage width and height change for test
        stage.setWidth(350);
        stage.setHeight(610);
        verify(userService).findAllOnlineUsers();
        verify(chatService).getGroups(any());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        onlineUserSubController = null;
        onlineUserSubControllerProvider = null;
        chatController = null;
        messageEventSubject.onComplete();
    }

    @Test
    void openTabWithOldGroup() {
        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // Make a member list and add it to group
        List<String> groupList = new ArrayList<>();
        groupList.add("123");
        groupList.add("42");

        // Initialize methods that where used after you double-click on table view
        when(eventListener.listen("groups" + "." + "123" + "." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(Observable.empty());

        // Get a group with you and your chat partner
        when(chatService.getSpecificGroups(anyString())).thenReturn(Observable.just(List.of(new Group("3", "3", "123", "Rick:Rainer", groupList))));

        // Load empty list of messages in private chat controller
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of()));

        // Get your user data from login result storage
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "custom_id", "Rainer", "5", "6", null, "7", "8"));

        // Get your chat partner from server
        when(userService.getUser(anyString())).thenReturn(Observable.just(new User("42", "Rick", "online", null, null)));

        // Get focus cell and first element from table view if you double-click on it
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        // Verify that methods were used
        verify(chatService).getSpecificGroups(anyString());
        verify(chatService).getAllMessages(anyString(), anyString());
        verify(userService).getUser("42");

        // Assert that tab pane has 2 tabs, has your chat partners name and group id
        assertEquals(tabPane.getTabs().size(), 2);
        assertEquals(tabPane.getTabs().get(1).getText(), "Rick");
        assertEquals(tabPane.getTabs().get(1).getId(), "123");
    }

    @Test
    void openTabWithNewGroup() {
        // Check that user list has a user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Assert that tab pane has only 1 tab
        assertEquals(tabPane.getTabs().size(), 1);

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // Make a member list and add it to group
        List<String> groupList = new ArrayList<>();
        groupList.add("123");
        groupList.add("42");

        // Initialize methods that where used after you double-click on table view
        when(eventListener.listen("groups" + "." + "123" + "." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(Observable.empty());
        // Get an empty list of groups with your chat partner
        when(chatService.getSpecificGroups(anyString())).thenReturn(Observable.just(List.of()));

        // Create a new group with your chat partner
        when(chatService.createGroup(anyString(), anyString())).thenReturn(Observable.just(new Group("3", "3", "123", "Rick:Rainer", groupList)));

        // Load empty list of messages in private chat controller
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of()));

        // Get your user data from login result storage
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "custom_id", "Rainer", "5", "6", null, "7", "8"));

        // Get your chat partner from server
        when(userService.getUser(anyString())).thenReturn(Observable.just(new User("42", "Rick", "online", null, null)));

        // Get focus cell and first element from table view if you double-click on it
        // Open a chat tab with user where you already have a chat group
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        // Verify that methods were used
        verify(chatService).getSpecificGroups(anyString());
        verify(chatService).getAllMessages(anyString(), anyString());
        verify(chatService).createGroup("Rick", "42");
        verify(userService).getUser("42");

        // Assert that tab pane has 2 tabs, has your chat partners name and group id
        assertEquals(tabPane.getTabs().size(), 2);
        assertEquals(tabPane.getTabs().get(1).getText(), "Rick");
        assertEquals(tabPane.getTabs().get(1).getId(), "123");
    }

    @Test
    void openWithSelfNameFailure() {
        // Check that user list has a user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Assert that tab pane has only 1 tab
        assertEquals(tabPane.getTabs().size(), 1);

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // check if the user is the owner or not
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "42", "Rick", "online", "6", null, "7", "8"));

        // Get focus cell and first element from table view
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        // Assert that tab pane has 1 tabs ,nothing open
        assertEquals(tabPane.getTabs().size(), 1);
    }

    @Test
    void openTabAfterGetMessage() {
        // Check that user list has a user that was loaded with user service
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Assert that tab pane has only 1 tab
        assertEquals(tabPane.getTabs().size(), 1);

        // Make a member list and add it to group
        List<String> groupList = new ArrayList<>();
        groupList.add("123");
        groupList.add("42");

        // Return group for tab to open
        when(chatService.getGroup(anyString())).thenReturn(Observable.just(new Group("5", "4", "42", "Rick:Rainer", groupList)));

        // Load empty list of messages in private chat controller
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of()));

        // Get your user data from login result storage
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "custom_id", "Rick", "5", "6", null, "7", "8"));

        // Get your chat partner from server
        when(userService.getUser(anyString())).thenReturn(Observable.just(new User("42", "Rainer", "online", null, null)));

        // Trigger websocket event with new message event
        when(eventListener.listen("groups" + "." + "42" + "." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(Observable.empty());
        Event<Message> messageEvent = new Event<>("123.42.created", (new Message("5", "3", "4", "Rainer", "Hallo Rick")));
        messageEventSubject.onNext(messageEvent);
        waitForFxEvents();

        // Verify methods
        verify(chatService).getGroup("42");
        verify(userService).getUser("42");

        // Assert that tab pane has 2 tabs, has your chat partners name and group id
        assertEquals(tabPane.getTabs().size(), 2);
        assertEquals(tabPane.getTabs().get(1).getText(), "Rainer");
        assertEquals(tabPane.getTabs().get(1).getId(), "42");
    }
}