package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.AnimationService;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.util.ResourceBundle;

public class CreditsController implements Controller {

    private final App app;
    private final Provider<LoginController> loginController;
    private final ResourceBundle resourceBundle;
    private final AnimationService animationService;
    @FXML
    public Pane credits;
    @FXML
    public VBox root;


    @Inject
    public CreditsController(App app, Provider<LoginController> loginController, ResourceBundle resourceBundle, AnimationService animationService) {
        this.app = app;
        this.loginController = loginController;
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
        final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/credits_screen.fxml"));
        loader.setControllerFactory(c -> this);
        final Parent parent;
        try {
            parent = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        app.setScreenTitle(resourceBundle.getString(Constants.CREDITS_SCREEN_TITLE));
        credits.setTranslateY(825);
        animationService.moveNode(credits, 0, -2500, 32000, Interpolator.LINEAR);

        return parent;
    }


    public void back_login(MouseEvent mouseEvent) {
        final LoginController controller = loginController.get();
        controller.render();
        app.show(controller);
        if (mouseEvent != null) mouseEvent.consume();
    }
}
