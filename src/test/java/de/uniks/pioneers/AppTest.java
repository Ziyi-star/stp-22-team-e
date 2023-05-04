package de.uniks.pioneers;

import de.uniks.pioneers.model.User;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TableViewMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;


class AppTest extends ApplicationTest {

    App app;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language", Locale.GERMAN);

    MainComponent testComponent;

    // Make a nodeList to clear all elements
    private List<Node> nodeList = new ArrayList<>();

    @Override
    public void start(Stage stage) {
        app = new App(null);
        testComponent = DaggerTestComponent.builder().mainApp(app).build();
        app.start(stage);
        app.show(testComponent.loginController());
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        app = null;
        testComponent = null;
        nodeList.clear();
        nodeList = null;
    }

    @Test
    public void criticalPath() {
        // Assert login screen title
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOGIN_SCREEN_TITLE));

        // change language
        clickOn("#imageview_settings");
        clickOn("#radio_english");
        clickOn("#btn_apply");
        assertEquals(Locale.getDefault(), Locale.ENGLISH);

        //change language back
        clickOn("#imageview_settings");
        clickOn("#radio_german");
        clickOn("#btn_apply");
        assertEquals(Locale.getDefault(), Locale.GERMAN);

        // Go to register screen
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        // Assert that you are in register screen
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.REGISTER_SCREEN_TITLE));

        // Register a user
        // Write name and password
        write("\t");
        write("\t");
        write("Rick\t");
        write("12345678\t");
        write("12345678\t");

        // Check that button is enabled
        FxAssert.verifyThat("#registerUserRegisterButton", NodeMatchers.isEnabled());

        // Click register button
        type(KeyCode.ENTER);

        // Check Alert
        FxAssert.verifyThat("OK", NodeMatchers.isVisible());
        FxAssert.verifyThat(lookup(resourceBundle.getString("change.to.login")), LabeledMatchers.hasText(resourceBundle.getString("change.to.login")));

        // Click on ok and change back to log in screen
        type(KeyCode.ENTER);

        // Assert login screen title and that username and password are set
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOGIN_SCREEN_TITLE));
        FxAssert.verifyThat("#textField_LoginAndTitleScreen_Username_Input", NodeMatchers.isNotNull());
        FxAssert.verifyThat("#passwordField_LoginAndTitleScreen_Password_Input", NodeMatchers.isNotNull());

        //go to credits screen
        clickOn("#credits_button");
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.CREDITS_SCREEN_TITLE));
        clickOn("#credits");
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOGIN_SCREEN_TITLE));

        // Log in
        write("\t");
        write("\t");
        write("\t");
        write("\t");
        type(KeyCode.ENTER);

        clickOn("#mapMenuButton");
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.MAP_MENU_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");

        clickOn("#ownLabel");
        ListView<Node> ownMapsList = lookup("#ownMapsList").query();
        HBox add = (HBox) ownMapsList.getItems().get(0);
        clickOn(add);
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.MAP_EDITOR_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");
        nodeList.add(ownMapsList);
        nodeList.add(add);

        clickOn("#leaveEditorButton");
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.MAP_MENU_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");

        clickOn("#exit");


        // Assert that you are in lobby select screen
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOBBY_SELECT_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");

        // Check that user list has a user and game list has a game
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Morty"));
        FxAssert.verifyThat("#lobby_list", TableViewMatchers.containsRow("a", "Morty"));

        TableView<User> tableView = lookup("#lobby_OnlineUserList").query();
        TabPane tabPane = lookup("#lobby_ChatTabPane").query();
        nodeList.add(tableView);
        nodeList.add(tabPane);

        // Open a private chat tab to take it to the next screen
        // Get focus cell and first element from table view if you double-click on it
        write("\t");
        type(KeyCode.SPACE);
        doubleClickOn(tableView.getFocusModel().getFocusedCell().getTableColumn().getTableView());

        // Assert that 2 tabs are open
        assertEquals(tabPane.getTabs().size(), 2);

        // Get to create lobby button
        clickOn("#createLobby_button");

        // Create a lobby
        write("Rickerus\t");
        write("123\t");
        type(KeyCode.ENTER);

        // Assert that you are in lobby screen
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOBBY_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");

        // Assert that 2 tabs are open after screen changed
        TabPane lobbyTabPane = lookup("#lobby_ChatTabPane").query();
        nodeList.add(lobbyTabPane);
        assertEquals(lobbyTabPane.getTabs().size(), 3);

        // Assert that lobby list contains user
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Morty"));
        ListView<Node> listView = lookup("#playerReady").query();
        nodeList.add(listView);
        assertEquals(listView.getItems().size(), 1);

        // Exit lobby
        clickOn("#exit_button");

        // Assert that 2 tabs are open after screen changed
        assertEquals(tabPane.getTabs().size(), 2);

        // Join lobby
        // Get focus on the first cell in game lobby list
        write("\t");
        write("\t");
        type(KeyCode.SPACE);

        // Go to join button and enter the password to join the game
        clickOn("#joinLobby_button");
        write("a\t");
        clickOn("#joinLobbyPassword_button");
        //already in Lobby
        // Go to Color button and choose color for the member
        clickOn("#color_button_lobby");
        ListView<Node> colorListView = lookup("#colorListView").query();
        HBox colorHBox1 = (HBox) colorListView.getItems().get(0);
        Label colorLabel = (Label) colorHBox1.getChildren().get(1);
        nodeList.add(colorHBox1);
        nodeList.add(colorLabel);

        clickOn(colorLabel);
        clickOn("#applyColor_button");

        // Assert screen title, game members and chat tabs
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOBBY_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");
        // Assert that tab pane has three open tabs
        TabPane tabPaneWithLobby = lookup("#lobby_ChatTabPane").query();
        nodeList.add(tabPaneWithLobby);
        assertEquals(tabPaneWithLobby.getTabs().size(), 3);
        FxAssert.verifyThat("#lobby_OnlineUserList", TableViewMatchers.containsRow("Morty"));
        ListView<Node> joinBox = lookup("#playerReady").query();
        nodeList.add(joinBox);
        assertEquals(joinBox.getItems().size(), 2);

        // Begin game
        clickOn("#ready_button");
        clickOn("#begin_button");

        // Click dices to roll
        clickOn("#image_dice");

        // Build first settlement (founding phase 1) + road
        clickOn("#selectCircle_0000");
        waitForFxEvents();
        clickOn("#selectCircle_00011");

        // Build second settlement (founding phase 2) + road
        clickOn("#selectCircle_0006");
        waitForFxEvents();
        clickOn("#selectCircle_0007");

        // Click dices to roll
        clickOn("#image_dice");

        // Set the robber after rolling a 7
        clickOn("#selectCircle_1-10");

        // Make a trade with another player
        clickOn("#button_trade");
        clickOn("#image_grain");
        clickOn("#image_brick_gain");
        clickOn("#image_check");

        // Needed to trigger a move event for the trade
        // Player 2 accept the trade and is shown in accept offer list
        clickOn("#send_textField");
        write("accept");
        clickOn("#send_button");

        // Click on avatar and then accept the trade
        clickOn(((HBox)lookup("#hbox_trade_players").query()).getChildren().get(0));
        clickOn("#accept_button");

        // Buy a development card and play the road building card
        clickOn("#button_development_card");
        clickOn("#development_menu");
        clickOn("#road_building_card");

        // Select circle to build a road
        clickOn("#selectCircle_01-17");
        clickOn("#selectCircle_-11011");

        // Upgrade settlement
        clickOn("#button_city");
        clickOn("#building_0000");

        //test change to victory screen
        sleep(5500);
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.VICTORY_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");
        clickOn("#exitButton");

        // Assert that you are in lobby select screen
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOBBY_SELECT_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + "Rick");
        assertEquals(tabPane.getTabs().size(), 2);

        // Log out
        clickOn("#logOut_button");

        // Assert login screen title
        assertEquals(app.getScreenTitle(), resourceBundle.getString(Constants.LOGIN_SCREEN_TITLE));
    }

}