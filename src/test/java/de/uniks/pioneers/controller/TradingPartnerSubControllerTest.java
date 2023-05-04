package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.AnimationService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TradingPartnerSubControllerTest extends ApplicationTest {
    @InjectMocks
    TradingPartnerSubController tradingPartnerSubController;

    @Spy
    LoginResultStorage loginResultStorage;

    @Spy
    PioneersUIService pioneersUIService;

    @Mock
    PioneersService pioneersService;

    @Mock
    AnimationService animationService;

    @Mock
    PioneersController pioneersController;


    @Mock
    App app;

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    private Map<String, Player> userIdToPlayer = new HashMap<>();
    private Map<String, User> userIdToUserInfo = new HashMap<>();


    @Override
    public void start(Stage stage) {
        loginResultStorage.setLoginResult(new LoginResult("", "", "1", "morty", "online", Constants.AVATAR_LIST.get(0), null, "", ""));

        new App(tradingPartnerSubController).start(stage);
        // Stage width and height change for test
        stage.setWidth(900);
        stage.setHeight(500);
        stage.centerOnScreen();

        User user1 = new User("1", "morty", "online", Constants.AVATAR_LIST.get(0), null);
        User user2 = new User("2", "rick", "online", Constants.AVATAR_LIST.get(1), null);
        User user3 = new User("3", "beth", "online", Constants.AVATAR_LIST.get(2), null);
        User user4 = new User("4", "summer", "online", Constants.AVATAR_LIST.get(3), null);

        Player player1 = new Player("12345", "1", "#0000ff", true, 6, null, null, 0, 0, null);
        Player player2 = new Player("12345", "2", "#000000", true, 2, null, null, 0, 0, null);
        Player player3 = new Player("12345", "3", "#db3c23", true, 2, null, null, 0, 0, null);
        Player player4 = new Player("12345", "4", "#2469a7", true, 2, null, null, 0, 0, null);

        userIdToPlayer.put("1", player1);
        userIdToPlayer.put("2", player2);
        userIdToPlayer.put("3", player3);
        userIdToPlayer.put("4", player4);

        userIdToUserInfo.put("1", user1);
        userIdToUserInfo.put("2", user2);
        userIdToUserInfo.put("3", user3);
        userIdToUserInfo.put("4", user4);

        ResourcesDto resourcesDto = new ResourcesDto(0, 0, 0, -1, 1);
        tradingPartnerSubController.setResourcesDto(resourcesDto);

        ResourcesDto resources = new ResourcesDto(0, 0, 0, 1, -1);
        Move offerMove1 = new Move("1", "2", "12345", "2", "offer", 0, null, null, resources, null);
        Move offerMove2 = new Move("1", "1", "12345", "1", "offer", 0, null, null, resources, null);
        Move offerMove3 = new Move("1", "3", "12345", "3", "offer", 0, null, null, resources, null);

        tradingPartnerSubController.showMenu();

        tradingPartnerSubController.setUserList(userIdToUserInfo);
        tradingPartnerSubController.setPlayerList(userIdToPlayer);
        tradingPartnerSubController.setMove(offerMove1);
        tradingPartnerSubController.setMove(offerMove2);
        tradingPartnerSubController.setMove(offerMove3);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        tradingPartnerSubController = null;
        userIdToPlayer = null;
        userIdToUserInfo = null;
        pioneersUIService = null;
    }

    @Test
    void sendOfferClickedOnAvatar() {
        tradingPartnerSubController.setButtonState(true);
        HBox hbox_trade_players = lookup("#hbox_trade_players").query();
        ImageView avatarImage = (ImageView) hbox_trade_players.getChildren().get(0);
        when(pioneersController.getThisPlayer()).thenReturn(new Player("1", "1", "", true, 1, new Resources(0, 5, 5, 5, 5, 5), null, 0, 1, null));
        clickOn(avatarImage);
        ResourcesDto resourcesDto = new ResourcesDto(0, 0, 0, -1, 1);
        when(pioneersService.makeMoveTrade(Constants.ACTION.ACCEPT.toString(), resourcesDto, "2")).thenReturn(Observable.empty());
        clickOn("#accept_button");
        verify(pioneersService).makeMoveTrade(Constants.ACTION.ACCEPT.toString(), resourcesDto, "2");

    }


    @Test
    void checkResource() {
        HBox hbox_trade_want = lookup("#hbox_trade_give").query();
        Pane hbox_trade_want_pane = (Pane) hbox_trade_want.getChildren().get(0);
        ImageView tradeImage = (ImageView) hbox_trade_want_pane.getChildren().get(0);
        assertEquals(tradeImage.getId(), "lumber_Card.png");

        HBox hbox_trade_get = lookup("#hbox_trade_get").query();
        Pane hbox_trade_get_pane = (Pane) hbox_trade_get.getChildren().get(0);
        ImageView getImage = (ImageView) hbox_trade_get_pane.getChildren().get(0);
        assertEquals(getImage.getId(), "wool_Card.png");
    }

    @Test
    void checkAvatar() {
        HBox hbox_trade_players = lookup("#hbox_trade_players").query();
        assertEquals(hbox_trade_players.getChildren().size(), 3);
    }

    @Test
    void closeMenu() {
        tradingPartnerSubController.checkForOffer();
        when(pioneersService.makeMoveTrade(Constants.ACTION.ACCEPT.toString(), null, null)).thenReturn(Observable.just(new Move("1", "1", "3", "1", "accept", 0, null, null, null, null)));
        clickOn("#partner_avatar");
        clickOn("#imageview_reject");
        verify(pioneersService).makeMoveTrade(Constants.ACTION.ACCEPT.toString(), null, null);
    }

}