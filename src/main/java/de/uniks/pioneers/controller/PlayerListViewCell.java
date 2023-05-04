package de.uniks.pioneers.controller;

import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.DevelopmentCard;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;

public class PlayerListViewCell extends ListCell<Player> {

    private final ResourceBundle resourceBundle;
    private final PioneersUIService pioneersUIService;
    @FXML
    public Label player_name;
    @FXML
    public ImageView player_image;
    @FXML
    public GridPane player_grid;
    @FXML
    public Label unknown_resource;
    @FXML
    public Label longest_road;
    @FXML
    public Label unknown_development_cards;
    @FXML
    public Label label_knight_counter;
    @FXML
    public HBox root;
    private Player player;
    private Map<String, User> userHashMap;
    private FXMLLoader mLLoader;

    public PlayerListViewCell(ResourceBundle resourceBundle, PioneersUIService pioneersUIService) {
        this.resourceBundle = resourceBundle;
        this.pioneersUIService = pioneersUIService;
    }

    public Player getPlayer() {
        return player;
    }

    //create item here
    @Override
    protected void updateItem(Player player, boolean empty) {
        super.updateItem(player, empty);

        if (empty || player == null) {
            setText(null);
            setGraphic(null);
        } else {

            if (mLLoader == null) {
                mLLoader = new FXMLLoader(Main.class.getResource("views/playerListItemView.fxml"), resourceBundle);
                mLLoader.setControllerFactory(c -> this);
                try {
                    mLLoader.load();
                    mLLoader.setController(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //set current name and image from avatar
                this.player = player;
                player_name.setText(userHashMap.get(player.userId()).name());
                pioneersUIService.generatePlayerPortrait(player_image, userHashMap.get(player.userId()).avatar(), Color.valueOf(player.color()), 64);

                setText(null);
                setGraphic(player_grid);
            }
        }
    }

    public void updatePlayerListLabels(Player newPlayerState) {
        if (newPlayerState.active()) {
            unknown_resource.setText(Integer.toString(newPlayerState.resources().unknown()));
            int notRevealed = 0;
            for (DevelopmentCard card : newPlayerState.developmentCards()) {
                if (!card.revealed()) {
                    notRevealed += 1;
                }
            }
            unknown_development_cards.setText(Integer.toString(notRevealed));
        }
        player_grid.setOpacity(!newPlayerState.active() ? 0.5 : 1.0);
    }

    public void setUserList(Map<String, User> userIdToName) {
        userHashMap = userIdToName;
    }

}
