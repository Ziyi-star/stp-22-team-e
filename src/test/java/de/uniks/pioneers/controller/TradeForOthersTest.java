package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.layout.HBox;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class TradeForOthersTest extends ApplicationTest {
    @Spy
    LoginResultStorage loginResultStorage;
    @Mock
    PioneersService pioneersService;
    @Spy
    PioneersUIService pioneersUIService;
    @Mock
    TradeAndDropSubController tradeAndDropSubController;
    @Mock
    PioneersController pioneersController;
    @InjectMocks
    TradeForOthersSubController tradeForOthersSubController;

    private Map<String, Player> userIdToPlayer = new HashMap<>();
    private Map<String, User> userIdToUserInfo = new HashMap<>();

    @Override
    public void start(Stage stage) {
        loginResultStorage.setLoginResult(new LoginResult("", "", "1", "chen1", "online", Constants.AVATAR_LIST.get(0), null, "", ""));
        new App(tradeForOthersSubController).start(stage);
        // Stage width and height change for test
        stage.setWidth(900);
        stage.setHeight(500);
        stage.centerOnScreen();

        User user1 = new User("1", "chen1", "online", Constants.AVATAR_LIST.get(0), null);
        User user2 = new User("2", "chen2", "online", Constants.AVATAR_LIST.get(1), null);

        Resources resource = new Resources(5, 4, 3, 2, 1, 0);
        Player player1 = new Player("12345", "1", "#0000ff", true, 6, resource, null, 0, 0, null);
        Player player2 = new Player("12345", "2", "#000000", true, 2, resource, null, 0, 0, null);

        userIdToPlayer.put("1", player1);
        userIdToPlayer.put("2", player2);

        userIdToUserInfo.put("1", user1);
        userIdToUserInfo.put("2", user2);


        when(pioneersController.getThisPlayer()).thenReturn(player1);

        ResourcesDto resources = new ResourcesDto(0, 0, 0, 1, -1);

        Move buildMove = new Move("1", "move", "12345", "2", "build", 0, null, null, resources, null);

        tradeForOthersSubController.showMenu();
        tradeForOthersSubController.setListPlayers(userIdToPlayer);
        tradeForOthersSubController.setListUsers(userIdToUserInfo);
        tradeForOthersSubController.setProviderPlayerAvatar("1");
        tradeForOthersSubController.showMenuThings(buildMove);
        tradeForOthersSubController.init();


    }

    @Override
    public void stop() throws Exception {
        super.stop();
        tradeForOthersSubController = null;
        pioneersService = null;
        pioneersController = null;
        userIdToPlayer = null;
        userIdToUserInfo = null;
    }

    @Test
    public void clickOnCheck() {
        //click on check to create move
        ResourcesDto resourcesDto = new ResourcesDto(0, 0, 0, -1, 1);
        when(pioneersService.makeMoveResources(Constants.ACTION.OFFER.toString(), resourcesDto)).thenReturn(Observable.empty());
        clickOn("#imageview_accept");
        verify(pioneersService).makeMoveResources(Constants.ACTION.OFFER.toString(), resourcesDto);
    }

    @Test
    public void clickOnClose() {
        //click on close to create move
        when(pioneersService.makeMoveAction(Constants.ACTION.OFFER.toString())).thenReturn(Observable.empty());
        clickOn("#image_close");
        verify(pioneersService).makeMoveAction(Constants.ACTION.OFFER.toString());
    }

    @Test
    public void checkTheResourcesGive() {
        // check the give box children counts
        HBox hBox = lookup("#hbox_trade_give").query();
        assertEquals(1, hBox.getChildren().size());
    }

    @Test
    public void checkTheResourcesGain() {
        // check the gain box children counts
        HBox hBox = lookup("#hbox_trade_want").query();
        assertEquals(1, hBox.getChildren().size());
    }
}
