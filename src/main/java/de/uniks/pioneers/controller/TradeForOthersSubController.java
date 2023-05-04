package de.uniks.pioneers.controller;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.dto.ResourcesDto;
import de.uniks.pioneers.model.Move;
import de.uniks.pioneers.model.Player;
import de.uniks.pioneers.model.Resources;
import de.uniks.pioneers.model.User;
import de.uniks.pioneers.service.LoginResultStorage;
import de.uniks.pioneers.service.PioneersService;
import de.uniks.pioneers.service.PioneersUIService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class TradeForOthersSubController implements Controller {

    //Controller that deals with the UI that gets displayed when a different player offers you a trade

    private final PioneersService pioneersService;
    private final PioneersUIService pioneersUIService;
    private final LoginResultStorage loginResultStorage;
    private final TradeAndDropSubController tradeAndDropSubController;
    private final PioneersController pioneersController;
    @FXML
    public ImageView imageview_accept;
    @FXML
    public ImageView current_player_avatar;
    @FXML
    public ImageView provider_avatar;
    @FXML
    public HBox hbox_trade_give;
    @FXML
    public HBox hbox_trade_want;
    @FXML
    public Node root;
    private Parent parentFxml;
    private Pane buildMenu;
    private Map<String, User> userHashMap;
    private Map<String, Player> playerMap;

    private ResourcesDto resources;


    @Inject
    public TradeForOthersSubController(PioneersService pioneersService, PioneersUIService pioneersUIService, LoginResultStorage loginResultStorage, TradeAndDropSubController tradeAndDropSubController, PioneersController pioneersController) {
        this.pioneersService = pioneersService;
        this.pioneersUIService = pioneersUIService;
        this.loginResultStorage = loginResultStorage;
        this.tradeAndDropSubController = tradeAndDropSubController;
        this.pioneersController = pioneersController;
    }

    @Override
    public void init() {
        tradeAndDropSubController.setTradeForOthersSubController(this);
    }

    public void showMenuThings(Move move) {
        String partnerId = move.userId();
        setProviderPlayerAvatar(partnerId);
        //show offer menu on others players screen
        resources = move.resources();

        generateCard("views/images/grain_Card.png", resources.grain());
        generateCard("views/images/lumber_Card.png", resources.lumber());
        generateCard("views/images/ore_Card.png", resources.ore());
        generateCard("views/images/brick_Card.png", resources.brick());
        generateCard("views/images/wool_Card.png", resources.wool());

        checkResources();
        showMenu();
    }

    private void checkResources() {
        Resources currentResources = pioneersController.getThisPlayer().resources();
        boolean enough = resources.brick() <= currentResources.brick();
        enough &= resources.grain() <= currentResources.grain();
        enough &= resources.lumber() <= currentResources.lumber();
        enough &= resources.wool() <= currentResources.wool();
        enough &= resources.ore() <= currentResources.ore();
        imageview_accept.setOpacity(enough ? 1 : 0.3f);
        imageview_accept.setDisable(!enough);
    }

    @Override
    public void destroy() {

    }

    @Override
    public Parent render() {
        final Parent parent;
        if (parentFxml == null) {
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/trade_offer.fxml"));
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

        hideTheMenu();

        return parent;
    }

    public void setProviderPlayerAvatar(String id) {
        Player player = playerMap.get(id);
        User user = userHashMap.get(id);
        pioneersUIService.generatePlayerPortrait(provider_avatar, user.avatar(), Color.valueOf(player.color()), 48);
    }

    public void setPlayerPortrait(Player thisPlayer) {
        pioneersUIService.generatePlayerPortrait(current_player_avatar, loginResultStorage.getLoginResult().avatar(), Color.valueOf(thisPlayer.color()), 48);
    }

    public void showMenu() {
        root.setVisible(true);
        root.setMouseTransparent(false);
    }

    public void setParent(Parent parent) {
        this.parentFxml = parent;
    }

    public void setBuildMenu(Pane buildMenu) {
        this.buildMenu = buildMenu;
    }

    public void hideTheMenu() {
        root.setVisible(false);
        root.setMouseTransparent(true);
        hbox_trade_give.getChildren().clear();
        hbox_trade_want.getChildren().clear();
        if (parentFxml != null) {
            buildMenu.setVisible(true);
        }
        imageview_accept.setOpacity(1);
        imageview_accept.setDisable(false);
    }

    public void closeMenu() {
        hideTheMenu();
        pioneersService.makeMoveAction(Constants.ACTION.OFFER.toString()).observeOn(Constants.FX_SCHEDULER).subscribe();
    }


    public void generateCard(String path, int resourceNumber) {
        if (resourceNumber != 0) {
            HBox container = resourceNumber < 0 ? hbox_trade_want : hbox_trade_give;
            resourceNumber = Math.abs(resourceNumber);
            Pane pane = new Pane();
            ImageView imageView = new ImageView();
            Image img = new Image(Objects.requireNonNull(Main.class.getResourceAsStream(path)));
            imageView.setImage(img);
            imageView.setFitWidth(38);
            imageView.setFitHeight(55);

            String resourceNumberString = String.valueOf(resourceNumber);
            Label label = new Label(resourceNumberString);
            label.setLayoutX(container == hbox_trade_give ? 23 : 0);
            label.getStyleClass().add("counter");
            label.setFont(new Font("Arial", 16));
            pane.getChildren().setAll(imageView, label);


            //add generated card to container hbox in the front or back depending on whether it is give or take
            int i = container == hbox_trade_give ? -1 : 1;
            container.getChildren().add(0, pane);
            int viewOrder = i * container.getChildren().size();
            pane.setUserData(viewOrder);
            if (container.getChildren().size() > 0) {
                container.getChildren().get(0).setViewOrder(viewOrder);
            }
        }
    }


    public void acceptOffer() {
        //new ResourcesDto(grain, brick, ore, lumber, wool),
        pioneersService.makeMoveResources(Constants.ACTION.OFFER.toString(), new ResourcesDto(-resources.grain(), -resources.brick(), -resources.ore(), -resources.lumber(), -resources.wool())).observeOn(Constants.FX_SCHEDULER).subscribe(result -> {
            if (!result._id().equals("")) {
                hideTheMenu();
            }
        });
    }

    public void setListPlayers(Map<String, Player> userIdToPlayer) {
        playerMap = userIdToPlayer;
    }

    public void setListUsers(Map<String, User> userIdToName) {
        userHashMap = userIdToName;
    }

    public void editOffer() {
        if (!tradeAndDropSubController.root.isVisible()) {
            tradeAndDropSubController.showMenu(true);
            pioneersController.hideDevelopmentMenu();
            tradeAndDropSubController.editTradeOffer(new ResourcesDto(-resources.grain(), -resources.brick(), -resources.ore(), -resources.lumber(), -resources.wool()));
        }
    }
}