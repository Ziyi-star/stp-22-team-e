package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.dto.UpdateMapTemplateDto;
import de.uniks.pioneers.model.MapTemplate;
import de.uniks.pioneers.service.MapTemplateService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.inject.Inject;

public class RenameController implements Controller {

    private final MapTemplateService mapTemplateService;
    @FXML
    public Pane root;
    @FXML
    public ImageView apply;
    @FXML
    public TextField name;
    @FXML
    public ImageView close;
    @FXML
    public Button rename_button;
    @FXML
    public Button changeDescription_button;
    @FXML
    public ImageView description_image;
    @FXML
    public TextArea description;
    @FXML
    public ImageView name_image;
    private MapTemplate map;
    private Pane renameMapsPane;

    private Pane blurBackground;

    @Inject
    public RenameController(MapTemplateService mapTemplateService) {
        this.mapTemplateService = mapTemplateService;
    }

    @Override
    public void init() {
    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        return null;
    }

    public void show(MapTemplate map) {
        root.setVisible(true);
        root.setMouseTransparent(false);
        name.setVisible(false);
        name_image.setVisible(false);
        description.setVisible(false);
        description_image.setVisible(false);
        this.map = map;
    }

    public void hide() {
        root.setVisible(false);
        root.setMouseTransparent(true);
        renameMapsPane.setMouseTransparent(true);
        name.setText("");
        description.setText("");
        blurBackground.setEffect(null);
        blurBackground.setMouseTransparent(false);
        changeDescription_button.setStyle("-fx-background-color: #6ea3e0;");
        rename_button.setStyle("-fx-background-color: #6ea3e0;");
    }

    public void apply(MouseEvent mouseEvent) {
        if (!name.getText().equals("")) {
            mapTemplateService.editMap(map._id(), new UpdateMapTemplateDto(name.getText(), null, null, null, null)).observeOn(Constants.FX_SCHEDULER).subscribe();
            hide();
        } else if (!description.getText().equals("")) {
            mapTemplateService.editMap(map._id(), new UpdateMapTemplateDto(null, null, description.getText(), null, null)).observeOn(Constants.FX_SCHEDULER).subscribe();
            hide();
        }
    }

    public void close(MouseEvent mouseEvent) {
        hide();
    }

    public void setRenamePane(Pane renameMapsPane) {
        this.renameMapsPane = renameMapsPane;
    }

    public void rename(ActionEvent actionEvent) {
        name.setVisible(true);
        name.setText(map.name());
        name.requestFocus();
        name.selectAll();
        name_image.setVisible(true);
        description.setVisible(false);
        description_image.setVisible(false);
        rename_button.setStyle("-fx-background-color: #476bff;");
        changeDescription_button.setStyle("-fx-background-color: #6ea3e0;");
    }

    public void changeDescription(ActionEvent actionEvent) {
        description.setVisible(true);
        description.setText(map.description());
        description.requestFocus();
        description.selectAll();
        description_image.setVisible(true);
        name.setVisible(false);
        name_image.setVisible(false);
        changeDescription_button.setStyle("-fx-background-color: #476bff;");
        rename_button.setStyle("-fx-background-color: #6ea3e0;");
    }

    public void setBlur(Pane blurBackground) {
        this.blurBackground = blurBackground;
    }
}
