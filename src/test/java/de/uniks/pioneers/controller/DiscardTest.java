package de.uniks.pioneers.controller;


import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.LoginResult;
import de.uniks.pioneers.model.Resources;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DiscardTest extends ApplicationTest {

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
        loginResultStorage.setLoginResult(new LoginResult("", "", "Owen", "Chenr", "online", Constants.AVATAR_LIST.get(0), null, "", ""));
        new App(tradeSubController).start(stage);
        // Stage width and height change for test
        stage.setWidth(900);
        stage.setHeight(500);
        stage.centerOnScreen();

        when(pioneersController.getMyHarbors()).thenReturn(new ArrayList<>());

        tradeSubController.setCurrentResources(new Resources(0, 4, 0, 0, 4, 0));
        tradeSubController.showMenu(false);
        tradeSubController.init();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        tradeSubController = null;
        pioneersController = null;
        tradingPartnerSubController = null;
        pioneersUIService = null;
        pioneersService = null;
    }

    @Test
    public void sendHalfSuccess() {
        //select 4 lumber to give
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_lumber");

        when(pioneersService.makeMoveResources("drop", new ResourcesDto(0, 0, 0, -4, 0))).thenReturn(Observable.empty());

        clickOn("#image_check");
        verify(pioneersService).makeMoveResources("drop", new ResourcesDto(0, 0, 0, -4, 0));
    }

    @Test
    public void notHalf() {
        //select 4 lumber to give and 1 garin
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_lumber");
        clickOn("#image_grain");

        assertEquals(((Label) lookup("#drop_text_left_counter").query()).getText(), "5/4");
    }
}
