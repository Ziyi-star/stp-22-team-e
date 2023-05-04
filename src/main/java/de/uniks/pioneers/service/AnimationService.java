package de.uniks.pioneers.service;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

import javax.inject.Inject;

public class AnimationService {
    @Inject
    public AnimationService() {

    }

    //Node is the Node that is supposed to be animated
    //moveX and moveY determine the amount of pixels moved relative to the ORIGINAL position of the node.
    // if the node has been moved with animations before, and you want to return it to the original position the proper values are 0 for both moveX and moveY
    public ParallelTransition fadeTranslate(Node node, float moveX, float moveY, float startOpacity, float endOpacity, float duration) {
        TranslateTransition translateTransition = moveNode(node, moveX, moveY, duration, Interpolator.EASE_BOTH);
        FadeTransition fadeTransition = fadeNode(node, startOpacity, endOpacity, duration);
        ParallelTransition parallelTransition = new ParallelTransition(translateTransition, fadeTransition);
        parallelTransition.play();
        return parallelTransition;
    }

    public TranslateTransition moveNode(Node node, float moveX, float moveY, float duration, Interpolator interpolation) {
        TranslateTransition translateTransition = new TranslateTransition(new Duration(duration), node);
        translateTransition.setToY(moveY);
        translateTransition.setToX(moveX);
        translateTransition.setInterpolator(interpolation);
        translateTransition.play();
        return translateTransition;
    }

    public FadeTransition fadeNode(Node node, float startOpacity, float endOpacity, float duration) {
        FadeTransition fadeTransition = new FadeTransition(new Duration(duration), node);
        fadeTransition.setFromValue(startOpacity);
        fadeTransition.setToValue(endOpacity);
        fadeTransition.play();
        return fadeTransition;
    }
}
