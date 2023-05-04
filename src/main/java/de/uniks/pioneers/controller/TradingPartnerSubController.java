package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.Move;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.AnimationService;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class TradingPartnerSubController implements Controller {

    //Controller that deals with the UI that gets displayed FOR THE PLAYER that made the trade request

    private final PioneersUIService pioneersUIService;
    private final PioneersService pioneersService;
    private final LoginResultStorage loginResultStorage;
    private final ResourceBundle resourceBundle;
    private final AnimationService animationService;
    private final PioneersController pioneersController;

    @FXML
    public Node root;
    @FXML
    public HBox hbox_trade_players;
    @FXML
    public ImageView current_player;
    @FXML
    public HBox hbox_trade_give;
    @FXML
    public HBox hbox_trade_get;
    @FXML
    public ImageView partner_avatar;
    @FXML
    public HBox hbox_counteroffer;
    @FXML
    public ScrollPane scrollpane_accept;
    @FXML
    public ScrollPane scrollpane_offer;
    @FXML
    public ImageView image_check;
    @FXML
    public ImageView imageview_reject;
    private Parent parentFxml;

    private HashMap<String, Integer> resourceDtoList = new HashMap<>();

    private Map<String, Player> userIdToPlayer = new HashMap<>();

    private Map<String, User> userIdToUserInfo = new HashMap<>();

    private ResourcesDto resourcesDtoSendOff;

    //saves the id of the currently selected player
    private String playerSelected;
    private boolean active = false;
    private boolean acceptPhase = false;
    private double giveHboxLayoutX;
    private boolean spectator = false;
    private User offerUser;

    @Inject
    public TradingPartnerSubController(PioneersUIService pioneersUIService, PioneersService pioneersService, LoginResultStorage loginResultStorage, ResourceBundle resourceBundle, AnimationService animationService, PioneersController pioneersController) {
        this.pioneersUIService = pioneersUIService;
        this.pioneersService = pioneersService;
        this.loginResultStorage = loginResultStorage;
        this.resourceBundle = resourceBundle;
        this.animationService = animationService;
        this.pioneersController = pioneersController;
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
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/trade_accept_list.fxml"), resourceBundle);
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
        giveHboxLayoutX = hbox_trade_give.getLayoutX();
        //show resources
        return parent;
    }

    public void setPlayerPortrait(Player thisPlayer) {
        // get correct avatar: trade offer if spectator, else the current
        String avatar = spectator ? offerUser.avatar() : loginResultStorage.getLoginResult().avatar();
        pioneersUIService.generatePlayerPortrait(current_player, avatar, Color.valueOf(thisPlayer.color()), 48);
    }

    private void renderItem(Move move) {
        //only show player in list if his offer isn't empty
        if (move.resources() == null) {
            return;
        }

        //show trade Partner
        String userId = move.userId();
        String avatar = userIdToUserInfo.get(userId).avatar();

        ImageView portrait = pioneersUIService.generatePlayerPortrait(avatar, Color.valueOf(userIdToPlayer.get(userId).color()), 48);
        portrait.setUserData(move);
        portrait.setId(userId);

        portrait.setOnMouseClicked(this::onImageClicked);
        portrait.setOnMouseEntered(this::onImageHoverEnter);
        portrait.setOnMouseExited(this::onImageHoverExit);
        if (move.resources().equals(new ResourcesDto(-resourcesDtoSendOff.grain(), -resourcesDtoSendOff.brick(), -resourcesDtoSendOff.ore(), -resourcesDtoSendOff.lumber(), -resourcesDtoSendOff.wool()))) {
            hbox_trade_players.getChildren().add(portrait);
        } else {
            hbox_counteroffer.getChildren().add(portrait);
        }
    }

    public void generateCard(String path, int resourceNumber, HBox container) {
        Pane pane = new Pane();
        pane.setPrefHeight(38);
        pane.setPrefHeight(55);
        ImageView imageView = new ImageView();
        Image img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(path)));
        pane.setOnMouseEntered(this::onMouseEnterResource);
        pane.setOnMouseExited(this::onMouseExitResource);

        String[] parts = path.split("/");
        imageView.setImage(img);
        imageView.setId(parts[2]);
        imageView.setFitWidth(38);
        imageView.setFitHeight(55);

        String resourceNumberString = String.valueOf(resourceNumber);
        Label label = new Label(resourceNumberString);
        label.setFont(new Font("Arial", 12)); // old 16
        label.setLayoutX(container == hbox_trade_get ? 24 : 0);
        label.setLayoutY(-3);
        label.getStyleClass().add("counter");
        container.getChildren().add(0, pane);
        pane.getChildren().setAll(imageView, label);
        int i = container == hbox_trade_get ? -1 : 1;
        int viewOrder = i * container.getChildren().size();
        pane.setUserData(viewOrder);
        if (container.getChildren().size() > 0) {
            container.getChildren().get(0).setViewOrder(viewOrder);
        }

    }

    private void onMouseExitResource(MouseEvent mouseEvent) {
        Pane pane = (Pane) mouseEvent.getSource();
        animationService.moveNode(pane.getChildren().get(0), 0, 0, 200, Interpolator.EASE_BOTH);
        animationService.moveNode(pane.getChildren().get(1), 0, 0, 200, Interpolator.EASE_BOTH);
        pane.setViewOrder((int) pane.getUserData());
    }

    private void onMouseEnterResource(MouseEvent mouseEvent) {
        Pane pane = (Pane) mouseEvent.getSource();
        animationService.moveNode(pane.getChildren().get(0), 0, -20, 200, Interpolator.EASE_BOTH);
        animationService.moveNode(pane.getChildren().get(1), 0, -20, 200, Interpolator.EASE_BOTH);
        pane.setViewOrder(-10);
    }


    private void onImageClicked(MouseEvent mouseEvent) {
        ImageView imageView = (ImageView) mouseEvent.getTarget();
        if (mouseEvent.getClickCount() == 1) {
            playerSelected = imageView.getId();
            Move move = (Move) imageView.getUserData();
            pioneersUIService.generatePlayerPortrait(partner_avatar, imageView.getImage().getUrl(), Color.valueOf(userIdToPlayer.get(move.userId()).color()), 48);
            showOffer(resourcesToHashMap(move.resources(), true));
            if (!spectator && pioneersService.canAffordCounterOffer(move.resources(), pioneersController.getThisPlayer().resources()) && acceptPhase) {
                image_check.setDisable(false);
                image_check.setOpacity(1f);
            }
        }
    }

    private void onImageHoverEnter(MouseEvent mouseEvent) {
        ImageView imageView = (ImageView) mouseEvent.getTarget();
        imageView.setScaleX(1.1);
        imageView.setScaleY(1.1);
    }

    private void onImageHoverExit(MouseEvent mouseEvent) {
        ImageView imageView = (ImageView) mouseEvent.getTarget();
        imageView.setScaleX(1);
        imageView.setScaleY(1);
    }

    public void hideMenu() {
        root.setVisible(false);
        root.setMouseTransparent(true);
        hbox_trade_players.getChildren().clear();
        hbox_trade_give.getChildren().clear();
        hbox_trade_get.getChildren().clear();
        hbox_counteroffer.getChildren().clear();
        partner_avatar.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/player.png"))));
        partner_avatar.setEffect(null);
        playerSelected = null;
        active = false;
        acceptPhase = false;
        resourceDtoList.clear();
        setButtonState(false);
    }

    public void showMenu() {
        root.setVisible(true);
        root.setMouseTransparent(false);
        active = true;
        image_check.setDisable(true);
        image_check.setOpacity(0.3f);
        imageview_reject.setDisable(true);
        imageview_reject.setOpacity(0.3f);
        showOffer(resourceDtoList);
    }

    private void showOffer(HashMap<String, Integer> res) {
        hbox_trade_give.getChildren().clear();
        hbox_trade_get.getChildren().clear();
        for (Map.Entry<String, Integer> entry : res.entrySet()) {
            Integer resource = entry.getValue();
            String path = entry.getKey();
            if (resource != 0) {
                HBox container = resource < 0 ? hbox_trade_give : hbox_trade_get;
                generateCard(path, Math.abs(resource), container);
            }
        }
        hbox_trade_give.setLayoutX(giveHboxLayoutX - (hbox_trade_give.getChildren().size() - 1) * 10);
    }

    public void acceptOffer() {
        //build
        pioneersService.makeMoveTrade(Constants.ACTION.ACCEPT.toString(), resourcesDtoSendOff, playerSelected).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            if (!result._id().equals("")) {
                hideMenu();
                pioneersController.setDevelopmentCardAllowed(true);
            }
        });
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void setMove(Move move) {
        if (active) {
            renderItem(move);
        }
    }

    public void setUserList(Map<String, User> userIdToUserInfo) {
        this.userIdToUserInfo = userIdToUserInfo;
    }

    public void setPlayerList(Map<String, Player> userIdToPlayer) {
        this.userIdToPlayer = userIdToPlayer;
    }

    public void setResourcesDto(ResourcesDto resourcesDto) {
        this.resourcesDtoSendOff = resourcesDto;
        resourceDtoList = resourcesToHashMap(resourcesDtoSendOff, false);
    }

    public HashMap<String, Integer> resourcesToHashMap(ResourcesDto resourcesDto, boolean invert) {
        HashMap<String, Integer> hashMap = new HashMap<>();
        int i = invert ? -1 : 1;
        hashMap.put("views/images/grain_Card.png", i * resourcesDto.grain());
        hashMap.put("views/images/brick_Card.png", i * resourcesDto.brick());
        hashMap.put("views/images/ore_Card.png", i * resourcesDto.ore());
        hashMap.put("views/images/lumber_Card.png", i * resourcesDto.lumber());
        hashMap.put("views/images/wool_Card.png", i * resourcesDto.wool());
        return hashMap;
    }

    public void closeMenu() {
        pioneersService.makeMoveTrade(Constants.ACTION.ACCEPT.toString(), null, null).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            if (!result._id().equals("")) {
                hideMenu();
                pioneersController.setDevelopmentCardAllowed(true);
            }
        });
    }

    public void checkForOffer() {
        if (active) {
            acceptPhase = true;
            int offers = hbox_trade_players.getChildren().size();
            int counterOffers = hbox_counteroffer.getChildren().size();
            if (offers + counterOffers == 0) {
                closeMenu();
            } else {
                if (playerSelected != null) {
                    image_check.setDisable(false);
                    image_check.setOpacity(1);
                }
                // disable reject button for spectator
                if (!spectator) {
                    imageview_reject.setDisable(false);
                    imageview_reject.setOpacity(1f);
                }
            }
        }
    }

    public void setButtonState(boolean active) {
        imageview_reject.setOpacity(active ? 1.0 : 0.3);
        imageview_reject.setDisable(!active);
        image_check.setOpacity(active ? 1.0 : 0.3);
        image_check.setDisable(!active);
    }

    public boolean isVisible() {
        return root.isVisible();
    }

    public void setSpectator(boolean spectator, User offerUser) {

        this.spectator = spectator;
        this.offerUser = offerUser;
    }
}