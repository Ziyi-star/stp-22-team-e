package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.service.UserService;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class RulesControllerTest extends ApplicationTest {

    @Mock
    App app;

    @Mock
    UserService userService;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    RulesController rulesController;

    @Override
    public void start(Stage stage) {
        new App(rulesController).start(stage);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.centerOnScreen();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        rulesController = null;
        userService = null;
    }

    @Test
    void switchBetweenRules() {
        clickOn("#howToImage");
        clickOn("#diceRollsImage");
        clickOn("#buildingCostsImage");
        clickOn("#developmentCardsImage");
        clickOn("#tradingImage");

        assertEquals(lookup("#trading_Label").queryLabeled().getTextFill(), Color.valueOf("#FFD700"));
        FxAssert.verifyThat("#tradingPane", NodeMatchers.isVisible());
    }
}