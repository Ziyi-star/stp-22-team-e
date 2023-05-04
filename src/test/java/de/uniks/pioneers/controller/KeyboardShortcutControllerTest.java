package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.StringToKeyCodeService;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.ResourceBundle;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyboardShortcutControllerTest extends ApplicationTest {

    @Mock
    App app;
    @InjectMocks
    KeyboardShortcutController keyboardShortcutController;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    PioneersController pioneersController;

    @Mock
    StringToKeyCodeService stringToKeyCodeService;

    @Mock
    PrefService prefService;

    @Override
    public void start(Stage stage) {

        new App(keyboardShortcutController).start(stage);
        stage.setWidth(900);
        stage.setHeight(500);
        stage.centerOnScreen();

        when(prefService.getFull()).thenReturn("F11");
        when(prefService.getMapCenter()).thenReturn("Alt + Z");

        keyboardShortcutController.show();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        keyboardShortcutController = null;
        pioneersController = null;
    }

    @Test
    void applyFullScreen() {
        // test edit the fullscreen combo
        FxAssert.verifyThat("#fullscreen", TextInputControlMatchers.hasText("F11"));
        clickOn("#fullscreen_edit");
        FxAssert.verifyThat("#fullscreen", TextInputControlMatchers.hasText(""));
        type(KeyCode.F1);
        FxAssert.verifyThat("#fullscreen", TextInputControlMatchers.hasText("F1"));
        clickOn("#apply_button");
        verify(prefService).setFull("F1");


    }

    @Test
    void applyMapCenter() {
        // test edit the map center combo
        FxAssert.verifyThat("#map", TextInputControlMatchers.hasText("Alt + Z"));
        clickOn("#map_edit");
        FxAssert.verifyThat("#map", TextInputControlMatchers.hasText(""));
        push(new KeyCodeCombination((KeyCode.A), KeyCombination.ALT_DOWN));
        FxAssert.verifyThat("#map", TextInputControlMatchers.hasText("Alt + A"));
        clickOn("#map_edit");
        push(new KeyCodeCombination((KeyCode.A), KeyCombination.CONTROL_DOWN));
        FxAssert.verifyThat("#map", TextInputControlMatchers.hasText("Strg + A"));
        clickOn("#apply_button");
        verify(prefService).setMapCenter("Strg + A");
    }

    @Test
    void reset() {
        // tests reset
        clickOn("#fullscreen_edit");
        type(KeyCode.F1);
        clickOn("#map_edit");
        push(new KeyCodeCombination((KeyCode.A), KeyCombination.ALT_DOWN));
        doNothing().when(prefService).setFull("F11");
        doNothing().when(prefService).setMapCenter("Alt + Z");
        clickOn("#reset_button");
        verify(prefService).setFull("F11");
    }

    @Test
        // tests case when you click another edit button, without editing anything
    void multipleEdit() {
        clickOn("#fullscreen_edit");
        clickOn("#map_edit");
        FxAssert.verifyThat("#fullscreen", TextInputControlMatchers.hasText("F11"));
        clickOn("#fullscreen_edit");
        FxAssert.verifyThat("#map", TextInputControlMatchers.hasText("Alt + Z"));
        clickOn("#map_edit");
        FxAssert.verifyThat("#fullscreen", TextInputControlMatchers.hasText("F11"));
    }
}