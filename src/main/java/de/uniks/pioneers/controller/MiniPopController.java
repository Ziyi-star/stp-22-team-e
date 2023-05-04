package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.AnimationService;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.util.*;

public class MiniPopController implements Controller {

    private final ResourceBundle resourceBundle;
    private final AnimationService animationService;
    @FXML
    public Pane root;
    @FXML
    public ImageView image;
    @FXML
    public Label label;
    @FXML
    public Label text;

    @Inject
    public MiniPopController(ResourceBundle resourceBundle, AnimationService animationService) {
        this.resourceBundle = resourceBundle;
        this.animationService = animationService;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        root.setVisible(false);
        return null;
    }

    public void show(String development_card, String name) {
        // show which card was played
        String cardName = "";
        try {
            cardName = resourceBundle.getString(development_card + "1");// the "1" is for differentiation
        } catch (MissingResourceException ignored) {
        }

        switch (development_card) {
            case "monopoly" -> {
                image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(Constants.DEVELOPMENT_CARDS_IMAGE_PATH.MONOPOLY.toString()))));
                label.setText(name);
                text.setText(resourceBundle.getString("played") + " " + cardName);
            }
            case "year-of-plenty" -> {
                image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(Constants.DEVELOPMENT_CARDS_IMAGE_PATH.YEAR_OF_PLENTY.toString()))));
                label.setText(name);
                text.setText(resourceBundle.getString("played") + " " + cardName);
            }
            case "knight" -> {
                image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(Constants.DEVELOPMENT_CARDS_IMAGE_PATH.KNIGHT.toString()))));
                label.setText(name);
                text.setText(resourceBundle.getString("played") + " " + cardName);
            }
            case "road-building" -> {
                image.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream(Constants.DEVELOPMENT_CARDS_IMAGE_PATH.ROAD_BUILDING.toString()))));
                label.setText(name);
                text.setText(resourceBundle.getString("played") + " " + cardName);
            }
            default -> {
                return;
            }
        }

        root.setVisible(true);
        root.setOpacity(1);

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                animationService.fadeNode(root, 1, 0, 200).setOnFinished(e -> root.setVisible(false));
            }
        };
        timer.schedule(task, 3000);
    }

}
