package de.uniks.pioneers;

import de.uniks.pioneers.controller.Controller;
import de.uniks.pioneers.controller.LobbyController;
import de.uniks.pioneers.controller.LobbySelectController;
import de.uniks.pioneers.controller.PioneersController;
import de.uniks.pioneers.service.PrefService;
import de.uniks.pioneers.service.StringToKeyCodeService;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.util.Objects;

public class App extends Application {

    private Stage stage;
    private Controller controller;
    private PrefService prefService;
    private StringToKeyCodeService stringToKeyCodeService;

    public App() {
        // dagger
        final MainComponent mainComponent = DaggerMainComponent.builder().mainApp(this).build();
        controller = mainComponent.loginController();
        prefService = mainComponent.prefService();
        stringToKeyCodeService = mainComponent.stringToKeyCodeService();

    }

    public App(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void start(Stage primaryStage) {

        this.stage = primaryStage;
        stage.setWidth(1660);
        stage.setHeight(964);
        stage.setTitle("Pioneers");

        final Scene scene = new Scene(new Label("Loading..."));
        stage.setScene(scene);

        //set css style
        scene.getStylesheets().add(Objects.requireNonNull(Main.class.getResource("views/light-theme.css")).toString());

        setAppIcon(stage);
        setTaskbarIcon();
        primaryStage.show();

        //enter/leave fullscreen mode if alt+enter is pressed
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (prefService != null) {
                if (event.getCode().equals(stringToKeyCodeService.stringToKeyCode(prefService.getFull()))) {
                    stage.setFullScreen(!stage.isFullScreen());
                } else {
                    try {
                        KeyCombination keyCombination = (KeyCombination) stringToKeyCodeService.stringToKeyCode(prefService.getFull());
                        if (keyCombination.match(event)) {
                            stage.setFullScreen(!stage.isFullScreen());
                        }
                    } catch (Exception ignored) {

                    }
                }
            }
        });

        stage.setFullScreenExitHint("");

        if (controller != null) {
            // show login screen
            show(controller);
        }

        //Modify scene size on resize of window
        scene.widthProperty().addListener(observable -> setScale(stage.getScene()));
        scene.heightProperty().addListener(observable -> setScale(stage.getScene()));
    }

    private void setScale(Scene scene) {
        //Get new Window Size
        final double newWidth = scene.getWidth();
        final double newHeight = scene.getHeight();
        final double ratio = 1600.0 / 900.0;

        double scaleFactor =
                newWidth / newHeight > ratio
                        ? newHeight / 900.0
                        : newWidth / 1600.0;

        //Scale Elements to new window size
        Scale scale = new Scale(scaleFactor, scaleFactor);
        scale.setPivotX(0);
        scale.setPivotY(0);
        scene.getRoot().getTransforms().setAll(scale);

        BorderPane borderPane = (BorderPane) scene.getRoot().getChildrenUnmodifiable().get(0);
        borderPane.setPrefWidth(newWidth / scaleFactor);
        borderPane.setPrefHeight(newHeight / scaleFactor);
        borderPane.setStyle("-fx-background-color: #000000;");
    }

    // Set App icon in game
    private void setAppIcon(Stage stage) {
        Image image = new Image(Objects.requireNonNull(getClass().getResource("AppIcon.png")).toString());
        stage.getIcons().add(image);
    }

    // Set App icon in taskbar
    private void setTaskbarIcon() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        try {
            final Taskbar taskbar = Taskbar.getTaskbar();
            final java.awt.Image image = ImageIO.read(Objects.requireNonNull(Main.class.getResource("AppIcon.png")));
            taskbar.setIconImage(image);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void stop() {
        if (controller.getClass().equals(LobbySelectController.class)) {
            ((LobbySelectController) controller).logOut(null);
        } else if (controller.getClass().equals(LobbyController.class)) {
            ((LobbyController) controller).exit(null);
        } else if (controller.getClass().equals(PioneersController.class)) {
            ((PioneersController) controller).exit(null);
        }
        cleanup();
        System.exit(0);
    }

    public void show(Controller controller) {
        cleanup();
        this.controller = controller;
        controller.init();
        stage.getScene().setRoot(new Group(new BorderPane(controller.render())));
        setScale(stage.getScene());
    }

    private void cleanup() {
        if (controller != null) {
            controller.destroy();
            controller = null;
        }
    }

    public Stage getPrimaryStage() {
        return stage;
    }

    public String getScreenTitle() {
        return stage.getTitle();
    }

    // Set the screen title
    public void setScreenTitle(String title) {
        stage.setTitle(title);
    }
}
