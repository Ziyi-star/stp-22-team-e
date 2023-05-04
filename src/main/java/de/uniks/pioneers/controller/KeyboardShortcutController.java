package de.uniks.pioneers.controller;

import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.PrefService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class KeyboardShortcutController implements Controller {

    private final ResourceBundle resourceBundle;
    private final PrefService prefService;
    @FXML
    public Pane root;
    @FXML
    public TextField fullscreen;
    @FXML
    public TextField map;
    @FXML
    public Button apply_button;
    @FXML
    public ImageView fullscreen_edit;
    @FXML
    public ImageView map_edit;
    @FXML
    public Button reset_button;
    private boolean editMap = false;
    private Parent parentFxml;
    private boolean editFullscreen = false;

    @Inject
    public KeyboardShortcutController(ResourceBundle resourceBundle, PrefService prefService) {
        this.resourceBundle = resourceBundle;
        this.prefService = prefService;
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
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/keyboard_shortcuts_menu.fxml"), resourceBundle);
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

        hide();

        return parent;
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void hide() {
        root.setVisible(false);
        root.setMouseTransparent(true);
        editFullscreen = false;
        editMap = false;
    }

    public void show() {
        root.setVisible(true);
        root.setMouseTransparent(false);
        fullscreen.setText(prefService.getFull());
        map.setText(prefService.getMapCenter());
    }

    public void apply(ActionEvent actionEvent) {

        String fullscreenData = fullscreen.getText();
        String mapData = map.getText();

        if (!Objects.equals(fullscreenData, "") && !Objects.equals(fullscreenData, prefService.getFull()) && !Objects.equals(mapData, fullscreenData)) {
            prefService.setFull(fullscreenData);

        }


        if (!Objects.equals(mapData, "") && !Objects.equals(mapData, prefService.getMapCenter()) && !Objects.equals(mapData, fullscreenData)) {
            prefService.setMapCenter(mapData);

        }
        hide();
        actionEvent.consume();
    }

    public void fullscreenEdit(MouseEvent mouseEvent) {
        if (!editFullscreen) {
            editFullscreen = true;
            fullscreen.setText("");
            fullscreen.requestFocus();
            fullscreen.selectAll();
        }
        if (editMap) {
            editMap = false;
            map.setText(prefService.getMapCenter());
        }
        mouseEvent.consume();
    }

    public void mapEdit(MouseEvent mouseEvent) {
        if (!editMap) {
            editMap = true;
            map.setText("");
            map.requestFocus();
            map.selectAll();
        }
        if (editFullscreen) {
            editFullscreen = false;
            fullscreen.setText(prefService.getFull());
        }
        mouseEvent.consume();
    }

    public void fullscreenType(KeyEvent keyEvent) {
        comboTyped(keyEvent, fullscreen);
    }

    public void mapType(KeyEvent keyEvent) {
        comboTyped(keyEvent, map);
    }

    public void comboTyped(KeyEvent keyEvent, TextField field) {
        String str = String.valueOf(keyEvent.getCode());
        if (str.charAt(0) == 'F' && str.length() > 1) {
            field.setText(str);
            apply_button.requestFocus();
            if (editFullscreen) {
                editFullscreen = false;
            }
            if (editMap) {
                editMap = false;
            }
            keyEvent.consume();
        } else if (keyEvent.isControlDown() && keyEvent.getCode().isLetterKey()) {
            str = String.valueOf(keyEvent.getCode());
            field.setText("Strg" + " + " + str);
            apply_button.requestFocus();
            if (editFullscreen) {
                editFullscreen = false;
            }
            if (editMap) {
                editMap = false;
            }
            keyEvent.consume();
        } else if (keyEvent.isAltDown() && keyEvent.getCode().isLetterKey()) {
            str = String.valueOf(keyEvent.getCode());
            field.setText("Alt" + " + " + str);
            apply_button.requestFocus();
            if (editFullscreen) {
                editFullscreen = false;
            }
            if (editMap) {
                editMap = false;
            }
            keyEvent.consume();
        }
    }

    public void reset(ActionEvent actionEvent) {
        prefService.setFull("F11");
        prefService.setMapCenter("Alt + Z");
        fullscreen.setText(prefService.getFull());
        map.setText(prefService.getMapCenter());
        editMap = false;
        editFullscreen = false;
        actionEvent.consume();
    }

    public void toggleMenu() {
        if (root.isVisible()) {
            hide();
        } else {
            show();
        }
    }
}
