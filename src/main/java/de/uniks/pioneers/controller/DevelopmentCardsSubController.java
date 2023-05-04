package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.DevelopmentCard;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.service.PioneersService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ResourceBundle;

public class DevelopmentCardsSubController implements Controller {
    private final ResourceBundle resourceBundle;
    private final PlentyMonopolySubController plentyMonopolySubController;
    private final PioneersService pioneersService;
    private Parent parentFxml;
    private int knightCounter;
    private int roadBuildingCounter;
    private int monopolyCounter;
    private int yearOfPlentyCounter;
    private int victoryPointsCounter;
    private int lockedKnightCounter;
    private int lockedRoadBuildingCounter;
    private int lockedMonopolyCounter;
    private int lockedYearOfPlentyCounter;
    private Boolean playCardAllowed;
    @FXML
    public VBox root_development_cards;
    @FXML
    public ImageView knight_card;
    @FXML
    public ImageView road_building_card;
    @FXML
    public ImageView year_of_plenty_card;
    @FXML
    public ImageView monopoly_card;
    @FXML
    public ImageView victory_point_card;
    @FXML
    public Label label_knight;
    @FXML
    public Label label_road_building;
    @FXML
    public Label label_year_of_plenty;
    @FXML
    public Label label_monopoly;
    @FXML
    public Label label_victory_point;
    private PioneersMapController mapController;
    private Player thisPlayer;


    @Inject
    public DevelopmentCardsSubController(ResourceBundle resourceBundle, PlentyMonopolySubController plentyMonopolySubController, PioneersService pioneersService) {
        this.resourceBundle = resourceBundle;
        this.plentyMonopolySubController = plentyMonopolySubController;
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
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/developmentCardsMenu.fxml"), resourceBundle);
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

        // Hide menu if you load controller
        hideMenu();

        return parent;
    }

    private void showMenu() {
        root_development_cards.setVisible(true);
        root_development_cards.setMouseTransparent(false);

        setCardOpacity(playCardAllowed);

        // Show the current number of development cards
        if (thisPlayer != null && thisPlayer.developmentCards() != null){
            setDevelopmentCardsNumber();
        }
    }

    private void setCardOpacity(boolean playCardAllowed) {
        // Set opacity to 1 if you allowed to play a card else to 0.3
        knight_card.setOpacity(playCardAllowed ? 1.0 : 0.3);
        road_building_card.setOpacity(playCardAllowed ? 1.0 : 0.3);
        monopoly_card.setOpacity(playCardAllowed ? 1.0 : 0.3);
        year_of_plenty_card.setOpacity(playCardAllowed ? 1.0 : 0.3);
        victory_point_card.setOpacity(playCardAllowed ? 1.0 : 0.3);
    }

    public void hideMenu() {
        root_development_cards.setVisible(false);
        root_development_cards.setMouseTransparent(true);

        this.knightCounter = 0;
        this.roadBuildingCounter = 0;
        this.monopolyCounter = 0;
        this.yearOfPlentyCounter = 0;
        this.victoryPointsCounter = 0;
        this.lockedKnightCounter = 0;
        this.lockedRoadBuildingCounter = 0;
        this.lockedMonopolyCounter = 0;
        this.lockedYearOfPlentyCounter = 0;
    }

    public void toggleMenu() {
        if (root_development_cards.isVisible()) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void setDevelopmentCardsNumber(){
        // Count all locked and not revealed cards to show it on the label counter
        for (DevelopmentCard card : thisPlayer.developmentCards()) {
            if (card.type().equals(Constants.DEVELOPMENT_CARDS.KNIGHT.toString())){
                if (card.locked()){
                    this.lockedKnightCounter += 1;
                } else if (!card.revealed()) {
                    this.knightCounter += 1;
                }
            } else if (card.type().equals(Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString())) {
                if (card.locked()) {
                    this.lockedRoadBuildingCounter += 1;
                } else if (!card.revealed()) {
                    this.roadBuildingCounter += 1;
                }
            } else if (card.type().equals(Constants.DEVELOPMENT_CARDS.MONOPOLY.toString())) {
                if (card.locked()) {
                    this.lockedMonopolyCounter += 1;
                } else if (!card.revealed()) {
                    this.monopolyCounter += 1;
                }
            } else if (card.type().equals(Constants.DEVELOPMENT_CARDS.YEAR_OF_PLENTY.toString())) {
                if (card.locked()) {
                    this.lockedYearOfPlentyCounter += 1;
                } else if (!card.revealed()) {
                    this.yearOfPlentyCounter += 1;
                }
            } else {
                this.victoryPointsCounter += 1;
                this.label_victory_point.setText(Integer.toString(victoryPointsCounter));
            }
        }

        // Update all labels
        updateDevelopmentCardLabel(knightCounter, lockedKnightCounter, label_knight);
        updateDevelopmentCardLabel(roadBuildingCounter, lockedRoadBuildingCounter, label_road_building);
        updateDevelopmentCardLabel(monopolyCounter, lockedMonopolyCounter, label_monopoly);
        updateDevelopmentCardLabel(yearOfPlentyCounter, lockedYearOfPlentyCounter, label_year_of_plenty);
    }

    private void updateDevelopmentCardLabel(int playableCounter, int lockedCounter, Label labelDevelopmentCard) {
        // Set locked counter if you have only locked cards
        if (playableCounter == 0 && lockedCounter > 0) {
            labelDevelopmentCard.getStyleClass().removeAll("counter");
            // Check that you don't add the same style class twice or more
            if (!labelDevelopmentCard.getStyleClass().toString().equals("label redCounter")) {
                labelDevelopmentCard.getStyleClass().add("redCounter");
            }
            labelDevelopmentCard.setText(Integer.toString(lockedCounter));
        } else {
            labelDevelopmentCard.getStyleClass().removeAll("redCounter");
            // Check that you don't add the same style class twice or more
            if (!labelDevelopmentCard.getStyleClass().toString().equals("label counter")) {
                labelDevelopmentCard.getStyleClass().add("counter");
            }
            // Set playable + locked counter if you have at least one playable card
            labelDevelopmentCard.setText(Integer.toString(playableCounter + lockedCounter));
        }
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void developmentCardClicked(MouseEvent mouseEvent) {
        ImageView imageView = (ImageView) mouseEvent.getSource();
        useDevelopmentCard((String) imageView.getUserData());
        mouseEvent.consume();
    }

    private void useDevelopmentCard(String developmentCard) {
        // Check if you allowed to play a card
        // currently false: roll phase, rob placing and not your turn
        if (this.playCardAllowed) {
            switch (developmentCard) {
                case "knight" -> {
                    if (this.knightCounter > 0) {

                        // tell server that move is rob

                        pioneersService.makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(),
                                        Constants.DEVELOPMENT_CARDS.KNIGHT.toString())
                                .observeOn(Constants.FX_SCHEDULER).subscribe(res -> {

                                    if (!res._id().equals("")) {

                                        // set field off if already existing
                                        this.mapController.setFieldMode(Constants.FIELD_MODE.OFF, "",
                                                "", Color.valueOf(thisPlayer.color()));
                                        // open robber field mode
                                        this.mapController.setFieldMode(Constants.FIELD_MODE.PLACE_ROBBER,
                                                Constants.ACTION.ROB.toString(),
                                                null, Color.valueOf(thisPlayer.color()));
                                        // close menu
                                        hideMenu();
                                    }

                                });
                    }
                }
                case "road-building" -> {
                    if (this.roadBuildingCounter > 0) {
                        // tell server that move is build roads

                        pioneersService.makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(), Constants.DEVELOPMENT_CARDS.ROAD_BUILDING.toString())
                                .observeOn(Constants.FX_SCHEDULER).subscribe(res -> {

                                    if (!res._id().equals("")) {
                                        // close menu
                                        hideMenu();
                                    }
                                });
                    }
                }
                case "monopoly" -> {
                    if (this.monopolyCounter > 0) {
                        pioneersService.makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(), "monopoly").observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                            if (!result._id().equals("")) {
                                hideMenu();
                                plentyMonopolySubController.setMonopoly();
                                plentyMonopolySubController.show();
                            }
                        });

                    }
                }
                case "year-of-plenty" -> {
                    if (this.yearOfPlentyCounter > 0) {
                        pioneersService.makeMoveDevelopmentCard(Constants.ACTION.BUILD.toString(), "year-of-plenty").observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                            if (!result._id().equals("")) {
                                hideMenu();
                                plentyMonopolySubController.setPlenty();
                                plentyMonopolySubController.show();
                            }
                        });
                    }

                }
            }
        }
    }

    public void setPlayCardAllowed(Boolean playCardAllowed) {
        this.playCardAllowed = playCardAllowed;
        setCardOpacity(playCardAllowed);
    }

    public void setMapController(PioneersMapController mapController) {
        this.mapController = mapController;
    }

    public void setPlayer(Player thisPlayer) {
        this.thisPlayer = thisPlayer;
    }
}
