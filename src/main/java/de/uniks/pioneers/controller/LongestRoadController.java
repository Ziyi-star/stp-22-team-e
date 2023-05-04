package de.uniks.pioneers.controller;

import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.AnimationService;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class LongestRoadController implements Controller {

    private final ResourceBundle resourceBundle;
    private final PioneersUIService pioneersUIService;
    private final AnimationService animationService;
    @FXML
    public Pane root;
    @FXML
    public ImageView player_image;
    @FXML
    public Label label;
    @FXML
    public Label name_label;
    public ImageView knight;
    public ImageView road1;
    public ImageView road2;
    public ImageView road3;
    private Parent parentFxml;
    private String longestPlayerId;

    private String largestPlayerId;

    @Inject
    public LongestRoadController(ResourceBundle resourceBundle, PioneersUIService pioneersUIService, AnimationService animationService) {

        this.resourceBundle = resourceBundle;
        this.pioneersUIService = pioneersUIService;
        this.animationService = animationService;
    }

    @Override
    public void init() {
        root.setMouseTransparent(true);
    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        hide();
        return null;
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public String getLongestPlayerId() {
        return longestPlayerId;
    }

    public void setLongestPlayerId(String playerId) {
        longestPlayerId = playerId;
    }

    public String getLargestPlayerId() {
        return largestPlayerId;
    }

    public void setLargestPlayerId(String largestPlayerId) {
        this.largestPlayerId = largestPlayerId;
    }

    public void hide() {
        root.setVisible(false);
        knight.setVisible(false);
        road1.setVisible(false);
        road2.setVisible(false);
        road3.setVisible(false);
    }

    public void show(User user, Player player, boolean showKnight) {
        root.setVisible(true);

        knight.setVisible(showKnight);
        road1.setVisible(!showKnight);
        road2.setVisible(!showKnight);
        road3.setVisible(!showKnight);

        pioneersUIService.generatePlayerPortrait(player_image, user.avatar(), Color.valueOf(player.color()), 64);
        name_label.setText(user.name());
        label.setText(showKnight ? resourceBundle.getString("largestArmy") : resourceBundle.getString("longestRoad"));

        animationService.fadeTranslate(root, 0, 500, 0, 1, 1000).setOnFinished(e ->
        {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    animationService.fadeTranslate(root, 0, 1000, 1, 0, 1000).setOnFinished(e2 ->
                            animationService.moveNode(root, 0, 0, 10, Interpolator.EASE_OUT));
                }
            };
            timer.schedule(timerTask, 2100);
        });
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                hide();
            }
        };
        timer.schedule(task, 5000);
    }
}
