package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.PioneersService;
import io.reactivex.rxjava3.core.Observable;
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
import org.testfx.matcher.control.LabeledMatchers;

import java.util.List;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentCardsSubControllerTest extends ApplicationTest {

    @Spy
    ResourceBundle resourceBundle = ResourceBundle.getBundle("de/uniks/pioneers/language");

    @Mock
    PioneersService pioneersService;
    @Mock
    PioneersMapController mapController;

    @InjectMocks
    DevelopmentCardsSubController developmentCardsSubController;

    @Override
    public void start(Stage stage) {
        new App(developmentCardsSubController).start(stage);
        stage.setHeight(500);
        stage.setWidth(500);
        stage.centerOnScreen();

        // Hide Menu at start and set a list of development cards
        developmentCardsSubController.hideMenu();
        Player player = new Player("3", "1", "#000000ff", true, 2, new Resources(10, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, null, false, false, null,
                List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString(), false, true),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.MONOPOLY.toString(), false, true),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.YEAR_OF_PLENTY.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.VICTORY_POINT.toString(), false, true)));
        developmentCardsSubController.setPlayer(player);

        // Open menu
        developmentCardsSubController.setPlayCardAllowed(true);
        developmentCardsSubController.toggleMenu();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        developmentCardsSubController = null;
        pioneersService = null;
        mapController = null;
    }

    @Test
    void checkCorrectNumberOnLabels() {
        // Check the correct numbers and style classes
        FxAssert.verifyThat("#label_knight", LabeledMatchers.hasText("2"));
        FxAssert.verifyThat("#label_road_building", LabeledMatchers.hasText("1"));
        FxAssert.verifyThat("#label_monopoly", LabeledMatchers.hasText("1"));
        FxAssert.verifyThat("#label_year_of_plenty", LabeledMatchers.hasText("1"));
        FxAssert.verifyThat("#label_victory_point", LabeledMatchers.hasText("1"));

        assertEquals(lookup("#label_knight").query().getStyleClass().toString(), "label counter");
        assertEquals(lookup("#label_road_building").query().getStyleClass().toString(), "label redCounter");
        assertEquals(lookup("#label_monopoly").query().getStyleClass().toString(), "label redCounter");
        assertEquals(lookup("#label_year_of_plenty").query().getStyleClass().toString(), "label counter");
        assertEquals(lookup("#label_victory_point").query().getStyleClass().toString(), "label counter");
    }

    @Test
    void clickKnightCard() {
        when(pioneersService.makeMoveDevelopmentCard(anyString(), anyString())).thenReturn(Observable.just(
                new Move("25.07", "id123", "gameId123", "userId123", null, 5,
                        null, null, null, null, null)));


        clickOn("#knight_card");

        verify(pioneersService).makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(),
                Constants.DEVELOPMENT_CARDS.KNIGHT.toString());

        // dev cards menu is closed again
        FxAssert.verifyThat("#root_development_cards", NodeMatchers.isInvisible());
    }

    @Test
    void clickBuildRoadsCard() {
        Player player = new Player("3", "1", "#000000ff", true, 2, new Resources(10, 2, 2, 2, 2, 2), new RemainingBuildings(2, 1, 4), 2, null, false, false, null,
                List.of(new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.KNIGHT.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.MONOPOLY.toString(), false, true),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.YEAR_OF_PLENTY.toString(), false, false),
                        new DevelopmentCard(Constants.DEVELOPMENT_CARDS.VICTORY_POINT.toString(), false, true)));
        developmentCardsSubController.setPlayer(player);

        // Open menu
        developmentCardsSubController.hideMenu();
        developmentCardsSubController.toggleMenu();

        when(pioneersService.makeMoveDevelopmentCard(anyString(), anyString())).thenReturn(Observable.just(
                new Move("25.07", "id123", "gameId123", "userId123", null, 5,
                        null, null, null, null, null)));


        clickOn("#road_building_card");

        verify(pioneersService).makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(),
                Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString());

        // dev cards menu is closed again
        FxAssert.verifyThat("#root_development_cards", NodeMatchers.isInvisible());
    }


}