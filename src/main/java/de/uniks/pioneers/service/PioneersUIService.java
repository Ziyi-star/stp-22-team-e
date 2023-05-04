package de.uniks.pioneers.service;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.ExpectedMove;
import de.uniks.pioneers.model.Move;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.Resources;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.*;

public class PioneersUIService {
    /*
        Service class for a variety of UI related things, such as generating player portraits or
        updating UI elements.
     */

    private AnimationService animationService;

    @Inject
    public PioneersUIService() {
    }

    public void setAnimationService(AnimationService animationService) {
        this.animationService = animationService;
    }

    public ImageView generatePlayerPortrait(String imagePath, Color color, int size) {
        //Generates a playerPortrait with a colored outline.
        //Uses the default avatar in case no avatar is selected
        ImageView playerPortrait = new ImageView();
        generatePlayerPortrait(playerPortrait, imagePath, color, size);
        return playerPortrait;
    }

    public void generatePlayerPortrait(ImageView playerPortrait, String imagePath, Color color, int size) {
        //Generates a playerPortrait with a colored outline.
        //Uses the default avatar in case no avatar is selected
        Image portraitImage = new Image(imagePath == null ? Constants.AVATAR_LIST.get(0) : imagePath);
        playerPortrait.setImage(portraitImage);
        DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, color, 5, 0.9, 0, 0);
        dropShadow.setHeight(12 * (size / 64.0));
        dropShadow.setWidth(12 * (size / 64.0));
        playerPortrait.setEffect(dropShadow);
        playerPortrait.setFitWidth(size);
        playerPortrait.setFitHeight(size);
    }

    public Image numberToImage(int roll) {
        String number;
        switch (roll) {
            case 1 -> number = "one";
            case 2 -> number = "two";
            case 3 -> number = "three";
            case 4 -> number = "four";
            case 5 -> number = "five";
            default -> number = "six";
        }
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/" + number + ".png")));
    }


    public void animateCards(Player oldPlayer, Player thisPlayer, Pane buildMenu) {
        //signum = 1: lost cards
        //signum = -1: gained cards
        float signum = 0;
        //checks for all cards if changes happened
        for (int i = 1; i <= 5; i++) {
            switch (i) {
                case 1 -> signum = Math.signum(oldPlayer.resources().lumber() - thisPlayer.resources().lumber());
                case 2 -> signum = Math.signum(oldPlayer.resources().wool() - thisPlayer.resources().wool());
                case 3 -> signum = Math.signum(oldPlayer.resources().ore() - thisPlayer.resources().ore());
                case 4 -> signum = Math.signum(oldPlayer.resources().brick() - thisPlayer.resources().brick());
                case 5 -> signum = Math.signum(oldPlayer.resources().grain() - thisPlayer.resources().grain());
            }
            //initiate animation if changes to current resource happened
            if (signum != 0) {
                ImageView img = new ImageView();
                buildMenu.getChildren().add(img);
                //Disclaimer: this relies on the current order of elements in fxml if any changes are made to it this might have to adapt accordingly
                ImageView card = (ImageView) buildMenu.getChildren().get(i);
                img.setImage(card.getImage());
                img.setLayoutX(card.getLayoutX());
                //give the card a y-offset depending on whether it is received or not
                img.setLayoutY(card.getLayoutY() + Math.signum(signum - 1) * 200);
                showAnimation(signum, buildMenu, img, 300);
            }
        }
    }

    public void animateDevelopmentCard(String type, Pane buildMenu, ImageView development_menu) {
        float signum = -1;
        // Check which typ of card it is to load the right image
        switch (type) {
            case "knight" ->
                    animateGainedDevelopmentCard(signum, development_menu, buildMenu, "development_Card_knight");
            case "road-building" ->
                    animateGainedDevelopmentCard(signum, development_menu, buildMenu, "development_Card_road");
            case "monopoly" ->
                    animateGainedDevelopmentCard(signum, development_menu, buildMenu, "development_Card_monopoly");
            case "year-of-plenty" ->
                    animateGainedDevelopmentCard(signum, development_menu, buildMenu, "development_Card_plenty");
            case "victory-point" ->
                    animateGainedDevelopmentCard(signum, development_menu, buildMenu, "development_Card_victorypoint");
        }
    }

    private void animateGainedDevelopmentCard(float signum, ImageView development_menu, Pane buildMenu, String cardTyp) {
        ImageView developmentImg = new ImageView();
        buildMenu.getChildren().add(developmentImg);
        // Get card images
        Image cardImage = new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/development_cards/" + cardTyp + ".png")));
        // Set the image and change it positions over the development menu icon, so it moves down with the animation
        developmentImg.setImage(cardImage);
        developmentImg.setLayoutX(development_menu.getLayoutX());
        developmentImg.setLayoutY(development_menu.getLayoutY() + Math.signum(signum - 1) * 200);
        // Show animation with method
        showAnimation(signum, buildMenu, developmentImg, 550);
    }

    private void showAnimation(float signum, Pane buildMenu, ImageView img, int duration) {
        animationService.fadeTranslate(img, 0, -signum * 200, Math.signum(signum + 1), 1 - Math.signum(signum + 1), duration)
                .setOnFinished(e -> buildMenu.getChildren().remove(img));
    }

    public void updateResourceLabels(Label label_lumber, Label label_wool, Label label_ore, Label label_brick,
                                     Label label_grain, Label label_unknown, Resources ownResources) {
        label_lumber.setText(Integer.toString(ownResources.lumber()));
        label_wool.setText(Integer.toString(ownResources.wool()));
        label_ore.setText(Integer.toString(ownResources.ore()));
        label_brick.setText(Integer.toString(ownResources.brick()));
        label_grain.setText(Integer.toString(ownResources.grain()));
        if (label_unknown != null) {
            label_unknown.setText(Integer.toString(ownResources.lumber() + ownResources.wool() + ownResources.ore() + ownResources.brick() + ownResources.grain()));
        }
    }

    public void updateLabel(Label longest_road, Player thisPlayer, Player candidate, HashMap<String, Integer> armies) {
        // sets label
        int size;
        if (armies == null) {
            size = thisPlayer.longestRoad() == null ? 0 : (int) thisPlayer.longestRoad();
        } else {
            size = armies.getOrDefault(thisPlayer.userId(), 0);
        }
        if (candidate != null && thisPlayer.userId().equals(candidate.userId())) {
            longest_road.setText(String.valueOf(size));
            longest_road.getStyleClass().removeAll("counterPlayerList", "counter");
            longest_road.getStyleClass().add("counterPlayerYellow");
        } else {
            longest_road.setText(String.valueOf(size));
            longest_road.getStyleClass().removeAll("counterPlayerYellow");
            longest_road.getStyleClass().add("counterPlayerList");
        }
    }


    public void enableUIDice(boolean status, ImageView dice1, ImageView dice2) {
        dice1.setOpacity(status ? 1f : 0.3f);
        dice1.setDisable(!status);
        dice2.setOpacity(status ? 1f : 0.3f);
        dice2.setDisable(!status);
    }

    public void setDiceImage(Move result, ImageView dice1, ImageView dice2) {
        int roll = result.roll();
        Random rand = new Random();
        int firstRoll;
        int secondRoll;
        if (roll <= 6) {
            firstRoll = rand.nextInt(1, roll);
        } else {
            firstRoll = rand.nextInt(roll - 6, 7);
        }
        secondRoll = roll - firstRoll;
        dice1.setImage(numberToImage(firstRoll));
        dice2.setImage(numberToImage(secondRoll));
    }

    public void showWinner(String winnerId, String thisPlayerId, String thisPlayerGameId, ResourceBundle resourceBundle, Pane inGamePane, AnchorPane inGameAnchorPane) {
        Image image;
        Pane pane = new Pane();
        Label label = new Label();
        label.setPrefHeight(900);
        label.setPrefWidth(1600);
        label.setAlignment(Pos.CENTER);
        if (winnerId.equals(thisPlayerId)) {
            image = setVictoryImage("views/images/victory_screen/victory.png", "victory", "victoryLabel", label, resourceBundle);
        } else if (thisPlayerGameId.equals("")) {
            image = setVictoryImage("views/images/victory_screen/victory.png", "end", "victoryLabel", label, resourceBundle);
        } else {
            image = setVictoryImage("views/images/victory_screen/lose.png", "defeat", "defeatLabel", label, resourceBundle);
        }
        ImageView imageView = new ImageView(image);
        pane.getChildren().addAll(imageView, label);
        inGamePane.getChildren().addAll(pane);
        GaussianBlur blur = new GaussianBlur(8);
        inGameAnchorPane.setEffect(blur);
        animationService.fadeNode(pane, 0, 1, 600);
    }

    private Image setVictoryImage(String image, String key, String style, Label label, ResourceBundle resourceBundle) {
        label.setText(resourceBundle.getString(key));
        label.getStyleClass().add(style);
        return new Image(Objects.requireNonNull(Main.class.getResourceAsStream(image)));
    }

    public void toggleNode(ImageView button, Node toggledNode, int direction, Node longestRoad) {
        boolean b = longestRoad != null;
        //set move direction based on the moved node
        if (toggledNode.isVisible()) {
            if (b) {
                longestRoad.setLayoutX(longestRoad.getLayoutX() + direction * 360);
            }
            animationService.moveNode(button.getParent(), direction * 350, 0, 500, Interpolator.EASE_BOTH);
            animationService.moveNode(toggledNode, direction * 400, 0, 500, Interpolator.EASE_BOTH)
                    .setOnFinished(e -> {
                        toggledNode.setVisible(false);
                        animationService.fadeNode(button, 1, 0, 200);
                    });
        } else {
            if (b) {
                longestRoad.setLayoutX(longestRoad.getLayoutX() - direction * 360);
            }
            toggledNode.setVisible(true);
            animationService.moveNode(button.getParent(), 0, 0, 500, Interpolator.EASE_BOTH);
            animationService.moveNode(toggledNode, 0, 0, 500, Interpolator.EASE_BOTH)
                    .setOnFinished(e -> animationService.fadeNode(button, 0, 1, 200));
        }
    }

    public void showInGameNotice(Constants.GAME_NOTICE type, Label label, ResourceBundle resourceBundle) {
        //Function to show an in-game notice (e.g. not enough figures, not enough resources, Rate Limit..)
        String msg = resourceBundle.getString(type.toString());
        label.setText(msg);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                Platform.runLater(() -> label.setText(""));
            }
        }, 4000);
    }

    public void outline(GridPane pane, Player player, ExpectedMove nextMove) {
        // set background color
        if (player.userId().equals(nextMove.players().get(0))) {
            pane.setStyle("-fx-background-color: rgba(166,215,255,0.2); -fx-border-insets: 9; -fx-border-color: #6294a6");
        } else {
            pane.setStyle("-fx-background-color:  #FFFFFF24; -fx-border-insets: 9; -fx-border-color:  grey");
        }
    }
}
