package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.Event;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.model.Vote;
import de.uniks.pioneers.service.*;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.ListViewMatchers;

import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
class MapMenuControllerTest extends ApplicationTest {

    @Mock
    App app;

    @Mock
    UserService userService;

    @Mock
    EventListener eventListener;

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    MapTemplateService mapTemplateService;

    @Mock
    MapVoteService mapVoteService;

    @Mock
    MusicService musicService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    MapMenuController mapMenuController;

    private PublishSubject<Event<MapTemplate>> mapEventSubject;
    private PublishSubject<Event<Vote>> voteEventSubject;
    private PublishSubject<Event<User>> userEventSubject;

    @Override
    public void start(Stage stage) {

        when(mapTemplateService.getAllMaps()).thenReturn(Observable.just(List.of(new MapTemplate("1", "1", "123", "map1", null, "3", 1, null, null),
                new MapTemplate("2", "2", "1234", "map2", Constants.AVATAR_LIST.get(0), "0", 1, null, null),
                new MapTemplate("1", "1", "12345", "map3", null, "42", -1, null, null),
                new MapTemplate("1", "1", "123456", "map3", null, "0815", -1, null, null))));

        when(mapVoteService.getAllVotesFromUser(anyString())).thenReturn(Observable.just(List.of(new Vote("1", "2", "123", "3", 1),
                new Vote("1", "2", "1234", "3", 1),
                new Vote("1", "2", "12345", "3", -1))));

        mapEventSubject = PublishSubject.create();
        voteEventSubject = PublishSubject.create();
        userEventSubject = PublishSubject.create();

        when(eventListener.listen("maps.*.*", MapTemplate.class)).thenReturn(mapEventSubject);

        when(eventListener.listen("maps.*.votes.3.*", Vote.class)).thenReturn(voteEventSubject);

        when(eventListener.listen("users.*.*", User.class)).thenReturn(userEventSubject);

        when(loginResultStorage.getLoginResult()).thenReturn(new LoginResult("1", "2", "3", "Owen", "online", null, null, "4", "5"));

        when(userService.getAllUsers()).thenReturn(Observable.just(List.of(new User("2", "User", "online", null, null),
                new User("3", "User2", "offline", null, null), new User("0", "EvilMorty", "online", null, null))));

        new App(mapMenuController).start(stage);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mapMenuController = null;
        mapTemplateService = null;
        mapVoteService = null;
        userService = null;
        mapEventSubject.onComplete();
        voteEventSubject.onComplete();
        userEventSubject.onComplete();
    }

    @Test
    void showOwnAndPublicMaps() {
        clickOn("#ownLabel");
        FxAssert.verifyThat("#ownMapsList", ListViewMatchers.hasItems(2));

        clickOn("#publicLabel");
        FxAssert.verifyThat("#publicMapsList", ListViewMatchers.hasItems(3));
    }

    @Test
    void showAndHideMap() {
        Event<MapTemplate> mapEvent = new Event<>(".created", new MapTemplate("3", "3", "12345", "NewMap", Constants.AVATAR_LIST.get(0), "00", 4, null, null));
        mapEventSubject.onNext(mapEvent);
        waitForFxEvents();

        ListView<Node> listView = lookup("#publicMapsList").query();
        HBox hBox = (HBox) listView.getItems().get(0);
        ImageView imageView = (ImageView) hBox.getChildren().get(0);
        clickOn(imageView);

        // get imageview where map popup is displayed
        Pane background = lookup("#imagePane").query();
        FxAssert.verifyThat(background, Node::isVisible);

        clickOn(background);
        waitForFxEvents();

        // image is not visible anymore after clicked
        assertFalse(background.isVisible());

        imageView = null;
        listView = null;
        background = null;
    }

    @Test
    void clickOnThumbsUpAndUpdateVote() {
        // Click on the thumps up and update your -1 vote to +1
        when(mapVoteService.updateVote(anyString(), anyString(), anyInt())).thenReturn(Observable.just(new Vote("2", "2", "12345", "3", 1)));

        // Get thumps up image from maps list to click on it
        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(1);
        ImageView thumbsUp = (ImageView) map.getChildren().get(2);

        clickOn(thumbsUp);

        // Verify that methods were used
        verify(mapVoteService).updateVote("12345", "3", 1);

        // Update vote
        Event<Vote> voteEvent2 = new Event<>(".updated", new Vote("1", "1", "1234", "3", 1));
        voteEventSubject.onNext(voteEvent2);
        waitForFxEvents();

        thumbsUp = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void clickOnThumbsDownAndUpdateVote() {
        // Click on the thumps up and update your 1 vote to -1
        when(mapVoteService.updateVote(anyString(), anyString(), anyInt())).thenReturn(Observable.just(new Vote("2", "2", "1234", "3", -1)));

        // Get thumps down image from maps list to click on it
        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(0);
        ImageView thumbsDown = (ImageView) map.getChildren().get(3);

        clickOn(thumbsDown);

        // Verify that methods were used
        verify(mapVoteService).updateVote("1234", "3", -1);

        thumbsDown = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void clickOnThumbsUpAndSendVote() {
        // Click on the thumps up and send +1 vote
        when(mapVoteService.sendVote(anyString(), anyInt())).thenReturn(Observable.just(new Vote("2", "2", "123456", "3", 1)));

        // Get thumps up image from maps list to click on it
        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(2);
        ImageView thumpsUp = (ImageView) map.getChildren().get(2);

        clickOn(thumpsUp);

        // Verify that methods were used
        verify(mapVoteService).sendVote("123456", 1);

        // Create a new vote
        Event<Vote> voteEvent = new Event<>(".created", new Vote("4", "5", "123456", "3", 1));
        voteEventSubject.onNext(voteEvent);
        waitForFxEvents();

        thumpsUp = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void deleteThumbsUpVote() {
        // Click on the thumps up to delete the vote
        when(mapVoteService.deleteVote("1234", "3")).thenReturn(Observable.just(new Vote("2", "2", "1234", "3", 1)));

        // Get thumps up image from maps list to click on it
        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(0);
        ImageView thumbsUp = (ImageView) map.getChildren().get(2);

        clickOn(thumbsUp);

        // Verify that methods were used
        verify(mapVoteService).deleteVote("1234", "3");

        thumbsUp = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void deleteThumbsDownVote() {
        // Click on the thumps up to delete the vote
        when(mapVoteService.deleteVote("12345", "3")).thenReturn(Observable.just(new Vote("2", "2", "12345", "3", -1)));

        // Get thumps up image from maps list to click on it
        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(1);
        ImageView thumbsUp = (ImageView) map.getChildren().get(3);

        clickOn(thumbsUp);

        // Verify that methods were used
        verify(mapVoteService).deleteVote("12345", "3");

        // Delete vote
        Event<Vote> voteEvent3 = new Event<>(".deleted", new Vote("1", "2", "12345", "3", -1));
        voteEventSubject.onNext(voteEvent3);
        waitForFxEvents();

        thumbsUp = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void clickOnScore() {
        // click on score symbol
        when(mapVoteService.getVotes(anyString())).thenReturn(Observable.just(List.of(new Vote("1", "1", "123", "3", -1),
                new Vote("1", "1", "123", "2", 1))));

        ListView<Node> publicMapsList = lookup("#publicMapsList").query();
        HBox map = (HBox) publicMapsList.getItems().get(0);
        VBox vBox = (VBox) map.getChildren().get(4);

        Event<User> userEvent = new Event<>(".created", new User("4", "UserNew", "online", null, null));
        userEventSubject.onNext(userEvent);

        clickOn(vBox);

        // check that user in right list
        FxAssert.verifyThat("#likeList", ListViewMatchers.hasItems(1));
        FxAssert.verifyThat("#likeList", ListViewMatchers.hasListCell("User"));
        FxAssert.verifyThat("#dislikeList", ListViewMatchers.hasItems(1));
        FxAssert.verifyThat("#dislikeList", ListViewMatchers.hasListCell("User2"));

        clickOn();

        vBox = null;
        map = null;
        publicMapsList = null;
    }

    @Test
    void ownMapDeleted() {
        clickOn("#ownLabel");

        when(mapTemplateService.deleteMap(anyString())).thenReturn(Observable.empty());
        ListView<Node> ownMapsList = lookup("#ownMapsList").query();
        HBox map = (HBox) ownMapsList.getItems().get(1);
        ImageView trashCan = (ImageView) map.getChildren().get(6);

        clickOn(trashCan);
        clickOn(resourceBundle.getString("yes"));
        verify(mapTemplateService).deleteMap("123");

        Event<MapTemplate> ownMapEvent = new Event<>(".deleted", new MapTemplate("1", "1", "123", "Test", null, "3", 1, null, null));
        mapEventSubject.onNext(ownMapEvent);
        waitForFxEvents();

        assertEquals(1, ownMapsList.getItems().size());

        trashCan = null;
        map = null;
        ownMapsList = null;
    }

    @Test
    void ownMapEdit() {
        clickOn("#ownLabel");

        when(mapTemplateService.editMap(anyString(), any())).thenReturn(Observable.just(new MapTemplate("1", "1", "123", "map1", null, "3", 1, null, null)));
        ListView<Node> ownMapsList = lookup("#ownMapsList").query();
        HBox map = (HBox) ownMapsList.getItems().get(1);
        Label trashCan = (Label) map.getChildren().get(1);

        clickOn(trashCan);
        clickOn("#rename_button");
        write("Test");
        clickOn("#apply");

        verify(mapTemplateService).editMap("123", new UpdateMapTemplateDto("Test", null, null, null, null));

        Event<MapTemplate> ownMapEvent = new Event<>(".updated", new MapTemplate("1", "1", "123", "Test", null, "3", 1, null, null));
        mapEventSubject.onNext(ownMapEvent);
        waitForFxEvents();

        assertEquals(trashCan.getText(), "Test");

        trashCan = null;
        map = null;
        ownMapsList = null;
    }

    @Test
    void ownMapEditDescription() {
        clickOn("#ownLabel");

        when(mapTemplateService.editMap(anyString(), any())).thenReturn(Observable.just(new MapTemplate("1", "1", "123", "map1", null, "3", 1, null, null)));
        ListView<Node> ownMapsList = lookup("#ownMapsList").query();
        HBox map = (HBox) ownMapsList.getItems().get(1);
        Label name = (Label) map.getChildren().get(1);

        clickOn(name);
        clickOn("#changeDescription_button");
        write("Test");
        clickOn("#apply");

        verify(mapTemplateService).editMap("123", new UpdateMapTemplateDto(null, null, "Test", null, null));

        Event<MapTemplate> ownMapEvent = new Event<>(".updated", new MapTemplate("1", "1", "123", "map1", null, "Test", "3", 1, null, null));
        mapEventSubject.onNext(ownMapEvent);
        waitForFxEvents();

        name = null;
        map = null;
        ownMapsList = null;
    }
}