package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.model.TileTemplate;
import de.uniks.pioneers.service.*;
import io.reactivex.rxjava3.core.Observable;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import javax.inject.Provider;
import java.util.List;
import java.util.ResourceBundle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
public class MapEditorControllerTest extends ApplicationTest {

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    App app;

    @Spy
    Provider<LobbySelectController> lobbySelectControllerProvider;

    @Spy
    MapService mapService;

    @Mock
    StringToKeyCodeService stringToKeyCodeService;

    @Mock
    PrefService prefService;

    @Mock
    UserService userService;

    @Mock
    LoginResultStorage loginResultStorage;

    @Mock
    MapEditorService mapEditorService;

    @InjectMocks
    MapEditorController mapEditorController;

    @Override
    public void start(Stage stage) {
        new App(mapEditorController).start(stage);
        stage.setWidth(1167);
        stage.setHeight(785);
        stage.centerOnScreen();
        waitForFxEvents();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mapEditorController = null;
        mapEditorService = null;
        mapService = null;
        lobbySelectControllerProvider = null;
        userService = null;
        loginResultStorage = null;
    }

    @Test
    void clickingElements() {
        waitForFxEvents();
        clickOn("#tilesRandomToggleType");
        clickOn("#tilesRandomToggle");
        clickOn("#tilesPasture");
        clickOn("#tilesMountains");
        clickOn("#tilesHills");
        clickOn("#tilesForest");
        clickOn("#tilesFields");
        clickOn("#tilesDesert");
        clickOn("#numbersRandomToggle");
        clickOn("#numbers2");
        clickOn("#numbers3");
        clickOn("#numbers4");
        clickOn("#numbers5");
        clickOn("#numbers6");
        clickOn("#numbers8");
        clickOn("#numbers9");
        clickOn("#numbers10");
        clickOn("#numbers11");
        clickOn("#numbers12");
        clickOn("#harborsRandomToggle");
        clickOn("#harborsLumber");
        clickOn("#harborsClay");
        clickOn("#harborsWool");
        clickOn("#harborsGrain");
        clickOn("#harborsOre");
        clickOn("#harborsGeneric");
    }
    @Test
    void placeAndDeleteElements() {
        clickOn("#tilesPasture");
        waitForFxEvents();
        moveTo("#numbers12");
        clickOn("#hex-330");
        moveTo("#numbers12");
        clickOn("#numbers6");
        moveTo("#numbers12");
        clickOn("#hex-330");
        moveTo("#numbers12");
        clickOn("#harborsOre");
        moveTo("#numbers12");
        clickOn("#hex-330");
        moveTo("#numbers12");


        clickOn("#harborsOre");
        clickOn("#hex000");
        clickOn("#numbers6");
        clickOn("#hex000");
        clickOn("#tilesPasture");
        clickOn("#hex000");
    }

    @Test
    void placeRandomElements() {
        clickOn("#tilesRandomToggle");
        waitForFxEvents();
        clickOn("#hex000");
        clickOn("#numbersRandomToggle");
        clickOn("#hex000");
        clickOn("#harborsRandomToggle");
        clickOn("#hex000");
    }

    @Test
    void saveMap() {
        clickOn("#tilesHills");
        waitForFxEvents();
        moveTo("#numbers12");
        clickOn("#hex-330");
        moveTo("#numbers12");
        clickOn("#saveMapButton");

        when(mapEditorService.uploadMap(any(), any(), any(), any(), any())).thenReturn(Observable.just(
                new MapTemplate("", "", "", "", "", "", 0, List.of(new TileTemplate(0, 0, 0, "pasture", null)), null)));

        when(mapEditorService.generateMapThumbnail(any())).thenReturn("hallo");

        write("Meine Map");
        clickOn(resourceBundle.getString("savemap"));
        waitForFxEvents();
        clickOn(resourceBundle.getString("map.continue"));
    }
}
