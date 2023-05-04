package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.Move;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.Resources;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TradeSubControllerTest extends ApplicationTest {

    @InjectMocks
    TradeAndDropSubController tradeSubController;

    @Spy
    LoginResultStorage loginResultStorage;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    PioneersService pioneersService;
    @Spy
    PioneersUIService pioneersUIService;
    @Mock
    PioneersController pioneersController;

    @Mock
    TradingPartnerSubController tradingPartnerSubController;

    @Mock
    App app;

    @Override
    public void start(Stage stage) {
        loginResultStorage.setLoginResult(new LoginResult("", "", "Owen", "JadaMaar", "online", Constants.AVATAR_LIST.get(0), null, "", ""));
        new App(tradeSubController).start(stage);
        // Stage width and height change for test
        stage.setWidth(900);
        stage.setHeight(500);
        stage.centerOnScreen();

        when(pioneersController.getMyHarbors()).thenReturn(new ArrayList<>());

        tradeSubController.setCurrentResources(new Resources(5, 5, 5, 5, 5, 5));
        tradeSubController.showMenu(true);
        tradeSubController.init();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        tradeSubController = null;
        pioneersController = null;
        tradingPartnerSubController = null;
    }

    @Test
    void sendOffer() {
        //select 3 lumber to give
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        //select 1 wool to give
        clickOn("#image_wool");
        //select 1 ore to give
        clickOn("#image_ore");
        //select 1 lumber to give
        clickOn("#image_brick");
        //select 1 lumber to give
        clickOn("#image_grain");
        //id give to generated cards
        clickOn("#generatedCard_0");
        clickOn("#image_lumber_gain");
        clickOn("#image_wool_gain");
        clickOn("#image_ore_gain");
        clickOn("#image_brick_gain");
        clickOn("#image_grain_gain");
        clickOn("#image_grain_gain");


        when(pioneersService.makeMoveResources("build", new ResourcesDto(1, 0, 0, -1, 0))).thenReturn(Observable.empty());

        clickOn("#image_check");
        verify(pioneersService).makeMoveResources("build", new ResourcesDto(1, 0, 0, -1, 0));

    }

    @Test
    void removeGivenCard() {
        //select 2 lumber to give
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        //select 1 wool to give
        clickOn("#image_wool");
        clickOn("#image_wool");
        //select 1 ore to give
        clickOn("#image_ore");
        clickOn("#image_ore");
        //select 1 lumber to give
        clickOn("#image_brick");
        clickOn("#image_brick");
        //select 1 lumber to give
        clickOn("#image_grain");
        clickOn("#image_grain");
        //remove all cards
        clickOn("#generatedCard_0");
        clickOn("#generatedCard_0");
        clickOn("#generatedCard_1");
        clickOn("#generatedCard_1");
        clickOn("#generatedCard_2");
        clickOn("#generatedCard_2");
        clickOn("#generatedCard_3");
        clickOn("#generatedCard_3");
        clickOn("#generatedCard_4");
        clickOn("#generatedCard_4");

        HBox hBox = lookup("#hbox_give").query();
        assertTrue(hBox.getChildren().isEmpty());
        assertEquals(((Label) lookup("#label_lumber").query()).getText(), "5");
        assertEquals(((Label) lookup("#label_wool").query()).getText(), "5");
        assertEquals(((Label) lookup("#label_ore").query()).getText(), "5");
        assertEquals(((Label) lookup("#label_brick").query()).getText(), "5");
        assertEquals(((Label) lookup("#label_grain").query()).getText(), "5");
    }

    @Test
    void removeGainedCard() {
        clickOn("#image_lumber_gain");
        clickOn("#image_lumber_gain");
        //select 1 wool to give
        clickOn("#image_wool_gain");
        clickOn("#image_wool_gain");
        //select 1 ore to give
        clickOn("#image_ore_gain");
        clickOn("#image_ore_gain");
        //select 1 lumber to give
        clickOn("#image_brick_gain");
        clickOn("#image_brick_gain");
        //select 1 lumber to give
        clickOn("#image_grain_gain");
        clickOn("#image_grain_gain");
        //remove all cards
        clickOn("#generatedCard_5");
        clickOn("#generatedCard_5");
        clickOn("#generatedCard_6");
        clickOn("#generatedCard_6");
        clickOn("#generatedCard_7");
        clickOn("#generatedCard_7");
        clickOn("#generatedCard_8");
        clickOn("#generatedCard_8");
        clickOn("#generatedCard_9");
        clickOn("#generatedCard_9");

        HBox hBox = lookup("#hbox_gain").query();
        assertTrue(hBox.getChildren().isEmpty());
    }

    @Test
    void closeMenu() {
        clickOn("#image_close");
        assertFalse(lookup("#root").query().isVisible());
    }

    @Test
    void setPlayer() {
        tradeSubController.setPlayer(new Player("", "Owen", "#0000FF", true, 8, null, null, 1, 1, null));
        Node avatar = lookup("#own_avatar").query();
        DropShadow dropShadow = (DropShadow) avatar.getEffect();
        assertEquals(dropShadow.getColor(), Color.valueOf("#0000FF"));
    }

    @Test
    void tradeWith() {
        clickOn("#image_brick");
        clickOn("#image_brick");
        clickOn("#image_brick");
        clickOn("#image_brick");
        clickOn("#image_wool_gain");
        assertTrue(tradeSubController.getBank());
        clickOn("#trade_partner_player");
        assertFalse(tradeSubController.getBank());
        clickOn("#trade_partner_player");
        assertTrue(tradeSubController.getBank());
    }

    @Test
    void autoBank() {

        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_lumber_gain");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_4");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain_gain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_9");
        assertTrue(tradeSubController.getBank());

    }

    @Test
    void autoShip3() {

        when(pioneersController.getMyHarbors()).thenReturn(new ArrayList<>(
                List.of("null")
        ));

        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_lumber_gain");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_4");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain_gain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_9");
        assertTrue(tradeSubController.getBank());
    }

    @Test
    void autoShip2() {

        when(pioneersController.getMyHarbors()).thenReturn(new ArrayList<>(
                List.of("lumber")
        ));


        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_wool_gain");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_4");
        assertTrue(tradeSubController.getBank());
        clickOn("#image_grain_gain");
        assertFalse(tradeSubController.getBank());
        clickOn("#generatedCard_9");
        assertTrue(tradeSubController.getBank());
    }


    @Test
    void errorTradeBank() {
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_lumber_gain");
        when(pioneersService.makeMoveTrade("build", new ResourcesDto(0, 0, 0, 1, -4), "684072366f72202b72406465")).thenReturn(Observable.just(new Move("errorResult", "", "", "", "", 0, "", null, null, "")));

        clickOn("#image_check");
        verify(pioneersService).makeMoveTrade("build", new ResourcesDto(0, 0, 0, 1, -4), "684072366f72202b72406465");
        assertTrue(lookup("OK").query().isVisible());
    }

    @Test
    void errorTrade() {
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_wool");
        clickOn("#image_lumber_gain");
        when(pioneersService.makeMoveResources("build", new ResourcesDto(0, 0, 0, 1, -3))).thenReturn(Observable.just(new Move("errorResult", "", "", "", "", 0, "", null, null, "")));
        clickOn("#image_check");
        verify(pioneersService).makeMoveResources("build", new ResourcesDto(0, 0, 0, 1, -3));
        assertTrue(lookup("OK").query().isVisible());
    }
}
