package de.uniks.pioneers.controller;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.ChatService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.MusicService;
import de.uniks.pioneers.service.UserService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
import java.util.Map;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class MessageSubControllerTest extends ApplicationTest {
    @Spy
    LoginResultStorage loginResultStorage;
    @Spy
    Provider<OnlineUserSubController> onlineUserSubControllerProvider;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    ChatService chatService;

    @Mock
    UserService userService;

    @Mock
    EventListener eventListener;

    @Mock
    MusicService musicService;

    @InjectMocks
    OnlineUserSubController onlineUserSubController;

    @InjectMocks
    ChatController chatController;

    private Subject<Event<Message>> lobbyMessageEventSubject;

    private Subject<Event<Message>> privateMessageEventSubject;

    private Map<String, User> membersToUI = new HashMap<>();

    @Override
    public void start(Stage stage) {
        // Create an empty group list
        when(chatService.getGroups(any())).thenReturn(Observable.just(List.of()));

        // Websocket return always empty
        // Event listener online user sub controller
        when(eventListener.listen("users.*.*", User.class)).thenReturn(Observable.empty());

        // Event listener chat controller
        when(eventListener.listen("groups.*." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(Observable.empty());

        // Get chat controller from provider
        when(onlineUserSubControllerProvider.get()).thenReturn(onlineUserSubController);

        // Initialize one online user for list
        when(userService.findAllOnlineUsers()).thenReturn(Observable.just(List.of(new User("2", "Rick", "online", null, null))));

        // Create hash map and open lobby chat
        User user = new User("2", "RainerW", "online", null, null);
        membersToUI.put("0815", user);

        when(chatService.getLoginResultStorage()).thenReturn(loginResultStorage);
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "sender_id", "Rainer", "5", "6", null, "7", "8"));

        new App(chatController).start(stage);
        // Stage width and height change for test
        stage.setWidth(350);    //350
        stage.setHeight(610);   //610

        // Create lobby message event subject for lobby event listener
        lobbyMessageEventSubject = PublishSubject.create();
        when(eventListener.listen(Constants.GAMES + "." + "42" + "." + Constants.MESSAGES + ".*.*", Message.class)).thenReturn(lobbyMessageEventSubject);

        // Open lobby chat
        chatController.createLobbyController("42", membersToUI);

        verify(userService).findAllOnlineUsers();
        verify(chatService).getGroups(any());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        onlineUserSubController = null;
        chatController = null;
        onlineUserSubControllerProvider = null;
        lobbyMessageEventSubject.onComplete();
        if (privateMessageEventSubject != null) privateMessageEventSubject.onComplete();
        membersToUI = null;
    }

    @Test
    void showPrivateMessages() {
        // Check that user list has a user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Change back to online user tab
        tabPane.getSelectionModel().select(0);

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // Make a group (members) and return if create method is called
        List<String> groupList = new ArrayList<>();
        groupList.add("135");
        groupList.add("246");
        when(chatService.getSpecificGroups(anyString())).thenReturn(Observable.just(List.of()));
        when(chatService.createGroup(anyString(), anyString())).thenReturn(Observable.just(
                new Group("3", "3", "group_id", "Rick:Rainer", groupList)));

        // create message list
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of(
                new Message("12", "2022-06-13T09:17:08.001Z", "123456", "sender_id", "Hallo"),
                new Message("13", "2022-06-13T09:17:08.001Z", "123457", "sender_id", "Joe"),
                new Message("14", "2022-06-13T09:17:08.001Z", "123458", "not_sender_id",
                        "EinsZweiDreiVierFuenfSechsSiebenAchtNeun ZehnElf ZwoelfDreizehnVierzehnFuenfzehn Sechzehn"),
                new Message("15", "2022-06-13T09:17:08.001Z", "123459", "not_sender_id",
                        "0123456789012345678901234567890123456789012345678901234567890123456789"),
                new Message("16", "2022-06-13T09:17:08.001Z", "123460", "not_sender_id",
                        "012345678901234567890123456789012345678901234567890123456789"))));

        // specify the result values
        when(chatService.getLoginResultStorage()).thenReturn(loginResultStorage);
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult(
                "1", "2", "sender_id", "Rainer", "5", "6", null, "7", "8"));

        when(userService.getUser(anyString())).thenReturn(Observable.just(
                new User("42", "Rick", "online", null, null)));

        // Event listener message sub controller groups
        when(eventListener.listen("groups" + "." + "group_id" + "." + Constants.MESSAGES + ".*.*", Message.class))
                .thenReturn(Observable.empty());

        // Get focus cell and first element from table view
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        ScrollPane scrollPane = (ScrollPane) tabPane.getTabs().get(2).getContent();

        VBox messageBox = (VBox) scrollPane.getContent();
        HBox hBox1 = (HBox) messageBox.getChildren().get(0);
        HBox hBox2 = (HBox) messageBox.getChildren().get(1);
        HBox hBox3 = (HBox) messageBox.getChildren().get(2);
        HBox hBox4 = (HBox) messageBox.getChildren().get(3);
        HBox hBox5 = (HBox) messageBox.getChildren().get(4);

        // own message
        Bubble messageBubble1 = (Bubble) hBox1.getChildren().get(1);
        //Label messageLabel1 = (Label) messageBubble1.getChildren().get(1);
        EmojiTextFlow messageLabel1 = (EmojiTextFlow) messageBubble1.getChildren().get(1);

        // own message
        Bubble messageBubble2 = (Bubble) hBox2.getChildren().get(1);
        EmojiTextFlow messageLabel2 = (EmojiTextFlow) messageBubble2.getChildren().get(1);

        // partner message, only message bubble, no delete button
        Bubble messageBubble3 = (Bubble) hBox3.getChildren().get(0);
        EmojiTextFlow messageLabel3 = (EmojiTextFlow) messageBubble3.getChildren().get(1);

        // partner message, only message bubble, no delete button
        Bubble messageBubble4 = (Bubble) hBox4.getChildren().get(0);
        EmojiTextFlow messageLabel4 = (EmojiTextFlow) messageBubble4.getChildren().get(1);

        // partner message, only message bubble, no delete button
        Bubble messageBubble5 = (Bubble) hBox5.getChildren().get(0);
        EmojiTextFlow messageLabel5 = (EmojiTextFlow) messageBubble5.getChildren().get(1);

        // Verify that methods were used
        verify(chatService).getSpecificGroups(anyString());
        verify(chatService).getAllMessages(anyString(), anyString());
        verify(chatService).createGroup(anyString(), anyString());
        verify(userService).getUser(anyString());

        // Assert that Labels in hbox (messages) have correct texts
        assertEquals("Hallo", ((Text) messageLabel1.getChildren().get(0)).getText());
        assertEquals("Joe", ((Text) messageLabel2.getChildren().get(0)).getText());

        // Assert that text was formatted in the right way
        assertEquals("EinsZweiDreiVierFu\nenfSechsSiebenAcht\nNeun ZehnElf\nZwoelfDreizehnVier\nzehnFuenfzehn\nSechzehn",
                ((Text) messageLabel3.getChildren().get(0)).getText());

        assertEquals("012345678901234567\n890123456789012345\n678901234567890123\n4567890123456789",
                ((Text) messageLabel4.getChildren().get(0)).getText());

        assertEquals("012345678901234567\n890123456789012345\n678901234567890123\n456789",
                ((Text)messageLabel5.getChildren().get(0)).getText());

        //assertEquals(5, hBox1.getItems().size());
        assertEquals(5, messageBox.getChildren().size());
        assertEquals(3, tabPane.getTabs().size());
        assertEquals(tabPane.getTabs().get(2).getText(), "Rick");
        assertEquals(tabPane.getTabs().get(2).getId(), "group_id");
    }

    @Test
    void sendMessage() {
        // Check that user list has a user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();

        // Change back to online user tab
        tabPane.getSelectionModel().select(0);

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // Make a group (members) and return if create method is called
        List<String> groupList = new ArrayList<>();
        groupList.add("135");
        groupList.add("246");
        when(chatService.getSpecificGroups(anyString())).thenReturn(Observable.just(List.of()));
        when(chatService.createGroup(anyString(), anyString())).thenReturn(Observable.just(new Group(
                "3", "3", "group_id", "Rick:Rainer", groupList)));
        when(chatService.sendMessage(anyString(), anyString(), anyString())).thenReturn(Observable.just(new Message(
                "13", "2022-06-13T09:17:08.001Z", "123457", "sender_id", "Wie geht es")));

        // create message list
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of(
                new Message("12", "2022-06-13T09:17:08.001Z", "123456", "sender_id", "Hallo"),
                new Message("13", "2022-06-13T09:17:08.001Z", "123457", "sender_id", "Joe"))));

        // specify the result values
        when(chatService.getLoginResultStorage()).thenReturn(loginResultStorage);

        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult(
                "1", "2", "sender_id", "Rainer", "5", "6", null, "7", "8"));

        when(userService.getUser(anyString())).thenReturn(Observable.just(
                new User("42", "Rick", "online", null, null)));

        // Event listener message sub controller groups
        privateMessageEventSubject = PublishSubject.create();
        when(eventListener.listen("groups" + "." + "group_id" + "." + Constants.MESSAGES + ".*.*", Message.class))
                .thenReturn(privateMessageEventSubject);

        // Get focus cell and first element from table view
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        // type new message und press send
        write("Wie geht es");
        type(KeyCode.ENTER);

        // Trigger websocket event with message
        Event<Message> messageEvent = new Event<>(".created", (new Message(
                "5", "2022-06-13T09:17:08.001Z", "4", "send_id", "Wie geht es")));

        privateMessageEventSubject.onNext(messageEvent);
        waitForFxEvents();

        // Get messages from list view
        ScrollPane scrollPane = (ScrollPane) tabPane.getTabs().get(2).getContent();

        VBox messageBox = (VBox) scrollPane.getContent();
        HBox hBox1 = (HBox) messageBox.getChildren().get(0);
        HBox hBox2 = (HBox) messageBox.getChildren().get(1);
        HBox hBox3 = (HBox) messageBox.getChildren().get(2);

        // Verify that methods were used
        verify(chatService).getSpecificGroups(anyString());
        verify(chatService).getAllMessages(anyString(), anyString());
        verify(chatService).createGroup(anyString(), anyString());
        verify(userService).getUser(anyString());
        verify(chatService).sendMessage("Wie geht es", "group_id", Constants.GROUPS);

        // Message HBox has bubble inside, bubble has message label in it
        // Assert that message bubble labels have correct texts
        // own message
        Bubble messageBubble1 = (Bubble) hBox1.getChildren().get(1);
        EmojiTextFlow messageLabel1 = (EmojiTextFlow) messageBubble1.getChildren().get(1);

        // own message
        Bubble messageBubble2 = (Bubble) hBox2.getChildren().get(1);
        EmojiTextFlow messageLabel2 = (EmojiTextFlow) messageBubble2.getChildren().get(1);

        // partner message, only message bubble, no delete button
        Bubble messageBubble3 = (Bubble) hBox3.getChildren().get(0);
        EmojiTextFlow messageLabel3 = (EmojiTextFlow) messageBubble3.getChildren().get(1);

        assertEquals("Hallo", ((Text) messageLabel1.getChildren().get(0)).getText());
        assertEquals("Joe", ((Text) messageLabel2.getChildren().get(0)).getText());
        assertEquals("Wie geht es", ((Text) messageLabel3.getChildren().get(0)).getText());

        assertEquals(3, messageBox.getChildren().size());
        assertEquals(tabPane.getTabs().size(), 3);
        assertEquals(tabPane.getTabs().get(2).getText(), "Rick");
        assertEquals(tabPane.getTabs().get(2).getId(), "group_id");
    }

    @Test
    void deletedMessage() {
        // Check that user list has a user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Rick"));

        // Initialize objects for test
        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();
        tabPane.getSelectionModel().select(0);

        // Set focus on table view first cell
        write("\t");
        type(KeyCode.SPACE);

        // Make a group (members) and return if create method is called
        List<String> groupList = new ArrayList<>();
        groupList.add("135");
        groupList.add("246");
        when(chatService.getSpecificGroups(anyString())).thenReturn(Observable.just(List.of()));
        when(chatService.createGroup(anyString(), anyString())).thenReturn(Observable.just(new Group(
                "3", "3", "group_id", "Rick:Rainer", groupList)));

        // create message list
        when(chatService.getAllMessages(anyString(), anyString())).thenReturn(Observable.just(List.of(
                new Message("12", "2022-06-13T09:17:08.001Z", "123456", "sender_id", "Hallo"),
                new Message("13", "2022-06-13T09:17:08.001Z", "123457", "sender_id", "Joe"))));

        //delete message
        when(chatService.deleteMessage(anyString(), anyString(), anyString())).thenReturn(Observable.just(
                new Message("15", "2022-06-13T09:17:08.001Z", "123456", "sender_id", "Wie geht es")));

        // specify the result values
        when(chatService.getLoginResultStorage()).thenReturn(loginResultStorage);
        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult(
                "1", "2", "sender_id", "Rainer", "5", "6", null, "7", "8"));
        when(userService.getUser(anyString())).thenReturn(Observable.just(
                new User("42", "Rick", "online", null, null)));

        // Event listener message sub controller groups
        privateMessageEventSubject = PublishSubject.create();
        when(eventListener.listen("groups" + "." + "group_id" + "." + Constants.MESSAGES + ".*.*", Message.class))
                .thenReturn(privateMessageEventSubject);

        // Get focus cell and first element from table view
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        ScrollPane scrollPane = (ScrollPane) tabPane.getTabs().get(2).getContent();

        // VBox contains HBoxes for each message
        VBox messageBox = (VBox) scrollPane.getContent();
        HBox hBox1 = (HBox) messageBox.getChildren().get(0);
        HBox hBox2 = (HBox) messageBox.getChildren().get(1);

        // Message HBox has bubble inside, bubble has message label in it
        Bubble messageBubble1 = (Bubble) hBox1.getChildren().get(1);
        EmojiTextFlow messageLabel1 = (EmojiTextFlow) messageBubble1.getChildren().get(1);

        // click on message bubble for delete button
        clickOn(messageBubble1);
        waitForFxEvents();

        Bubble messageBubble2 = (Bubble) hBox2.getChildren().get(1);
        EmojiTextFlow messageLabel2 = (EmojiTextFlow) messageBubble2.getChildren().get(1);

        // Assert that Labels in bubble (messages) have correct texts and VBox has 2 items
        assertEquals("Hallo", ((Text) messageLabel1.getChildren().get(0)).getText());
        assertEquals("Joe", ((Text) messageLabel2.getChildren().get(0)).getText());
        assertEquals(2, messageBox.getChildren().size());

        //use button deleted the first message
        // HBox switched because of click, have to get again
        HBox switchedHBox = (HBox) messageBox.getChildren().get(0);
        ImageView button = (ImageView) switchedHBox.getChildren().get(1);
        clickOn(button);

        // Trigger websocket and to delete the messages
        Event<Message> messageEvent = new Event<>(".deleted", (new Message(
                "12", "2022-06-13T09:17:08.001Z", "123456", "sender_id", "Hallo")));
        privateMessageEventSubject.onNext(messageEvent);
        waitForFxEvents();

        // Verify that methods were used
        verify(chatService).getSpecificGroups(anyString());
        verify(chatService).getAllMessages(anyString(), anyString());
        verify(chatService).createGroup(anyString(), anyString());
        verify(userService).getUser(anyString());
        verify(chatService).deleteMessage("group_id", "123456", Constants.GROUPS);

        // Assert content after deleted message
        assertEquals(1, messageBox.getChildren().size());
        assertEquals(tabPane.getTabs().size(), 3);
        assertEquals(tabPane.getTabs().get(2).getText(), "Rick");
        assertEquals(tabPane.getTabs().get(2).getId(), "group_id");
    }

    @Test
    void showLobbyMessageWithName() {
        // Trigger websocket event for lobby event listener
        Event<Message> messageEvent = new Event<>("games.42.123.42.created", (new Message(
                "5", "2022-06-13T09:17:08.001Z", "4", "0815", "Hallo Rick")));

        lobbyMessageEventSubject.onNext(messageEvent);
        waitForFxEvents();

        // Get tab pane and lobby chat at position 1
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();
        // Scroll pane contains message box
        ScrollPane scrollPane = (ScrollPane) tabPane.getTabs().get(1).getContent();

        // VBox contains HBoxes for each message
        VBox messageBox = (VBox) scrollPane.getContent();
        HBox hBox1 = (HBox) messageBox.getChildren().get(0);

        // Message HBox has bubble inside, bubble has message label in it
        Bubble messageBubble = (Bubble) hBox1.getChildren().get(0);
        Label nameLabel = (Label) messageBubble.getChildren().get(1);
        EmojiTextFlow messageLabel = (EmojiTextFlow) messageBubble.getChildren().get(2);
        Label messageTime = (Label) messageBubble.getChildren().get(3);

        // Assert that expected text and message label from bubble are the same
        assertEquals("RainerW", nameLabel.getText());
        // Assert that expected time and messageTime label from bubble are the same
        assertEquals("\nHallo Rick", ((Text) messageLabel.getChildren().get(0)).getText());
        assertEquals("09:17", messageTime.getText());
    }


}