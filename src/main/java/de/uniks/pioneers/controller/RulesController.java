package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ResourceBundle;

public class RulesController implements Controller {

    private final App app;
    private final ResourceBundle resourceBundle;
    private final UserService userService;
    private final Provider<LobbySelectController> lobbySelectControllerProvider;
    @FXML
    public Label howTo_Label;
    @FXML
    public Label diceRolls_Label;
    @FXML
    public Label buildingCosts_Label;
    @FXML
    public Label developmentCards_Label;
    @FXML
    public Label trading_Label;
    @FXML
    public Pane howToPane;
    @FXML
    public Pane diceRollsPane;
    @FXML
    public Pane buildingCostsPane;
    @FXML
    public Pane developmentCardsPane;
    @FXML
    public Pane tradingPane;


    @Inject
    RulesController(App app, ResourceBundle resourceBundle, UserService userService, Provider<LobbySelectController> lobbySelectControllerProvider) {
        this.app = app;
        this.resourceBundle = resourceBundle;
        this.userService = userService;
        this.lobbySelectControllerProvider = lobbySelectControllerProvider;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        // load rules screen
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/rulesScreen.fxml"), resourceBundle);
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (
                IOException e) {
            e.printStackTrace();
            return null;
        }

        app.setScreenTitle(resourceBundle.getString(Constants.RULES_SCREEN_TITLE) + resourceBundle.getString("logged.in.as") + userService.getUserName());
        howTo_Label.setTextFill(Color.GOLD);

        return parent;
    }

    private void setVisibility(boolean howTo, boolean diceRolls, boolean buildingCosts, boolean developmentCards, boolean trading, Label rulesLabel) {
        howTo_Label.setTextFill(Color.SKYBLUE);
        howToPane.setVisible(howTo);
        diceRolls_Label.setTextFill(Color.SKYBLUE);
        diceRollsPane.setVisible(diceRolls);
        buildingCosts_Label.setTextFill(Color.SKYBLUE);
        buildingCostsPane.setVisible(buildingCosts);
        developmentCards_Label.setTextFill(Color.SKYBLUE);
        developmentCardsPane.setVisible(developmentCards);
        trading_Label.setTextFill(Color.SKYBLUE);
        tradingPane.setVisible(trading);
        rulesLabel.setTextFill(Color.GOLD);
    }

    public void showHowToWin(MouseEvent mouseEvent) {
        setVisibility(true, false, false, false, false, howTo_Label);
        mouseEvent.consume();
    }

    public void showDiceRolls(MouseEvent mouseEvent) {
        setVisibility(false, true, false, false, false, diceRolls_Label);
        mouseEvent.consume();
    }

    public void showBuildingAndCosts(MouseEvent mouseEvent) {
        setVisibility(false, false, true, false, false, buildingCosts_Label);
        mouseEvent.consume();
    }

    public void showDevelopmentCards(MouseEvent mouseEvent) {
        setVisibility(false, false, false, true, false, developmentCards_Label);
        mouseEvent.consume();
    }

    public void showTrading(MouseEvent mouseEvent) {
        setVisibility(false, false, false, false, true, trading_Label);
        mouseEvent.consume();
    }

    public void exit(ActionEvent actionEvent) {
        app.show(lobbySelectControllerProvider.get());
        actionEvent.consume();
    }
}
