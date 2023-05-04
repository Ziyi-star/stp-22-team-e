package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.GameService;
import de.uniks.pioneers.service.GameStorage;
import de.uniks.pioneers.service.MapTemplateService;
import de.uniks.pioneers.service.UserService;
import io.reactivex.rxjava3.core.Observable;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.List;
import java.util.ResourceBundle;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapChooseControllerTest extends ApplicationTest {
    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    MapChooseController mapChooseController;

    @Mock
    MapTemplateService mapTemplateService;

    @Mock
    UserService userService;

    @Mock
    GameService gameService;

    @Spy
    GameStorage gameStorage;

    @Override
    public void start(Stage stage) {
        GameSettings gameSettings = new GameSettings(2, 10);
        gameStorage.setGame(new Game("", "", "1", "Test Game ;)", "1", 1, false, gameSettings));
        when(userService.getAllUsers()).thenReturn(Observable.just(List.of(new User("1", "JadaMaar", "offline", null, null))));
        when(mapTemplateService.getAllMaps()).thenReturn(Observable.just(List.of(new MapTemplate("", "", "123", "TestMap", Constants.AVATAR_LIST.get(0), "This is a test map", "1", 100, null, null))));

        new App(mapChooseController).start(stage);
        // Stage width and height change for test
        stage.setWidth(400);
        stage.setHeight(200);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        mapChooseController = null;
    }

    @Test
    void show() {}

    @Test
    void selectMap() {
        //the icon ids are equal to their corresponding maps id
        clickOn("#123");
        GameSettings gameSettings = new GameSettings(2, 10, "123");
        when(gameService.getStoredGame()).thenReturn(new Game("", "", "1", "Test Game ;)", "1", 1, false, gameSettings));
        when(gameService.updateGame(null, null, false, gameSettings, null)).thenReturn(Observable.just(Constants.UPDATE_GAME_SUCCESS));
        clickOn("#apply");
        verify(gameService).updateGame(null, null, false, gameSettings, null);
    }

    @Test
    void defaultMap() {
        GameSettings gameSettings = new GameSettings(2, 10, null);
        when(gameService.getStoredGame()).thenReturn(new Game("", "", "1", "Test Game ;)", "1", 1, false, gameSettings));
        when(gameService.updateGame(null, null, false, gameSettings, null)).thenReturn(Observable.just(Constants.UPDATE_GAME_SUCCESS));
        clickOn("#default");
        verify(gameService).updateGame(null, null, false, gameSettings, null);

    }
}
