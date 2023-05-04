package de.uniks.pioneers.controller;

import com.pavlobu.emojitextflow.Emoji;
import com.vdurmont.emoji.EmojiParser;
import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import de.uniks.pioneers.model.*;
import de.uniks.pioneers.service.ChatService;
import de.uniks.pioneers.service.MusicService;
import de.uniks.pioneers.ws.EventListener;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


import java.util.*;
import java.util.Map;

public class MessageSubController implements Controller {

    private final ChatService chatService;

    private final EventListener eventListener;
    private final MusicService musicService;

    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private final Label msgCounterLabel = new Label();
    private final boolean isInGameChat;
    private ObservableList<Member> spectators = FXCollections.observableArrayList();
    private Tab tab;
    private Disposable disposable;
    private int msgCounter = 0;
    private String gameId;
    private Map<String, User> userIdToName;
    private Group group;
    private String pathId;
    private String pattern;
    private ScrollPane scrollPane;
    private VBox messageBox;


    public MessageSubController(ChatService chatService, EventListener eventListener, MusicService musicService, boolean isInGameChat) {

        this.chatService = chatService;
        this.eventListener = eventListener;
        this.musicService = musicService;
        this.isInGameChat = isInGameChat;
    }


    @Override
    public void init() {

        // lobby chat
        if (group == null) {
            pathId = gameId;
            pattern = Constants.GAMES;

        } else { // private chat
            pathId = group._id();
            pattern = Constants.GROUPS;
            chatService.getAllMessages(pathId, pattern).observeOn(Constants.FX_SCHEDULER).subscribe(this.messages::setAll);
        }

        // websocket
        disposable = this.eventListener.listen(pattern + "." + pathId + "." + Constants.MESSAGES + ".*.*", Message.class)
                .observeOn(Constants.FX_SCHEDULER).subscribe(resultEvent -> {

                    final Message message = resultEvent.data();
                    if (resultEvent.event().endsWith("created")) {
                        if (messages.stream().noneMatch(msg -> message._id().equals(msg._id()))) {
                            // not the current chat tab is selected
                            if (!this.tab.isSelected()) {
                                musicService.playMessageSound();

                                // tab already has label
                                if (this.tab.getGraphic() != null) {
                                    Label graphic = (Label) this.tab.getGraphic();
                                    if (graphic.getText().equals("1") && this.msgCounter == 0) {
                                        this.msgCounter = 2;
                                    } else {
                                        this.msgCounter += 1;
                                    }
                                } else {
                                    // tab does not have a label, but must increment counter
                                    this.msgCounter += 1;
                                }
                                this.msgCounterLabel.setStyle("-fx-background-color: red");
                                // show max of "+9" as unread messages
                                if (this.msgCounter > 9) {
                                    this.msgCounterLabel.setText("+9");
                                } else {
                                    this.msgCounterLabel.setText(String.valueOf(this.msgCounter));
                                }
                                // set new unread message label
                                this.tab.setGraphic(this.msgCounterLabel);
                            }
                            messages.add(message);
                        }
                    } else if (resultEvent.event().endsWith("deleted")) {

                        messages.removeIf(m -> m._id().equals(message._id()));
                    }
                });

    }

    @Override
    public void destroy() {
        this.disposable.dispose();
    }

    @Override
    public Parent render() {

        // scroll pane used to scroll through messages
        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // message box displays messages
        messageBox = new VBox();
        // spacing between single messages
        messageBox.setSpacing(4);
        messageBox.setPrefWidth(tab.getTabPane().getPrefWidth());
        scrollPane.setContent(messageBox);
        scrollPane.setFitToWidth(true);
        messageBox.setPadding(new Insets(4));
        scrollPane.getStyleClass().addAll("chatScroll");

        tab.setContent(scrollPane);
        this.messages.addListener((ListChangeListener<? super Message>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(msg -> {
                        // get added message
                        Node node = showMessages(msg);
                        // add message to message box
                        messageBox.getChildren().add(node);
                        // fixes scrolling bug
                        scrollPane.layout();
                        // scroll to bottom of scroll pane
                        scrollPane.setVvalue(1.0f);
                    });
                }

                if (c.wasRemoved()) {
                    c.getRemoved().forEach(msg -> {

                        // remove message from message box, stay at the same position
                        messageBox.getChildren().remove(c.getFrom());
                    });
                }
            }
        });

        // remove unread message counter if tab is clicked on / selected
        this.tab.setOnSelectionChanged(t -> {
            if (this.tab.isSelected()) {
                this.tab.setGraphic(null);
                this.clearMsgCounter();
            }
        });

        return null;
    }

    private Node showMessages(Message message) {
        // set width of shown messages
        int messageWidth = isInGameChat ? 16 : 18;
        Label messageLabel = new Label();

        // create String with emojis instead of emoji text representation
        String messageEmoji = EmojiParser.parseToUnicode(message.body());

        StringBuilder textEmojiParse = new StringBuilder();
        com.pavlobu.emojitextflow.EmojiParser parser = com.pavlobu.emojitextflow.EmojiParser.getInstance();

        Queue<Object> queue = parser.toEmojiAndText(message.body());
        for (Object obj : queue) {
            if (obj instanceof String) {
                textEmojiParse.append(obj);
            }
            if (obj instanceof Emoji) {
                Emoji em = (Emoji) obj;
                textEmojiParse.append(em.getUnicode());
            }
        }

        // get all emojis from message
        List<String> emojiList = new ArrayList<>();
        //emojiList = EmojiParser.extractEmojis(messageEmoji);
        emojiList = EmojiParser.extractEmojis(textEmojiParse.toString());

        // replace all emojis from list with uncommon char
        String replaced = EmojiParser.replaceAllEmojis(textEmojiParse.toString(), Constants.REPLACEMENT_CHAR);

        // performance: only use builder when needed (messages longer than 30 chars)
        if (replaced.length() > messageWidth) {
            // create array list from words of message
            String[] splitRes = messageEmoji.split(" ");
            ArrayList<String> messageParts = new ArrayList<>(Arrays.asList(splitRes));

            // built formatted message
            StringBuilder completeMessage = new StringBuilder();
            StringBuilder lineBuilder = new StringBuilder();

            // check that there are still remaining message parts
            while (!messageParts.isEmpty()) {

                // (1) next word is longer than a whole line (30 chars)
                if (messageParts.get(0).length() > messageWidth) {

                    // longWordBuilder used to format long single words
                    StringBuilder longWordBuilder = new StringBuilder(messageParts.get(0));

                    // (1.1) long word from message still longer than whole line
                    while (longWordBuilder.length() > messageWidth) {

                        // get part of next word with length 30, add part to complete message, then newline
                        completeMessage.append(longWordBuilder.substring(0, messageWidth)).append("\n");
                        longWordBuilder.delete(0, messageWidth);

                        // last part of long word from message is shorter than / same size as whole line
                        if (longWordBuilder.length() <= messageWidth) {

                            // last part from longWordBuilder fits in line
                            if (lineBuilder.length() + longWordBuilder.length() < messageWidth) {

                                // no need to add " "
                                if (lineBuilder.length() == 0) {
                                    lineBuilder.append(longWordBuilder);
                                } else {
                                    lineBuilder.append(" ").append(longWordBuilder);
                                }
                                longWordBuilder.setLength(0);

                            } else {
                                // last part of longWordBuilder does not fit in line
                                // add LineBuilder to completeMessage, start new lineBuilder and longWordBuilder
                                if (lineBuilder.length() > 0) {
                                    // do not add empty line and newline
                                    completeMessage.append(lineBuilder).append("\n");
                                }
                                lineBuilder.setLength(0);
                                lineBuilder.append(longWordBuilder);
                                longWordBuilder.setLength(0);
                            }
                        }
                    }
                    // remove long word that is already added to completeMessage
                    messageParts.remove(0);

                } else { // (2) next word is shorter / equal to whole line

                    // lineBuilder is used to create new message line (less than 30 chars) from words of array list
                    while (lineBuilder.length() <= messageWidth) {
                        // array list still has message parts
                        if (!messageParts.isEmpty()) {
                            // lineBuilder + new message word would be too long for line
                            if ((lineBuilder.length() + messageParts.get(0).length() + 1) > messageWidth) {
                                // put lineBuilder in current line and start new lineBuilder (outer loop) in new line
                                completeMessage.append(lineBuilder).append("\n");
                                lineBuilder.setLength(0);
                                break;
                            } else {
                                if (lineBuilder.length() == 0) {
                                    lineBuilder.append(messageParts.get(0));
                                } else {
                                    // enough space in line to add " " and new message word
                                    lineBuilder.append(" ").append(messageParts.get(0));
                                }
                                // remove currently added word from array list
                                messageParts.remove(0);
                            }

                        } else {
                            // all message parts are added to complete message
                            completeMessage.append(lineBuilder);
                            // set line builder to size 0 if message parts is empty
                            lineBuilder.setLength(0);
                            break;
                        }
                    }
                }

            }
            if (lineBuilder.length() > 0) {
                completeMessage.append(lineBuilder);
            }

            String emojiBack = reverseCharToEmoji(emojiList, completeMessage);
            String finalMessage = EmojiParser.parseToUnicode(emojiBack);
            messageLabel.setText(finalMessage);
        } else {

            StringBuilder builder = new StringBuilder(replaced);
            String emojiBack = reverseCharToEmoji(emojiList, builder);
            String finalMessage = EmojiParser.parseToUnicode(emojiBack);
            messageLabel.setText(finalMessage);
        }

        //get date
        String messageTime = message.updatedAt().substring(11, 16);

        Bubble speechBubble;
        if (message.sender().equals(chatService.getLoginResultStorage().getLoginResult()._id())) {
            // owm messages, put trash image next to it and put to right side
            ImageView deleteMessageImage = new ImageView(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/delete_icon.png"))));
            deleteMessageImage.setPickOnBounds(true);
            deleteMessageImage.getStyleClass().add("trash");
            //set action for delete message
            deleteMessageImage.setOnMouseClicked(this::deleteMessage);
            deleteMessageImage.setUserData(message);
            // delete image is disabled from start
            deleteMessageImage.setDisable(true);
            deleteMessageImage.setVisible(false);

            // own speech bubble, partner false, set color to blue
            speechBubble = new Bubble(false, messageLabel.getText(), messageTime, false, false);
            Bubble bubble = speechBubble;

            // show or hide delete image when message bubble is clicked
            bubble.setOnMouseClicked(msg -> {

                // get HBox that contains speech bubble and image
                HBox clickedBox = (HBox) bubble.getParent();
                int index = messageBox.getChildren().indexOf(clickedBox);

                HBox switchedBox = new HBox();

                // delete image is hidden
                if (clickedBox.getChildren().get(0) instanceof ImageView deleteBtn) {
                    // enable and show image
                    deleteBtn.setDisable(false);
                    deleteBtn.setVisible(true);

                    // image and message bubble change position
                    switchedBox.getChildren().addAll(bubble, deleteBtn);

                } else {
                    // delete image is shown and enabled
                    ImageView deleteBtn = (ImageView) clickedBox.getChildren().get(1);

                    // disable and hide image
                    deleteBtn.setDisable(true);
                    deleteBtn.setVisible(false);

                    // image and message bubble change position
                    switchedBox.getChildren().addAll(deleteBtn, bubble);
                }

                switchedBox.setAlignment(Pos.CENTER_RIGHT);
                switchedBox.setPadding(new Insets(5));
                switchedBox.setSpacing(8);

                // swap HBoxes aka show or hide message delete image
                messageBox.getChildren().remove(clickedBox);
                messageBox.getChildren().add(index, switchedBox);
            });

            HBox hbox = new HBox(deleteMessageImage, speechBubble);
            hbox.setAlignment(Pos.CENTER_RIGHT);
            // spacing between message and image
            hbox.setSpacing(8);
            scrollPane.layout();

            return hbox;

        } else {
            // partner messages, put to left side
            Label messageContent = new Label();
            String partnerMessage = messageLabel.getText();

            if (userIdToName == null) {
                // private partner message, no name partner name
                messageContent.setText(partnerMessage);
                speechBubble = new Bubble(true, partnerMessage, messageTime, false, false);
            } else if (spectators == null) {
                // lobby partner message, put partner name in bubble
                User sender = userIdToName.get(message.sender());
                speechBubble = new Bubble(true, sender.name() + "\n" + partnerMessage, messageTime, true, false);

            } else {
                User sender = userIdToName.get(message.sender());
                speechBubble = new Bubble(true, sender.name() + "\n" + partnerMessage, messageTime, true, false);
                for (Member spectator : spectators) {
                    // inGame spectator message
                    if (spectator.userId().equals(message.sender())) {
                        speechBubble = new Bubble(true, sender.name() + "\n" + partnerMessage, messageTime, true, true);
                        break;
                    } else {
                        speechBubble = new Bubble(true, sender.name() + "\n" + partnerMessage, messageTime, true, false);
                    }
                }
            }

            HBox hbox = new HBox(speechBubble);
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.setPadding(new Insets(5));
            return hbox;
        }
    }

    private String reverseCharToEmoji(List<String> emojiList, StringBuilder completeMessage) {
        for (int i = 0; i < completeMessage.length(); i += 1) {
            String letter = "" + completeMessage.charAt(i);
            // replace next occurrence of replacement char with emoji from list
            if (letter.equals(Constants.REPLACEMENT_CHAR_SINGLE)) {
                completeMessage.replace(i, i + 2, emojiList.get(0));
                emojiList.remove(0);
            }
        } return completeMessage.toString();
    }


    private void deleteMessage(MouseEvent mouseEvent) {
        ImageView image = (ImageView) mouseEvent.getSource();
        Message message = (Message) image.getUserData();
        this.chatService.deleteMessage(pathId, message._id(), pattern).observeOn(Constants.FX_SCHEDULER).subscribe();
    }

    private void clearMsgCounter() {
        this.msgCounter = 0;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {

        this.gameId = gameId;
    }

    public void setUserHashMap(Map<String, User> userIdToName) {
        this.userIdToName = userIdToName;
    }

    public void setSpectators(ObservableList<Member> spectators) {
        this.spectators = spectators;
    }

}
