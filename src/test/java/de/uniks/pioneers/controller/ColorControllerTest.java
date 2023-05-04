package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.service.GameMemberService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class ColorControllerTest extends ApplicationTest {
    @Mock
    App app;


    @Mock
    GameMemberService gameMemberService;

    @Mock
    Provider<LobbyController> lobbyControllerProvider;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    ColorController colorController;

    private List<Member> list = new ArrayList<>();
    private ObservableList<Member> members = FXCollections.observableList(list);

    @Override
    public void start(Stage stage) {

        // Return a list of member in game
        Member a1 = new Member("3", "3", "3", "0815", false, "#ffc0cbff", false);
        Member a2 = new Member("3", "3", "3", "42", false, "#ffff00ff", false);
        Member a3 = new Member("3", "3", "3", "45", false, "#ff0000ff", false);
        Member a4 = new Member("3", "3", "3", "45", false, "008000ff", false);
        Member a5 = new Member("3", "3", "3", "45", false, "0000ffff", false);
        Member a6 = new Member("3", "3", "3", "45", false, "00ffffff", false);
        Member a7 = new Member("3", "3", "3", "45", false, null, false);
        //member in members add
        members.add(a1);
        members.add(a2);
        members.add(a3);
        members.add(a4);
        members.add(a5);
        members.add(a6);
        members.add(a7);

        colorController.setFxml("views/colorChooseDialog.fxml");
        colorController.setMembersList(members);

        new App(colorController).start(stage);
        // Stage width and height change for test
        stage.setWidth(500);
        stage.setHeight(500);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        colorController = null;
        lobbyControllerProvider = null;
        list = null;
        members = null;
    }

    @Test
    void checkLabelSize() {
        //check the size of the listview
        ListView<Node> colorListView = lookup("#colorListView").query();
        HBox colorHBox1 = (HBox) colorListView.getItems().get(0);
        HBox colorHBox2 = (HBox) colorListView.getItems().get(1);
        HBox colorHBox3 = (HBox) colorListView.getItems().get(2);
        assertEquals(colorHBox1.getChildren().size(), 3);
        assertEquals(colorHBox2.getChildren().size(), 3);
        assertEquals(colorHBox3.getChildren().size(), 1);
    }
}