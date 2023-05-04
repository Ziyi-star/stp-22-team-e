package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.service.GameMemberService;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

import static de.uniks.pioneers.Constants.LOBBY_COLOR_SCREEN_TITLE;
import static javafx.geometry.Insets.EMPTY;

public class ColorController implements Controller {
    private final GameMemberService gameMemberService;
    private final ResourceBundle resourceBundle;
    private final List<Label> colorLabelList = new ArrayList<>();
    private final List<Color> colorSelectList = new ArrayList<>();

    private final App app;
    @FXML
    public Label error_Label;
    @FXML
    public ListView<HBox> colorListView;
    String chosenColor;
    @FXML
    private Button applyColor_button;
    private Node blurBox;
    private ObservableList<Member> members;
    private String fxmlPath;
    private HBox hBox;

    @Inject
    public ColorController(GameMemberService gameMemberService, ResourceBundle resourceBundle, App app) {
        this.gameMemberService = gameMemberService;
        this.resourceBundle = resourceBundle;
        this.app = app;
    }

    @Override
    public void init() {
    }

    @Override
    public Parent render() {
        final FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(fxmlPath), resourceBundle);
        fxmlLoader.setControllerFactory(c -> this);
        Parent parent = null;
        try {
            BoxBlur blur = new BoxBlur(3, 3, 3);
            if (blurBox != null) {
                blurBox.setEffect(blur);
            }

            //load Dialog Window
            DialogPane loader = fxmlLoader.load();
            parent = loader;
            if (blurBox != null) {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setDialogPane(loader);
                dialog.setTitle(resourceBundle.getString(LOBBY_COLOR_SCREEN_TITLE));
                dialog.initOwner(app.getPrimaryStage());
                dialog.show();
                Window window = dialog.getDialogPane().getScene().getWindow();
                window.setOnCloseRequest(event -> {
                    dialog.setResult(ButtonType.CLOSE);
                    this.destroy();
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        setDefaultColors();

        renderColors();

        members.addListener((ListChangeListener<? super Member>) c -> {
            while (c.next()) {
                // new member joined
                if (!c.wasUpdated() && !c.wasRemoved() && c.wasAdded() && !c.wasReplaced()) {
                    Color newColor = getNewColor();
                    // when more than three colors new hBox
                    if (hBox.getChildren().size() >= 3) {
                        hBox = new HBox();
                        addColorLabel(hBox, newColor);
                        colorListView.getItems().add(hBox);
                        setMouseEvents();
                    } else {
                        addColorLabel(hBox, newColor);
                        setMouseEvents();
                    }
                }
                // member updated
                else if (c.wasReplaced() || c.wasRemoved()) {
                    colorListView.getItems().clear();
                    colorSelectList.clear();
                    setDefaultColors();
                    renderColors();
                    setMouseEvents();
                }
            }
        });

        setMouseEvents();

        return parent;

    }

    private void setMouseEvents() {
        //add listener for each label
        for (Label colorChoose : colorLabelList) {
            colorChoose.setOnMouseClicked(this::onLabelClicked);
            colorChoose.setOnMouseEntered(this::onLabelHoverEnter);
            colorChoose.setOnMouseExited(this::onLabelHoverExit);
        }
    }

    private void setDefaultColors() {
        colorSelectList.add(Color.RED);
        colorSelectList.add(Color.YELLOW);
        colorSelectList.add(Color.BLUE);
        colorSelectList.add(Color.GREEN);
        colorSelectList.add(Color.ORANGE);
        colorSelectList.add(Color.CYAN);
    }

    private void renderColors() {
        //first delete the color who already taken in colorSelectList
        for (Member member : members) {
            if (member.color() != null) {
                Color memberColor = Color.valueOf(member.color());
                colorSelectList.removeIf(item -> Objects.equals(item, memberColor));
            }

        }

        int alreadyInListMemberSize = 0;
        int counterSpectator = 0;
        while (alreadyInListMemberSize <= members.size()) {
            hBox = new HBox();
            //for loop in 3 step to make every HBox just 3 Labels
            for (int i = alreadyInListMemberSize; i < alreadyInListMemberSize + counterSpectator + 3; i++) {
                //3. do not exist i, out of Boundary
                if (i >= members.size()) {
                    break;
                }
                // 1. member already has a color in list
                if (!members.get(i).spectator()) {
                    if (members.get(i).color() != null) {
                        Color memberColor = Color.valueOf(members.get(i).color());
                        Label label = addColorLabel(hBox, memberColor);
                        label.setText("  X");
                        label.setTextAlignment(TextAlignment.CENTER);
                        label.setWrapText(true);
                        label.setFont(new Font("Arial", 62));
                        label.setMouseTransparent(true);
                    }

                    //2. member do not have a color, or member is the current user
                    if (members.get(i).color() == null) {
                        //first check the size of the colorSelectList,
                        // then get the first available color, then delete it in the list
                        Color newColor = getNewColor();
                        addColorLabel(hBox, newColor).setOnMouseClicked(this::onLabelClicked);
                    }
                }
                if (members.get(i).spectator()) {
                    counterSpectator++;
                }
            }
            alreadyInListMemberSize = alreadyInListMemberSize + counterSpectator + 3;
            colorListView.getItems().add(hBox);
        }
    }

    private Color getNewColor() {
        Color newColor;
        if (colorSelectList.size() > 0) {
            newColor = colorSelectList.get(0);
            colorSelectList.remove(0);
        } else {
            newColor = randomColor();
        }
        return newColor;
    }

    private Label addColorLabel(HBox hBox, Color newColor) {
        Label label = new Label();
        label.setBackground(new Background(new BackgroundFill(newColor, CornerRadii.EMPTY, EMPTY)));
        label.setMinSize(105, 90);
        colorLabelList.add(label);
        hBox.getChildren().add(label);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    private javafx.scene.paint.Color randomColor() {
        Random random = new Random(); // Probably really put this somewhere where it gets executed only once
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        java.awt.Color randomColor = new java.awt.Color(red, green, blue);
        return javafx.scene.paint.Color.rgb(randomColor.getRed(), randomColor.getGreen(), randomColor.getBlue(), randomColor.getAlpha() / 255.0);
    }

    private void onLabelClicked(MouseEvent mouseEvent) {
        //make the label bigger
        Label colorNode = (Label) mouseEvent.getTarget();
        if (colorNode.getText().isEmpty()) {
            applyColor_button.setDisable(false);
        }
        for (HBox hbox : colorListView.getItems()) {
            for (Node label : hbox.getChildren()) {
                ((Label) label).setBorder(null);
            }
        }
        colorNode.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        chosenColor = colorNode.getBackground().getFills().get(0).getFill().toString();

    }

    private void onLabelHoverEnter(MouseEvent mouseEvent) {
        Label colorNode = (Label) mouseEvent.getTarget();
        if (colorNode.getBorder() == null) {
            colorNode.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        }
    }

    private void onLabelHoverExit(MouseEvent mouseEvent) {
        Label colorNode = (Label) mouseEvent.getTarget();
        Border border = new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT));
        if (colorNode.getBorder().equals(border)) {
            colorNode.setBorder(null);
        }
    }

    @Override
    public void destroy() {
        if (blurBox != null) {
            blurBox.setEffect(null);
        }
    }


    public void setBlurBox(Node blurBox) {
        this.blurBox = blurBox;
    }

    public void setFxml(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public void applyColor() {
        //update user with new data
        if (chosenColor == null) {
            error_Label.setText(resourceBundle.getString("no.chosenColor"));
            return;
        }
        String chosenColorInString = chosenColor.replace("0x", "#");
        boolean notPicked = true;
        for (Member member : members) {
            if (member.color() != null) {
                notPicked &= !member.color().equals(chosenColorInString);
            }
        }

        if (notPicked) {
            gameMemberService.updateMemberColor(chosenColorInString)
                    .observeOn(Constants.FX_SCHEDULER)
                    .subscribe(res -> {
                        if (res.contains(Constants.CHANGE_MEMBER_SHIP_SUCCESS)) {
                            this.destroy();
                            // get a handle to the stage
                            Stage stage = (Stage) applyColor_button.getScene().getWindow();
                            // close profile settings menu
                            stage.close();
                        } else {
                            error_Label.setText(res);
                        }
                    });
        }
    }

    public void setMembersList(ObservableList<Member> members) {
        this.members = members;
    }
}