package de.uniks.pioneers.controller;

import de.uniks.pioneers.App;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.Resources;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class TradeAndDropSubController implements Controller {

    private final PioneersService pioneersService;
    private final PioneersUIService pioneersUIService;
    private final LoginResultStorage loginResultStorage;
    private final PioneersController pioneersController;
    private final App app;
    private final List<Label> cardLabels = new ArrayList<>();
    private final List<Boolean> cardGenerated = new ArrayList<>();
    private final List<String> imagePath = new ArrayList<>();
    private final List<Integer> resources = new ArrayList<>();
    private final HashMap<Integer, Label> resToLabel = new HashMap<>();
    private final ResourceBundle resourceBundle;
    private final Map<String, Player> userIdToPlayer;
    @FXML
    public ImageView image_close;
    @FXML
    public ImageView image_check;
    @FXML
    public Label label_ore;
    @FXML
    public Label label_lumber;
    @FXML
    public Label label_wool;
    @FXML
    public Label label_brick;
    @FXML
    public Label label_grain;
    @FXML
    public ImageView trade_partner_player;
    @FXML
    public Label label_ratio_lumber;
    @FXML
    public Label label_ratio_wool;
    @FXML
    public Label label_ratio_ore;
    @FXML
    public Label label_ratio_brick;
    @FXML
    public Label label_ratio_grain;
    @FXML
    public ImageView own_avatar;
    @FXML
    public HBox hbox_give;
    @FXML
    public HBox hbox_gain;
    @FXML
    public Label drop_text_left;
    @FXML
    public Label drop_text_left_counter;
    @FXML
    public Group trade_rate_elements_group;     //The right side of the menu (with the rates) unified into a simple to toggle group.
    @FXML
    public Group trade_misc_elements_group;     //The top side of the menu w/o the check and hBox.
    private TradeForOthersSubController tradeForOthersSubController;
    private TradingPartnerSubController tradingPartnerSubController;
    @FXML
    public Node root;
    @FXML
    private Label drop_text;
    private Parent parentFxml;
    private Pane buildMenu;
    private Resources ownResources;
    private int resourcesCount;
    private int[] resourceCountersSell = new int[5];
    private int[] resourceCountersBuy = new int[5];
    private boolean bank = false;
    private boolean tradeMode = true;
    private boolean editMode;
    private int totalResourcesAdded = 0;            //Amount of resources put into the top hbox to be sent for discard/trade
    private Boolean justWithBank = false;
    private boolean containsRedCounter = false;

    @Inject
    public TradeAndDropSubController(PioneersService pioneersService, PioneersUIService pioneersUIService, LoginResultStorage loginResultStorage, PioneersController pioneersController,
                                     TradingPartnerSubController tradingPartnerSubController, App app, ResourceBundle resourceBundle, Map<String, Player> userIdToPlayer) {
        this.pioneersService = pioneersService;
        this.pioneersUIService = pioneersUIService;
        this.loginResultStorage = loginResultStorage;
        this.pioneersController = pioneersController;
        this.tradingPartnerSubController = tradingPartnerSubController;
        this.app = app;
        this.resourceBundle = resourceBundle;
        this.userIdToPlayer = userIdToPlayer;
    }

    @Override
    public void init() {
        imagePath.add("views/images/lumber_Card.png");
        imagePath.add("views/images/wool_Card.png");
        imagePath.add("views/images/ore_Card.png");
        imagePath.add("views/images/brick_Card.png");
        imagePath.add("views/images/grain_Card.png");
        resToLabel.put(0, label_ratio_lumber);
        resToLabel.put(1, label_ratio_wool);
        resToLabel.put(2, label_ratio_ore);
        resToLabel.put(3, label_ratio_brick);
        resToLabel.put(4, label_ratio_grain);
    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {

        final Parent parent;
        if (parentFxml == null) {
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/trade_menu_player.fxml"), resourceBundle);
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

        hideMenu();

        hbox_give.getChildren().addListener((ListChangeListener<? super Node>) e -> {
            while (e.next()) {
                // enable button when you offer only available cards
                if (e.wasAdded() && !containsRedCounter) {
                    image_check.setDisable(false);
                    image_check.setOpacity(1);
                }
                // disable button when you offer no cards
                if (e.wasRemoved() && hbox_give.getChildren().size() == 0) {
                    containsRedCounter = false;
                    image_check.setDisable(true);
                    image_check.setOpacity(0.3);
                }
            }
        });

        return parent;
    }

    public void setPlayerPortrait(Player thisPlayer) {
        pioneersUIService.generatePlayerPortrait(own_avatar, loginResultStorage.getLoginResult().avatar(), Color.valueOf(thisPlayer.color()), 64);
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void showMenu(boolean tradeMode) {
        if (buildMenu != null) buildMenu.setVisible(false);
        root.setVisible(true);
        root.setMouseTransparent(false);
        checkForHarbors();
        switchMode(tradeMode);

        //fill list with placeholder for access
        for (int i = 0; i < 10; i++) {
            cardGenerated.add(false);
            cardLabels.add(null);
        }
    }

    public void switchMode(boolean tradeMode) {
        // Sets the mode of the Controller by hiding/showing certain elements over others.
        this.tradeMode = tradeMode;
        trade_rate_elements_group.setVisible(tradeMode);
        trade_misc_elements_group.setVisible(tradeMode);
        image_check.setLayoutY(tradeMode ? 38 : 58);
        image_check.setDisable(!tradeMode);
        image_check.setOpacity(tradeMode ? 1.0f : 0.3f);
        hbox_give.setLayoutX(tradeMode ? 181 : 39);
        drop_text.setVisible(!tradeMode);
        drop_text_left.setVisible(!tradeMode);
        drop_text_left_counter.setVisible(!tradeMode);
        if (!tradeMode) {
            totalResourcesAdded = 0;
            updateDropResourcesLeftCounter(totalResourcesAdded);
        }
    }

    public void hideMenu() {
        if (buildMenu != null) {
            buildMenu.setVisible(true);
        }
        root.setVisible(false);
        hbox_gain.getChildren().clear();
        hbox_give.getChildren().clear();
        if (parentFxml != null) {
            buildMenu.setVisible(true);
        }
        root.setMouseTransparent(true);

        bank = false;
        trade_partner_player.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/player.png"))));
        resourceCountersSell = new int[5];
        resourceCountersBuy = new int[5];

        //clear all temp saved labels and boolean
        cardGenerated.clear();
        cardLabels.clear();

        this.editMode = false;
        this.resources.clear();
    }

    public void setBuildMenu(Pane buildMenu) {
        this.buildMenu = buildMenu;
    }

    public void setCurrentResources(Resources currentResources) {
        this.ownResources = currentResources;
        pioneersUIService.updateResourceLabels(label_lumber, label_wool, label_ore, label_brick, label_grain, null, ownResources);
        resourcesCount = currentResources.brick() + currentResources.lumber() + currentResources.ore() + currentResources.grain() + currentResources.wool();
        if (!tradeMode) {
            updateDropResourcesLeftCounter(totalResourcesAdded);
        }
    }

    public int getResourcesCount() {
        return resourcesCount;
    }

    public boolean isVisible() {
        return root.isVisible();
    }

    public void setPlayer(Player thisPlayer) {
        DropShadow dropShadow = (DropShadow) own_avatar.getEffect();
        dropShadow.setColor(Color.valueOf(thisPlayer.color()));
    }

    public void setBank() {
        //don't set bank if a counteroffer is edited
        if (editMode) {
            return;
        }
        checkForHarbors(); // remove this if performance is crap, is only here that tests run smoother
        for (int resource = 0; resource <= 4; resource++) {
            if (resourceCountersSell[resource % 5] == 2 && (IntStream.of(resourceCountersSell).sum()) == 2 && resToLabel.get(resource).getText().equals("2 : 1") && (IntStream.of(resourceCountersBuy).sum()) == 1) {
                trade_partner_player.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/bank.png"))));
                bank = true;
            } else if (resourceCountersSell[resource % 5] == 3 && (IntStream.of(resourceCountersSell).sum()) == 3 && resToLabel.get(resource).getText().equals("3 : 1") && (IntStream.of(resourceCountersBuy).sum()) == 1) {
                trade_partner_player.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/bank.png"))));
                bank = true;
            } else if (resourceCountersSell[resource % 5] == 4 && (IntStream.of(resourceCountersSell).sum()) == 4 && resToLabel.get(resource).getText().equals("4 : 1") && (IntStream.of(resourceCountersBuy).sum()) == 1) {
                trade_partner_player.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/bank.png"))));
                bank = true;
            }
        }
    }

    public void resetBank() {
        trade_partner_player.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/player.png"))));
        bank = false;
    }

    public void increaseLumber() {
        card(0, label_lumber);
    }

    public void increaseWool() {
        card(1, label_wool);
    }

    public void increaseOre() {
        card(2, label_ore);
    }

    public void increaseBrick() {
        card(3, label_brick);
    }

    public void increaseGrain() {
        card(4, label_grain);
    }

    //resource: 0 = lumber, 1 = wool, 2 = ore, 3 = brick, 4 = grain
    public void card(int resource, Label linkedLabel) {
        int amount = Integer.parseInt(linkedLabel.getText());
        if (amount > 0) {
            if (!tradeMode) {
                updateDropResourcesLeftCounter(++totalResourcesAdded);
            }
            if (!cardGenerated.get(resource)) {
                HBox container = resource < 5 ? hbox_give : hbox_gain;
                generateCard(imagePath.get(resource % 5), linkedLabel, container, resource);
                resourceCountersSell[resource % 5] = 1;
                resetBank();
            } else {
                int counter = Integer.parseInt(cardLabels.get(resource).getText()) + 1;
                resourceCountersSell[resource % 5] = counter;
                String newText = Integer.toString(counter);
                Label label = cardLabels.get(resource);
                label.setText(newText);
                resetBank();
                setBank();
            }
            linkedLabel.setText(Integer.toString(--amount));
        }
    }

    //the resources' int encodes which labels have to be set to indicate the traded/remaining amount
    //the range indicates whether its given or taken resources
    //1-5 = give, 6-10 = take
    //the actual number specifies the set resource
    //0 = lumber, 1 = wool, 2 = ore, 3 = brick, 4 = grain
    //5 = lumber, 6 = wool, 7 = ore, 8 = brick, 9 = grain
    public void generateCard(String path, Label linkedLabel, HBox container, int resource) {
        Pane pane = new Pane();
        ImageView imageView = new ImageView();
        Image img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(path)));
        imageView.setImage(img);
        imageView.getStyleClass().add("card");
        imageView.setId("generatedCard_" + resource);

        Label label = new Label("1");
        cardLabels.set(resource, label);
        label.setFont(new Font("Arial", 20));
        //set style to redCounter if editMode is active, and you cannot afford this amount of the resource
        label.getStyleClass().add(editMode ? (this.resources.get(resource % 5) < 0 ? "redCounter" : "counter") : "counter");

        // disable accept button if you do not have enough resources
        if (label.getStyleClass().contains("redCounter")) {
            image_check.setDisable(true);
            image_check.setOpacity(0.3);
            // there was at least one resource that you do not have in your hand cards
            containsRedCounter = true;
        }
        label.setLayoutX(30);
        label.setMouseTransparent(true);

        pane.getChildren().setAll(imageView, label);

        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (!tradeMode) {
                updateDropResourcesLeftCounter(--totalResourcesAdded);
            }
            //decrease counter of generated card
            int counter = Integer.parseInt(label.getText()) - 1;
            if (0 <= resource && resource <= 4) {
                sellDecreased(resource, counter);
            } else {
                buyDecreased(resource, counter);
            }
            String newText = Integer.toString(counter);
            label.setText(newText);
            //increment linked counter if generated card is decremented
            if (linkedLabel != null) {
                if (!this.editMode || this.resources.get(resource) >= 0) {
                    linkedLabel.setText(Integer.toString(Integer.parseInt(linkedLabel.getText()) + 1));
                } else {
                    this.resources.set(resource, this.resources.get(resource) + 1);
                    if (this.resources.get(resource) >= 0) {
                        label.getStyleClass().removeAll("redCounter");
                        label.getStyleClass().add("counter");
                    }
                }
            }
            //delete card if counter hits zero
            if (counter == 0) {
                container.getChildren().remove(pane);
                cardGenerated.set(resource, false);
            }
        });

        //add generated card to container hbox in the front or back depending on whether it is give or take
        if (resource >= 5) {
            container.getChildren().add(0, pane);
        } else {
            container.getChildren().add(pane);
        }

        cardGenerated.set(resource, true);
    }

    private void buyDecreased(int resource, int counter) {
        resourceCountersBuy[resource % 5] = counter;
        if ((IntStream.of(resourceCountersBuy).sum()) == 1) {
            setBank();
        } else {
            resetBank();
        }
    }

    private void updateDropResourcesLeftCounter(int newTotal) {
        int goalValue = (int) Math.floor(resourcesCount * 0.5);
        drop_text_left_counter.setText(newTotal + "/" + goalValue);
        boolean correctValue = newTotal == goalValue;
        drop_text_left_counter.setTextFill(correctValue ? Color.GREEN : Color.RED);
        image_check.setOpacity(correctValue ? 1.0 : 0.3);
        image_check.setDisable(!correctValue);
    }

    private void sellDecreased(int resource, int counter) {
        resourceCountersSell[resource % 5] = counter;
        resetBank();
        setBank();
    }

    public void increaseLumberGained() {
        cardReceive(5);
    }

    public void increaseWoolGained() {
        cardReceive(6);
    }

    public void increaseOreGained() {
        cardReceive(7);
    }

    public void increaseBrickGained() {
        cardReceive(8);
    }

    public void increaseGrainGained() {
        cardReceive(9);
    }

    public void cardReceive(int resource) {
        if (!cardGenerated.get(resource)) {
            generateCard(imagePath.get(resource % 5), null, hbox_gain, resource);
            resourceCountersBuy[resource % 5] = 1;
            if ((IntStream.of(resourceCountersBuy).sum()) > 1) {
                resetBank();
            } else if ((IntStream.of(resourceCountersBuy).sum()) == 1) {
                setBank();
            }
        } else {
            resetBank();
            int counter = Integer.parseInt(cardLabels.get(resource).getText()) + 1;
            resourceCountersBuy[resource % 5] = counter;
            String newText = Integer.toString(counter);
            cardLabels.get(resource).setText(newText);
        }
    }

    public void sendOffer() {
        int lumber = cardLabels.get(0) != null ? Integer.parseInt(cardLabels.get(0).getText()) : 0;
        int wool = cardLabels.get(1) != null ? Integer.parseInt(cardLabels.get(1).getText()) : 0;
        int ore = cardLabels.get(2) != null ? Integer.parseInt(cardLabels.get(2).getText()) : 0;
        int brick = cardLabels.get(3) != null ? Integer.parseInt(cardLabels.get(3).getText()) : 0;
        int grain = cardLabels.get(4) != null ? Integer.parseInt(cardLabels.get(4).getText()) : 0;
        if (cardLabels.get(5) != null) {
            lumber -= Integer.parseInt(cardLabels.get(5).getText());
        }
        if (cardLabels.get(6) != null) {
            wool -= Integer.parseInt(cardLabels.get(6).getText());
        }
        if (cardLabels.get(7) != null) {
            ore -= Integer.parseInt(cardLabels.get(7).getText());
        }
        if (cardLabels.get(8) != null) {
            brick -= Integer.parseInt(cardLabels.get(8).getText());
        }
        if (cardLabels.get(9) != null) {
            grain -= Integer.parseInt(cardLabels.get(9).getText());
        }

        //check if the player can afford the counteroffer
        for (int i : this.resources) {
            if (i < 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText(resourceBundle.getString("cannot.afford"));
                alert.initOwner(app.getPrimaryStage());
                alert.showAndWait();
                return;
            }
        }

        //check that you don't initiate a trade that only offers or only demands resources
        if (!(lumber >= 0 && wool >= 0 && ore >= 0 && brick >= 0 && grain >= 0) &&
                !(lumber <= 0 && wool <= 0 && ore <= 0 && brick <= 0 && grain <= 0)) {
            ResourcesDto resourcesDto = new ResourcesDto(-grain, -brick, -ore, -lumber, -wool);

            if (userIdToPlayer != null) {
                justWithBank = userIdToPlayer.size() < 2;
            }

            if (bank) {
                pioneersService.makeMoveTrade(Constants.ACTION.BUILD.toString(), resourcesDto, "684072366f72202b72406465").observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                    if (result._id().equals("")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.initOwner(app.getPrimaryStage());
                        alert.showAndWait();
                    } else {
                        this.hideMenu();
                    }
                });
            } else if (!justWithBank) {
                String action = editMode ? Constants.ACTION.OFFER.toString() : Constants.ACTION.BUILD.toString();
                pioneersService.makeMoveResources(action, resourcesDto).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                    if (result._id().equals("")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.initOwner(app.getPrimaryStage());
                        alert.showAndWait();
                    } else {
                        if (!editMode) {
                            tradingPartnerSubController.setResourcesDto(resourcesDto);
                            tradingPartnerSubController.showMenu();
                            pioneersController.setDevelopmentCardAllowed(false);
                        } else {
                            tradeForOthersSubController.hideTheMenu();
                        }
                        this.hideMenu();
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("No Players to trade with");
                alert.initOwner(app.getPrimaryStage());
                alert.showAndWait();
            }


        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(resourceBundle.getString("no.empty.trade"));
            alert.initOwner(app.getPrimaryStage());
            alert.showAndWait();
        }
    }

    public void sendDiscard() {
        int lumber = Integer.parseInt(label_lumber.getText()) - ownResources.lumber();
        int wool = Integer.parseInt(label_wool.getText()) - ownResources.wool();
        int ore = Integer.parseInt(label_ore.getText()) - ownResources.ore();
        int brick = Integer.parseInt(label_brick.getText()) - ownResources.brick();
        int grain = Integer.parseInt(label_grain.getText()) - ownResources.grain();

        int prevResourcesCount = resourcesCount;

        int discardCount = (-1) * (lumber + wool + ore + brick + grain);
        if (resourcesCount % 2 == 1) {
            resourcesCount -= 1;
        }

        // check if you take some cards from player or not
        if ((lumber <= 0 && wool <= 0 && ore <= 0 && brick <= 0 && grain <= 0)) {
            if (resourcesCount == discardCount * 2) {
                pioneersService.makeMoveResources(Constants.ACTION.DROP.toString(), new ResourcesDto(grain, brick, ore, lumber, wool)).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
                    if (result._id().equals("")) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setContentText("result ist null");
                        alert.initOwner(app.getPrimaryStage());
                        alert.showAndWait();
                        resourcesCount = prevResourcesCount;
                    } else {
                        //hide the menu only if the move is successful
                        hideMenu();
                    }
                });
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("It's not half.");
                alert.initOwner(app.getPrimaryStage());
                alert.showAndWait();
                resourcesCount = prevResourcesCount;
            }
        }
    }

    public void sendRequest() {
        //Happens when you click on the Checkmark - sends a Server Request depending on the mode of the controller.
        if (tradeMode) {
            sendOffer();
        } else {
            sendDiscard();
        }
    }

    public void tradeWith() {
        //toggle to player, if you don't want to trade with the bank
        if (bank) {
            resetBank();
        } else {
            setBank();
        }
    }

    public void checkForHarbors() {
        // get harbors and set labels
        ArrayList<String> myHarbors = pioneersController.getMyHarbors();
        if (myHarbors.contains("null") && label_ratio_lumber.getText().equals("4 : 1")) {
            label_ratio_lumber.setText("3 : 1");
            label_ratio_wool.setText("3 : 1");
            label_ratio_ore.setText("3 : 1");
            label_ratio_brick.setText("3 : 1");
            label_ratio_grain.setText("3 : 1");
        }

        myHarbors.removeAll(Collections.singleton(null));

        for (var type : myHarbors) {
            switch (type) {
                case "lumber" -> label_ratio_lumber.setText("2 : 1");
                case "wool" -> label_ratio_wool.setText("2 : 1");
                case "ore" -> label_ratio_ore.setText("2 : 1");
                case "brick" -> label_ratio_brick.setText("2 : 1");
                case "grain" -> label_ratio_grain.setText("2 : 1");
            }
        }
    }

    public boolean getBank() {
        return bank;
    }

    public void setTradingPartnerController(TradingPartnerSubController tradingPartnerController) {
        this.tradingPartnerSubController = tradingPartnerController;
    }

    //negative resources must be give while positive one are gained
    public void editTradeOffer(ResourcesDto resourcesDto) {
        this.editMode = true;
        Resources resources = pioneersController.getThisPlayer().resources();

        //save how many resources the player would have if he accepted the initial offer
        //negative values mean that the player would be in debt which makes this trade impossible
        this.resources.add(resources.lumber() + Math.min(0, resourcesDto.lumber()));
        this.resources.add(resources.wool() + Math.min(0, resourcesDto.wool()));
        this.resources.add(resources.ore() + Math.min(0, resourcesDto.ore()));
        this.resources.add(resources.brick() + Math.min(0, resourcesDto.brick()));
        this.resources.add(resources.grain() + Math.min(0, resourcesDto.grain()));

        List<Integer> resourceList = new ArrayList<>();
        resourceList.add(resourcesDto.lumber());
        resourceList.add(resourcesDto.wool());
        resourceList.add(resourcesDto.ore());
        resourceList.add(resourcesDto.brick());
        resourceList.add(resourcesDto.grain());

        //set the current resources as if the player has enough to engage in the initial trade
        setCurrentResources(new Resources(0, Math.abs(resourcesDto.grain()), Math.abs(resourcesDto.brick()), Math.abs(resourcesDto.ore()), Math.abs(resourcesDto.lumber()), Math.abs(resourcesDto.wool())));
        //show initial trade offer
        int counter = 0;
        for (int res : resourceList) {
            for (int j = 0; j < Math.abs(res); j++) {
                if (Math.signum(res) == 1) {
                    cardReceive(counter + 5);
                } else {
                    switch (counter) {
                        case 0 -> card(counter, label_lumber);
                        case 1 -> card(counter, label_wool);
                        case 2 -> card(counter, label_ore);
                        case 3 -> card(counter, label_brick);
                        case 4 -> card(counter, label_grain);
                    }
                }
            }
            counter++;
        }

        //set the current resources to what the player would have if he accepted the initial offer excluding debts
        setCurrentResources(new Resources(0, Math.max(0, this.resources.get(4)), Math.max(0, this.resources.get(3)), Math.max(0, this.resources.get(2)), Math.max(0, this.resources.get(0)), Math.max(0, this.resources.get(1))));
    }

    public void setTradeForOthersSubController(TradeForOthersSubController tradeForOthersSubController) {
        this.tradeForOthersSubController = tradeForOthersSubController;
    }
}
