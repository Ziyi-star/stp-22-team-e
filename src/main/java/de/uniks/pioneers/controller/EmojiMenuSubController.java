package de.uniks.pioneers.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.pavlobu.emojitextflow.EmojiImageCache;
import com.pavlobu.emojitextflow.EmojiParser;
import de.uniks.pioneers.Main;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class EmojiMenuSubController implements Controller {

    private final ResourceBundle resourceBundle;
    private static final boolean SHOW_MISC = false;

    private TextField textField_lobbyChat_messageInput;
    private boolean testEmojiMenu;

    @FXML
    private Pane rootEmojiMenu;
    @FXML
    private ScrollPane searchScrollPane;
    @FXML
    private FlowPane searchFlowPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<Image> boxTone;

    public EmojiMenuSubController(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    @Override
    public void init() {
        initialize();
    }

    @Override
    public void destroy() {}

    @Override
    public Parent render() {
        final Parent parent;
        if (testEmojiMenu) {
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/emojiMenu.fxml"), resourceBundle);
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
        return parent;
    }

    public void toggleMenu() {
        if (rootEmojiMenu.isVisible()) {
            rootEmojiMenu.setVisible(false);
            rootEmojiMenu.setMouseTransparent(true);
        } else {
            rootEmojiMenu.setVisible(true);
            rootEmojiMenu.setMouseTransparent(false);
            rootEmojiMenu.toFront();
        }
    }

    public void setEmojiMenu(Pane rootEmojiMenu, ScrollPane searchScrollPane, FlowPane searchFlowPane, TabPane tabPane, TextField txtSearch, ComboBox<Image> boxTone) {
        // pane (root) on which emoji menu is based on
        this.rootEmojiMenu = rootEmojiMenu;
        // new scroll pane appears when searched in text field
        this.searchScrollPane = searchScrollPane;
        // flow pane groups emojis in same length rows on search scroll pane
        this.searchFlowPane = searchFlowPane;
        // tab pane for different emoji category tabs
        this.tabPane = tabPane;
        // text field to search emojis
        this.txtSearch = txtSearch;
        // combo box where emoji skin tone can be chosen from list
        this.boxTone = boxTone;
    }

    void initialize() {
        if (!SHOW_MISC) {
            // show only 2 emoji categories
            tabPane.getTabs().remove(2, tabPane.getTabs().size());
        }
        // list for the number of emoji skin tones
        ObservableList<Image> tonesList = FXCollections.observableArrayList();

        // get 5 skin tones with different color
        for (int i = 1; i <= 5; i++) {
            Emoji emoji = EmojiParser.getInstance().getEmoji(":thumbsup_tone"+ i + ":");
            Image image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex()));
            tonesList.add(image);
        }
        // default skin tone
        Emoji em = EmojiParser.getInstance().getEmoji(":thumbsup:");
        Image image = EmojiImageCache.getInstance().getImage(getEmojiImagePath(em.getHex()));
        tonesList.add(image);

        // put thumbs up image with different skin tones to combo box in upper right corner
        boxTone.setItems(tonesList);
        // rendering of items in combo box
        boxTone.setCellFactory(e->new ToneCell());
        boxTone.setButtonCell(new ToneCell());
        // call refreshTabs method as soon as emoji skin tone selection is selected or changed
        boxTone.getSelectionModel().selectedItemProperty().addListener(e->refreshTabs());

        // only vertical scrolling
        searchScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        searchFlowPane.prefWidthProperty().bind(searchScrollPane.widthProperty().subtract(5));
        searchFlowPane.setHgap(5);
        searchFlowPane.setVgap(5);

        // listener on text field
        txtSearch.textProperty().addListener(x-> {
            String text = txtSearch.getText();
            if (text.length() < 2) {
                // minimum search length is 2, if shorter than search results are empty
                searchFlowPane.getChildren().clear();
                searchScrollPane.setVisible(false);
            } else {
                // clear former search results from flow pane and make scroll pane visible again
                searchFlowPane.getChildren().clear();
                searchScrollPane.setVisible(true);
                // get result list of emojis from input in text field
                List<Emoji> results = EmojiParser.getInstance().search(text);
                // add the new results to flow pane
                results.forEach(emoji ->searchFlowPane.getChildren().add(createEmojiNode(emoji)));
            }
        });

        // initialize images on emoji category tabs
        for (Tab tab : tabPane.getTabs()) {
            // scroll pane and flow pane of each category tab
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.setPadding(new Insets(5));
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            // flow pane is narrower than scroll pane (scroll pane has scrollbar)
            pane.prefWidthProperty().bind(scrollPane.widthProperty().subtract(5));
            pane.setHgap(5);
            pane.setVgap(5);

            tab.setId(tab.getText());
            ImageView icon = new ImageView();
            icon.setFitWidth(20);
            icon.setFitHeight(20);

            // put emoji image on icon (image view)
            switch (tab.getText().toLowerCase()) {
                case "frequently used" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":heart:").getHex())));
                case "people" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":smiley:").getHex())));
                case "nature" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":dog:").getHex())));
                case "food" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":apple:").getHex())));
                case "activity" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":soccer:").getHex())));
                case "travel" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":airplane:").getHex())));
                case "objects" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":bulb:").getHex())));
                case "symbols" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":atom:").getHex())));
                case "flags" ->
                        icon.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(EmojiParser.getInstance().getEmoji(":flag_eg:").getHex())));
            }

            // put emoji icon on tab
            if(icon.getImage() != null) {
                tab.setText("");
                tab.setGraphic(icon);
            }

            // tooltip appears when mouse hovers on tab
            tab.setTooltip(new Tooltip(tab.getId()));
            tab.selectedProperty().addListener(ee-> {
                if (tab.getGraphic() == null) {
                    return;
                }
                // show emoji and category name when tab is clicked
                if(tab.isSelected()) {
                    tab.setText(tab.getId());
                } else {
                    tab.setText("");
                }
            });
        }

        // default selected emoji skin tone and emoji tab
        boxTone.getSelectionModel().select(0);
        tabPane.getSelectionModel().select(1);
    }
    private void refreshTabs() {
        // creates emojis with skin tones dependent on selected box tone
        Map<String, List<Emoji>> map = EmojiParser.getInstance().getCategorizedEmojis(boxTone.getSelectionModel().getSelectedIndex()+1);
        for (Tab tab : tabPane.getTabs()) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();
            pane.getChildren().clear();
            String category = tab.getId().toLowerCase();
            if(map.get(category) == null) {
                continue;
            }
            // loads emojis of each category (tab) depending on selected box tone
            map.get(category).forEach(emoji -> {
                pane.getChildren().add(createEmojiNode(emoji));
            });
        }
    }

    private Node createEmojiNode(Emoji emoji) {
        // try to load emoji in image view and put it on stack pane
        StackPane stackPane = new StackPane();
        stackPane.setMaxSize(32, 32);
        stackPane.setPrefSize(32, 32);
        stackPane.setMinSize(32, 32);
        stackPane.setPadding(new Insets(3));
        stackPane.setId(emoji.getShortname());
        ImageView imageView = new ImageView();
        imageView.setId("0");
        imageView.setFitWidth(32);
        imageView.setFitHeight(32);
        try {
            imageView.setImage(EmojiImageCache.getInstance().getImage(getEmojiImagePath(emoji.getHex())));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        stackPane.getChildren().add(imageView);

        // emoji text code appears as tooltip when mouse lingers over emoji
        Tooltip tooltip = new Tooltip(emoji.getShortname());
        Tooltip.install(stackPane, tooltip);
        // different cursor for emoji list
        stackPane.setCursor(Cursor.HAND);
        ScaleTransition st = new ScaleTransition(Duration.millis(90), imageView);

        // scale transition when mouse hovers over emoji, emoji gets bigger + drop shadow effect
        stackPane.setOnMouseEntered(e-> {
            imageView.setEffect(new DropShadow());
            st.setToX(1.2);
            st.setToY(1.2);
            st.playFromStart();
            // show emoji text code text field
            if(txtSearch.getText().isEmpty())
                txtSearch.setPromptText(emoji.getShortname());
        });

        // reverse scale transition + effect when mouse exits
        stackPane.setOnMouseExited(e-> {
            imageView.setEffect(null);
            st.setToX(1.);
            st.setToY(1.);
            st.playFromStart();
        });

        // emoji is clicked
        stackPane.setOnMouseClicked(click -> {

            // add emoji text code to message text field
            if (!this.textField_lobbyChat_messageInput.isDisabled()) {
                this.textField_lobbyChat_messageInput.appendText(emoji.getShortname());
            }

            Tab freqTab = tabPane.getTabs().get(0);
            ScrollPane scrollPane = (ScrollPane) freqTab.getContent();
            FlowPane pane = (FlowPane) scrollPane.getContent();

            // frequently used emoji category
            if (stackPane.getParent() == pane) {
                ImageView view = (ImageView) stackPane.getChildren().get(0);
                // get current click count of emoji
                int counterAfterClick = Integer.parseInt(view.getId()) + 1;
                view.setId(String.valueOf(counterAfterClick));

            } else { // normal emoji people category

                // check if emoji is already on freq list
                boolean onFreqList = false;
                for (Node stackP : pane.getChildren()) {
                    if (stackP.getId().equals(emoji.getShortname())) {
                        onFreqList = true;
                    }
                }
                // node is not yet on freq list, add to it
                if (!onFreqList) {
                    pane.getChildren().add(createEmojiNode(emoji));
                }
            }
        });
        return stackPane;
    }

    private String getEmojiImagePath(String hexStr) throws NullPointerException {
        // loads emoji images from file
        return Objects.requireNonNull(this.getClass().getResource("../views/images/emoji_images/emojitwo/" + hexStr + ".png")).toExternalForm();
    }

    public void setTextField(TextField textField_lobbyChat_messageInput) {
        this.textField_lobbyChat_messageInput = textField_lobbyChat_messageInput;
    }

    class ToneCell extends ListCell<Image> {
        private final ImageView imageView;

        public ToneCell() {
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            imageView = new ImageView();
            imageView.setFitWidth(20);
            imageView.setFitHeight(20);
        }

        @Override
        protected void updateItem(Image item, boolean empty) {
            super.updateItem(item, empty);

            if (item == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                imageView.setImage(item);
                setGraphic(imageView);
            }
        }
    }

    public void setTestEmojiMenu(boolean testEmojiMenu) {
        this.testEmojiMenu = testEmojiMenu;
    }

}
