package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Group;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.model.Message;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.ChatService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.MusicService;
import de.uniks.pioneers.service.UserService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ChatController implements Controller {

    private final ChatService chatService;
    private final EventListener eventListener;
    private final Provider<OnlineUserSubController> onlineUserSubControllerProvider;
    private final LoginResultStorage loginResultStorage;
    private final UserService userService;
    private final List<MessageSubController> privateChatSubControllers = new ArrayList<>();
    private final ResourceBundle resourceBundle;
    private final MusicService musicService;
    private final ObservableList<Group> groups = FXCollections.observableArrayList();
    @FXML
    public TextField textField_LobbyChat_messageInput;
    @FXML
    public Button button_LobbySelect_sendMessage;
    @FXML
    public TabPane lobby_ChatTabPane;
    @FXML
    public Tab lobby_UsersTab;
    @FXML
    public TableView<User> lobby_OnlineUserList;
    @FXML
    public TableColumn<User, String> lobby_UserNameColumn;
    @FXML
    public ImageView emojiMenu;
    private EmojiMenuSubController emojiMenuSubController;
    private ObservableList<Member> spectators = FXCollections.observableArrayList();
    private Disposable disposable;
    private OnlineUserSubController onlineUserSubController;
    private Parent parentFxml;

    private Boolean clickOnUser = false;

    private Boolean chatFromPreviousScreen = false;
    private MessageSubController lobbyChatSubController;
    private String gameId;
    private boolean isInGameChat = false;
    private Pane rootEmojiMenu;
    private ScrollPane searchScrollPane;
    private FlowPane searchFlowPane;
    private TabPane tabPane;
    private TextField txtSearch;
    private ComboBox<Image> boxTone;
    //private EmojiMenuSubController emojiMenuSubController;

    @Inject
    public ChatController(ChatService chatService, EventListener eventListener, Provider<OnlineUserSubController> onlineUserSubControllerProvider, LoginResultStorage loginResultStorage, UserService userService, ResourceBundle resourceBundle, MusicService musicService) {

        this.chatService = chatService;
        this.eventListener = eventListener;
        this.onlineUserSubControllerProvider = onlineUserSubControllerProvider;
        this.loginResultStorage = loginResultStorage;
        this.userService = userService;
        this.resourceBundle = resourceBundle;
        this.musicService = musicService;
    }

    @Override
    public void init() {

        // Listen on all messages events
        this.chatService.getGroups(null).observeOn(Constants.FX_SCHEDULER).subscribe(this.groups::setAll);
        disposable = this.eventListener.listen("groups.*." + Constants.MESSAGES + ".*.*", Message.class).observeOn(Constants.FX_SCHEDULER).subscribe(resultEvent ->
        {
            if (resultEvent.event().endsWith("created")) {
                // Get the group id and check if the chat tab pane has no open with this id
                String[] split = resultEvent.event().split("\\.");
                String groupId = split[1];
                if (checkNoTabOpen(groupId)) {
                    // Get group with this group id and create a new tab
                    // Check first if group already is in groups list
                    if (this.groups.stream().noneMatch(group -> group._id().equals(groupId))) {
                        this.chatService.getGroup(groupId).observeOn(Constants.FX_SCHEDULER).subscribe(resultGroup ->
                        {
                            this.groups.add(resultGroup);
                            this.createdNewTab(resultGroup);
                            //play sound when new tab opens
                            musicService.playMessageSound();
                        });
                    } else {
                        this.groups.forEach(group ->
                        {
                            if (group._id().equals(groupId)) {
                                this.createdNewTab(group);
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void destroy() {

        if (disposable != null) {
            this.disposable.dispose();
            this.onlineUserSubController.destroy();
            this.onlineUserSubController = null;

            // destroy controllers for every single chat tab
            this.privateChatSubControllers.forEach(MessageSubController::destroy);
            this.privateChatSubControllers.clear();

            if (lobbyChatSubController != null) {
                this.lobbyChatSubController.destroy();
                this.lobbyChatSubController = null;
            }
        }
    }

    @Override
    public Parent render() {

        // We only use this loader in a test
        // If the parent has no parent fxml
        final Parent parent;
        if (parentFxml == null) {
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/lobbyChat.fxml"), resourceBundle);
            loader.setControllerFactory(c -> this);
            try {
                parent = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            parent = null;
        }

        // only online user tab is opened when app is started, disable send message button and text field
        button_LobbySelect_sendMessage.setDisable(true);
        textField_LobbyChat_messageInput.setDisable(true);
        // disable or enable send button regarding which tab is selected
        lobby_ChatTabPane.setOnMouseClicked(this::checkUserTab);

        // can send messages via Enter, no need to click on button
        textField_LobbyChat_messageInput.setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Create a chat controller and show all online user
        this.onlineUserSubController = this.onlineUserSubControllerProvider.get();
        this.onlineUserSubController.setView(this.lobby_OnlineUserList, this.lobby_UserNameColumn);
        this.onlineUserSubController.init();
        this.onlineUserSubController.render();
        emojiMenuSubController = new EmojiMenuSubController(resourceBundle);
        emojiMenuSubController.setTestEmojiMenu(rootEmojiMenu == null);
        emojiMenuSubController.setTextField(textField_LobbyChat_messageInput);
        emojiMenuSubController.setEmojiMenu(rootEmojiMenu, searchScrollPane, searchFlowPane, tabPane, txtSearch, boxTone);
        emojiMenuSubController.render();
        emojiMenuSubController.init();

        this.lobby_OnlineUserList.setOnMouseClicked(this::checkGroups);

        // Create private chat tabs after screen changed
        this.chatService.getPrivateChats().forEach(group -> {
            this.chatFromPreviousScreen = true;
            this.createdNewTab(group);
        });

        return parent;
    }

    // Take private chats to next screen
    public void setPrivateChatToNextScreen() {
        this.chatService.setPrivateChatToNextScreen(this.privateChatSubControllers);
    }

    // Clear your private chats after log out, so you have no open tabs after log in
    public void clearPrivateChatAfterLogout() {
        this.chatService.setPrivateChatToNextScreen(new ArrayList<>());
    }

    private void checkUserTab(MouseEvent mouseEvent) {
        // disable send button if online user tab (first tab) is selected
        // enable send button if any other tab is selected
        if (mouseEvent.getClickCount() > 0) {
            button_LobbySelect_sendMessage.setDisable(lobby_ChatTabPane.getSelectionModel().getSelectedIndex() == 0);
            textField_LobbyChat_messageInput.setDisable(lobby_ChatTabPane.getSelectionModel().getSelectedIndex() == 0);
        }
    }

    private void checkGroups(MouseEvent mouseEvent) {
        // Check if mouse is double-clicked
        if (mouseEvent.getClickCount() == 2) {
            // Get the user from table view
            User user = this.lobby_OnlineUserList.getSelectionModel().getSelectedItem();
            this.clickOnUser = true;
            // Get your group with this user
            if (user != null) {
                //If user click on himself just return null
                if (user._id().equals(loginResultStorage.getLoginResult()._id())) {
                    return;
                }
                this.chatService.getSpecificGroups(user._id()).observeOn(Constants.FX_SCHEDULER).subscribe(result ->
                {
                    // If you have no group with this user create one
                    if (result.size() == 0) {
                        this.chatService.createGroup(user.name(), user._id()).observeOn(Constants.FX_SCHEDULER).subscribe(resultGroup ->
                        {
                            if (resultGroup.createdAt().equals(Constants.CUSTOM_ERROR)) {
                                this.clickOnUser = false;
                                // show error as Alert
                                showAlert(resultGroup.name(), resultGroup._id());
                            } else if (this.checkNoTabOpen(resultGroup._id())) {

                                this.createdNewTab(resultGroup);
                            }
                        });
                    } else {
                        // If there is no open tab created one
                        if (this.checkNoTabOpen(result.get(0)._id())) {
                            this.createdNewTab(result.get(0));
                        }
                    }
                });
            }
        }
    }

    private void createdNewTab(Group group) {

        // Create a new private controller and tab
        MessageSubController privateChatSubCon = new MessageSubController(this.chatService, this.eventListener, this.musicService, isInGameChat);
        privateChatSubControllers.add(privateChatSubCon);
        Tab tab = new Tab();
        this.lobby_ChatTabPane.getTabs().add(tab);

        // bind new tab to controller
        privateChatSubCon.setTab(tab);
        privateChatSubCon.setGroup(group);
        privateChatSubCon.init();
        privateChatSubCon.render();

        // get id from chat partner
        String chatPartnerId = null;
        for (String id : group.members()) {
            if (!id.equals(loginResultStorage.getLoginResult()._id())) {
                chatPartnerId = id;
            }
        }

        userService.getUser(chatPartnerId).observeOn(Constants.FX_SCHEDULER).subscribe(result ->
        {
            // get name from group partner
            String tabDesc = result.name();

            // Set tab name and id
            tab.setText(tabDesc);
            tab.setId(group._id());

            // your partner created chat with you
            // new chat tab opens in background
            if (!clickOnUser) {
                // create unread
                if (!chatFromPreviousScreen) {
                    Label msgCounterLabel = new Label();
                    msgCounterLabel.setStyle("-fx-background-color: red");
                    msgCounterLabel.setText("1");
                    tab.setGraphic(msgCounterLabel);
                }
                chatFromPreviousScreen = false;
            } else {
                // open newest only if you clicked on your chat partner
                this.lobby_ChatTabPane.getSelectionModel().selectLast();

                // send button and message input field are enabled
                button_LobbySelect_sendMessage.setDisable(false);
                textField_LobbyChat_messageInput.setDisable(false);
            }
        });
    }

    // Check for no open tab
    private boolean checkNoTabOpen(String id) {
        return this.lobby_ChatTabPane.getTabs().stream().noneMatch(tab -> id.equals(tab.getId()));
    }

    public void sendMessage() {

        String message = textField_LobbyChat_messageInput.getText();

        if ((message != null) && (!message.isEmpty())) {

            // get chat group from selected tab
            Tab selectedTab = lobby_ChatTabPane.getSelectionModel().getSelectedItem();

            // send message to lobby chat
            if (selectedTab.getText().equals(Constants.LOBBY_CHAT_NAME) && selectedTab.getId().equals(this.gameId)) {
                String gameId = lobbyChatSubController.getGameId();
                sendMessageToServer(message, gameId, Constants.GAMES);
            }

            // send private message
            else {
                sendMessageToServer(message, selectedTab.getId(), Constants.GROUPS);
            }
        }
    }

    private void sendMessageToServer(String message, String id, String path) {
        this.chatService.sendMessage(message, id, path)
                .observeOn(Constants.FX_SCHEDULER)
                .subscribe(result -> {
                    if (result.createdAt().equals(Constants.LOBBY_CHAT_ERROR)) {
                        showAlert("error", resourceBundle.getString("rate.limit."));
                    } else {
                        textField_LobbyChat_messageInput.setText("");
                    }
                });
    }

    // Set parent from lobby select controller
    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void createLobbyController(String gameId, Map<String, User> membersToUI) {
        lobbyChatSubController = new MessageSubController(chatService, eventListener, musicService, isInGameChat);
        this.gameId = gameId;

        Tab tab = new Tab(Constants.LOBBY_CHAT_NAME);
        tab.setId(this.gameId);

        this.lobby_ChatTabPane.getTabs().add(1, tab);

        lobbyChatSubController.setTab(tab);
        lobbyChatSubController.setSpectators(spectators);
        lobbyChatSubController.setGameId(this.gameId);
        lobbyChatSubController.setUserHashMap(membersToUI);
        lobbyChatSubController.init();
        lobbyChatSubController.render();

        // send button and message input field are enabled
        button_LobbySelect_sendMessage.setDisable(false);
        textField_LobbyChat_messageInput.setDisable(false);

        this.lobby_ChatTabPane.getSelectionModel().select(1);
    }

    private void showAlert(String header, String context) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, context);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public void consumeShortcuts(KeyEvent keyEvent) {
        keyEvent.consume();
    }

    public void setSpectators(ObservableList<Member> spectators) {
        this.spectators = spectators;
    }

    public void setIsInGameChat(boolean status) {
        this.isInGameChat = status;
    }

    public void openEmojiMenu(MouseEvent mouseEvent) {
        // open and close emoji menu
        emojiMenuSubController.toggleMenu();
        mouseEvent.consume();
    }

    public void setEmojiMenu(Pane rootEmojiMenu, ScrollPane searchScrollPane, FlowPane searchFlowPane, TabPane tabPane, TextField txtSearch, ComboBox<Image> boxTone) {

        this.rootEmojiMenu = rootEmojiMenu;
        this.searchScrollPane = searchScrollPane;
        this.searchFlowPane = searchFlowPane;
        this.tabPane = tabPane;
        this.txtSearch = txtSearch;
        this.boxTone = boxTone;
    }
}
