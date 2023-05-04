package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.service.PioneersService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class PlentyMonopolySubController implements Controller {
    @FXML
    public ImageView imageview_icon;
    @FXML
    public ImageView imageview_check;
    @FXML
    public Label label_lumber;
    @FXML
    public Label label_wool;
    @FXML
    public Label label_ore;
    @FXML
    public Label label_brick;
    @FXML
    public Label label_grain;
    @FXML
    private Label label_prompt;
    @FXML
    private Pane root;
    private final ResourceBundle resourceBundle;
    private final PioneersService pioneersService;
    private DevelopmentCardsSubController developmentCardsSubController;
    private boolean monopoly = true;
    private int clickCounter = 1;
    private int maxClickCount = 1;
    private Parent parentFxml;
    private final List<Integer> res = new ArrayList<>();
    private final List<ImageView> clickedResources = new ArrayList<>();
    private ImageView lastClickedCard;

    @Inject
    PlentyMonopolySubController(ResourceBundle resourceBundle, PioneersService pioneersService) {
        this.resourceBundle = resourceBundle;
        this.pioneersService = pioneersService;
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
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/yearOfPlenty_Monopoly.fxml"), resourceBundle);
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

        enableCheck(false);

        return parent;
    }

    //uses this Controller for year of plenty (Monopoly is default)
    public void setPlenty() {
        monopoly = false;
        clickCounter = 2;
        maxClickCount = 2;
        Platform.runLater(() -> label_prompt.setText(resourceBundle.getString("year.of.plenty")));
        imageview_icon.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/bank.png"))));
        //show counter
        label_lumber.setVisible(true);
        label_wool.setVisible(true);
        label_ore.setVisible(true);
        label_brick.setVisible(true);
        label_grain.setVisible(true);
    }

    public void setMonopoly() {
        monopoly = true;
        clickCounter = 1;
        maxClickCount = 1;
        Platform.runLater(() -> label_prompt.setText(resourceBundle.getString("monopoly.label")));
        imageview_icon.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/player.png"))));
        //hide counter
        label_lumber.setVisible(false);
        label_wool.setVisible(false);
        label_ore.setVisible(false);
        label_brick.setVisible(false);
        label_grain.setVisible(false);
    }

    public void show() {
        root.setVisible(true);
        root.setMouseTransparent(false);
        for (int i = 0; i < 5; i++) {
            res.add(0);
        }
    }

    public void hide() {
        if (developmentCardsSubController != null) {
            developmentCardsSubController.toggleMenu();
        }
        root.setVisible(false);
        root.setMouseTransparent(true);
        res.clear();
        //reset scales
        for (ImageView imageView : clickedResources) {
            imageView.setScaleX(1);
            imageView.setScaleY(1);
        }
        clickedResources.clear();
        label_lumber.setText("0");
        label_wool.setText("0");
        label_ore.setText("0");
        label_brick.setText("0");
        label_grain.setText("0");
    }

    public void apply(MouseEvent mouseEvent) {
        //set all not selected resources (value 0) to null. This is required for monopoly
        Integer grain = res.get(4) == 0 ? null : res.get(4);
        Integer brick = res.get(3) == 0 ? null : res.get(3);
        Integer ore = res.get(2) == 0 ? null : res.get(2);
        Integer lumber = res.get(0) == 0 ? null : res.get(0);
        Integer wool = res.get(1) == 0 ? null : res.get(1);
        ResourcesDto resource = new ResourcesDto(grain, brick, ore, lumber, wool);
        if (monopoly) {
            pioneersService.makeMoveResources(Constants.ACTION.MONOPOLY.toString(), resource).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                if (!result._id().equals("")) {
                    hide();
                }
            });
        } else {
            pioneersService.makeMoveResources(Constants.ACTION.YEAR_OF_PLENTY.toString(), resource).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                if (!result._id().equals("")) {
                    hide();
                }
            });
        }
        mouseEvent.consume();
    }

    public void setParent(Parent parentFxml) {
        this.parentFxml = parentFxml;
    }

    public void cardClicked(MouseEvent mouseEvent) {
        ImageView imageView = (ImageView) mouseEvent.getSource();
        clickedResources.add(imageView);
        MouseButton button = mouseEvent.getButton();
        String resource = (String) imageView.getUserData();
        //card is clicked with the left mouse button and the max amount of cards isn't reached yet
        if (button == MouseButton.PRIMARY && clickCounter > 0) {
            changeResourceAmount(resource, true);
            //increase card size when selected
            if (monopoly) {
                if (lastClickedCard != null) {
                    lastClickedCard.getParent().getStyleClass().removeAll("cardSelected");
                    changeResourceAmount((String) lastClickedCard.getUserData(), false);
                    clickCounter++;
                }
                imageView.getParent().getStyleClass().add("cardSelected");
                lastClickedCard = imageView;
                enableCheck(true);
            }
            else if (--clickCounter == 0) {
                enableCheck(true);
            }
        }
        //card is clicked with the right mouse button and at least one card is currently selected
        else if (button == MouseButton.SECONDARY && clickCounter < maxClickCount && getResourceAmount(resource) > 0 && !monopoly) {
            changeResourceAmount(resource, false);
            //decrease card size when unselected
            clickCounter++;
            enableCheck(false);
        }
        mouseEvent.consume();
    }

    //increments or decrements the counter for a specific resource by one
    private void changeResourceAmount(String resource, boolean add) {
        int i = add ? 1 : -1;
        int resourceIndex = 0;
        int resourceValue = 0;
        switch (resource) {
            case "lumber" -> {
                resourceValue = Math.max(res.get(0) + i, 0);
                label_lumber.setText(String.valueOf(resourceValue));
            }
            case "wool" -> {
                resourceIndex = 1;
                resourceValue = Math.max(res.get(1) + i, 0);
                label_wool.setText(String.valueOf(resourceValue));
            }
            case "ore" -> {
                resourceIndex = 2;
                resourceValue = Math.max(res.get(2) + i, 0);
                label_ore.setText(String.valueOf(resourceValue));
            }
            case "brick" -> {
                resourceIndex = 3;
                resourceValue = Math.max(res.get(3) + i, 0);
                label_brick.setText(String.valueOf(resourceValue));
            }
            case "grain" -> {
                resourceIndex = 4;
                resourceValue = Math.max(res.get(4) + i, 0);
                label_grain.setText(String.valueOf(resourceValue));
            }
        }
        res.set(resourceIndex, resourceValue);
    }

    private int getResourceAmount(String resource) {
        switch (resource) {
            case "lumber" -> {
                return res.get(0);
            }
            case "wool" -> {
                return res.get(1);
            }
            case "ore" -> {
                return res.get(2);
            }
            case "brick" -> {
                return res.get(3);
            }
            case "grain" -> {
                return res.get(4);
            }
            default -> {
                return 0;
            }
        }
    }

    private void enableCheck(boolean enabled) {
        imageview_check.setDisable(!enabled);
        imageview_check.setOpacity(enabled ? 1 : 0.3f);
    }

    public void setDevelopmentCardsSubController(DevelopmentCardsSubController developmentCardsSubController) {
        this.developmentCardsSubController = developmentCardsSubController;
    }
}
