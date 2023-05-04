package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.service.PioneersService;
import io.reactivex.rxjava3.core.Observable;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlentyMonopolySubControllerTest extends ApplicationTest {

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @InjectMocks
    PlentyMonopolySubController plentyMonopolySubController;

    @Mock
    PioneersService pioneersService;

    @Override
    public void start(Stage stage) {
        new App(plentyMonopolySubController).start(stage);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.centerOnScreen();

        plentyMonopolySubController.hide();
        plentyMonopolySubController.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        plentyMonopolySubController = null;
        pioneersService = null;
    }

    @Test
    void monopoly() {
        plentyMonopolySubController.setMonopoly();
        clickOn("#lumber");
        when(pioneersService.makeMoveResources(Constants.ACTION.MONOPOLY.toString(), new ResourcesDto(null, null, null, 1, null))).thenReturn(Observable.empty());
        clickOn("#imageview_check");

        verify(pioneersService).makeMoveResources(Constants.ACTION.MONOPOLY.toString(), new ResourcesDto(null, null, null, 1, null));
    }

    @Test
    void monopolyNotEnough() {
        plentyMonopolySubController.setMonopoly();
        clickOn("#imageview_check");

        ImageView check = lookup("#imageview_check").query();
        assertTrue(check.isDisable());

        check = null;
    }

    @Test
    void yearOfPlenty() {
        plentyMonopolySubController.setPlenty();
        clickOn("#lumber");
        clickOn("#wool");
        when(pioneersService.makeMoveResources(Constants.ACTION.YEAR_OF_PLENTY.toString(), new ResourcesDto(null, null, null, 1, 1))).thenReturn(Observable.empty());
        clickOn("#imageview_check");

        verify(pioneersService).makeMoveResources(Constants.ACTION.YEAR_OF_PLENTY.toString(), new ResourcesDto(null, null, null, 1, 1));
    }

    @Test
    void yearOfPlentyNotEnough() {
        plentyMonopolySubController.setPlenty();
        clickOn("#lumber");
        clickOn("#wool");
        clickOn("#wool", MouseButton.SECONDARY);
        clickOn("#imageview_check");

        ImageView check = lookup("#imageview_check").query();
        assertTrue(check.isDisable());

        check = null;
    }
}
