package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.Member;
import de.uniks.pioneers.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

public class SpectatorListViewCell extends ListCell<Member> {
    private final ResourceBundle resourceBundle;
    public AnchorPane spectatorPane;
    @FXML
    public Label spectator_name;
    @FXML
    public ImageView spectator_image;
    private FXMLLoader mLLoader;
    private Map<String, User> userHashMap;
    public SpectatorListViewCell(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    static void spectatorInfo(Label spectator_name, Map<String, User> userHashMap, String s, ImageView spectator_image) {
        spectator_name.setText(userHashMap.get(s).name());
        String image = userHashMap.get(s).avatar();
        if (image == null) {
            spectator_image.setImage(new Image(Constants.AVATAR_LIST.get(0)));
        } else {
            Image image1 = new Image(image);
            spectator_image.setImage(image1);
        }
    }

    @Override
    protected void updateItem(Member member, boolean empty) {
        super.updateItem(member, empty);

        if (empty || member == null) {
            setText(null);
            setGraphic(null);
        } else {

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(Main.class.getResource("views/spectatorListItemView.fxml"), resourceBundle);
                mLLoader.setControllerFactory(c -> this);
                try {
                    mLLoader.load();
                    mLLoader.setController(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //set current name and image from avatar
                spectatorInfo(spectator_name, userHashMap, member.userId(), spectator_image);
            }
        }
        setText(null);
        setGraphic(spectatorPane);
    }

    public void setUserList(Map<String, User> userIdToName) {
        userHashMap = userIdToName;
    }
}
