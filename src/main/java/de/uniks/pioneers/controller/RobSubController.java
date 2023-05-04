package de.uniks.pioneers.controller;

import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class RobSubController implements Controller {

    private final ResourceBundle resourceBundle;
    private final Map<String, User> userIdToUserInfo;
    private final PioneersUIService pioneersUIService;
    @FXML
    private HBox playerHBox;
    @FXML
    private Node robMenuRoot;
    private HexagonRobberSubController selectedSubController;
    private Parent parentFxml;
    private String selectedVictim;

    @Inject
    public RobSubController(ResourceBundle resourceBundle, Map<String, User> userIDToUserInfo, PioneersUIService pioneersUIService) {
        this.resourceBundle = resourceBundle;

        this.userIdToUserInfo = userIDToUserInfo;
        this.pioneersUIService = pioneersUIService;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        final Parent parent;
        if (parentFxml == null) {
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/robMenu.fxml"), resourceBundle);
            loader.setControllerFactory(c -> this);
            try {
                parent = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            parent = null;
        }
        hideMenu();
        return parent;
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void showMenu() {
        robMenuRoot.setVisible(true);
        robMenuRoot.setMouseTransparent(false);
    }

    public void hideMenu() {
        robMenuRoot.setVisible(false);
        robMenuRoot.setMouseTransparent(true);
        clearVictims();
    }

    public void clearVictims() {
        playerHBox.getChildren().clear();
    }

    public void confirmRob(MouseEvent mouseEvent) {
        //On Checkmark - confirm rob on the selected player and pass to selectedSubController
        selectedSubController.robSomeone(selectedVictim);
        mouseEvent.consume();
    }

    public void drawVictims(List<Player> players) {
        //Set what the potential victims of the robbery could be.
        int idCounter = 0;
        for (Player victim : players) {
            User victimInfo = userIdToUserInfo.get(victim.userId());

            ImageView victimPortrait = pioneersUIService.generatePlayerPortrait(victimInfo.avatar(), Color.valueOf(victim.color()), 64);
            victimPortrait.setId("victimPortrait" + idCounter++);

            victimPortrait.setOnMouseClicked(event -> {
                //Reset other potential selections
                playerHBox.getChildren().forEach(playerImg -> {
                    playerImg.setScaleX(1);
                    playerImg.setScaleY(1);
                });

                //Select this one
                selectVictim(victim.userId(), victimPortrait);
            });

            if (idCounter == 1) {
                //If it's the first guy, select them by default
                selectVictim(victim.userId(), victimPortrait);
            }

            playerHBox.getChildren().add(victimPortrait);
        }
    }

    private void selectVictim(String victimUserId, ImageView victimPortrait) {
        selectedVictim = victimUserId;
        victimPortrait.setScaleX(1.2);
        victimPortrait.setScaleY(1.2);
    }

    public void setSelectedSubController(HexagonRobberSubController selectedSubController) {
        this.selectedSubController = selectedSubController;
    }
}
