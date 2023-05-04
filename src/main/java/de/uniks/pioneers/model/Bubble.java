package de.uniks.pioneers.model;

import com.pavlobu.emojitextflow.EmojiTextFlow;
import com.pavlobu.emojitextflow.EmojiTextFlowParameters;
import javafx.geometry.NodeOrientation;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.jetbrains.annotations.NotNull;

/**
 * @author zeppel
 * @version 1
 * <p>
 * Library for creating nice chatbubbles in JavaFX Copyright zeppel 2019
 * <p>
 * Requirements: JavaFX
 * @web https://zeppel.eu
 * @github github.com/zeppelsoftware
 */
public class Bubble extends Group {

    private final Font textFont = Font.font("Arial", 14);
    private final Paint textColor = Color.BLACK;
    private final Font metaFont = Font.font("Arial", 8);
    private final Paint metaColor = Color.BLACK;
    private final Paint bubbleColor = Color.rgb(128, 191, 255);
    private final Paint partnerColor = Color.rgb(255, 242, 179);

    private final Paint spectatorColor = Color.rgb(160, 160, 160);

    private Rectangle r;


    // Set bubble color depending on partner boolean
    public Bubble(boolean partner, String text, String meta, boolean isLobby, boolean isSpectator) {
        super();
        init(text, meta, partner, isLobby, isSpectator);
    }

    private void init(String text, String meta, boolean partner, boolean isLobby, boolean isSpectator) {
        int x = 0;
        int y = 0;
        // temp for text
        Text temp = new Text(text);
        temp.setFont(textFont);
        int textW = (int) temp.getLayoutBounds().getWidth();
        int textH = (int) temp.getLayoutBounds().getHeight();
        int p = 14;
        int s = 2;
        int w = textW + p * 2 + s * 2;
        int h = textH + p * 2;

        // tmp for meta
        Text tmp = new Text(meta);
        tmp.setFont(metaFont);
        int metaW = (int) tmp.getLayoutBounds().getWidth();
        int metaH = (int) tmp.getLayoutBounds().getHeight();
        h += metaH;

        // label meta
        Label m = new Label(meta);
        m.setFont(metaFont);
        m.setTextFill(metaColor);
        int pm = 10;
        int sm = 2;
        m.setTranslateX(x + (w - (metaW + pm + sm)));
        m.setTranslateY(y + textH + pm * 2);

        // bubble
        r = new Rectangle();
        r.setTranslateX(x);
        r.setTranslateY(y);

        r.setWidth(w);
        r.setHeight(h);

        int edgeRadius = 30;
        r.setArcHeight(edgeRadius);
        r.setArcWidth(edgeRadius);


        if (partner && !isSpectator) {
            // partner is true, set bubble color to beige
            r.setFill(partnerColor);
        } else if (isSpectator) {
            // spectator is true, set bubble color to grey
            r.setFill(spectatorColor);
        } else {
            // partner is false, set bubble color to blue
            r.setFill(bubbleColor);
        }

        if (partner && isLobby) {
            // label name
            String[] name = text.split("\\n", 2);
            Label t = new Label(name[0]);
            setTextLayout(t, Color.BLUE, x, p, s, y);
            t.getStyleClass().addAll("name");

            Text msg = new Text("\n" + name[1]);
            EmojiTextFlow flow = getEmojiTextFlow(x, y, p, s, msg);

            getChildren().addAll(r, t, flow, m);

        } else {

            Text msg = new Text(text);
            msg.setFont(Font.font(8));

            EmojiTextFlow flow = getEmojiTextFlow(x, y, p, s, msg);
            getChildren().addAll(r, flow, m);
        }
    }

    @NotNull
    private EmojiTextFlow getEmojiTextFlow(int x, int y, int p, int s, Text msg) {
        // parameters for flow, important were alignment and text size
        EmojiTextFlowParameters parameters = new EmojiTextFlowParameters();
        parameters.setEmojiScaleFactor(1.0);
        parameters.setTextAlignment(TextAlignment.LEFT);
        parameters.setFont(Font.font("System", FontWeight.NORMAL, 14.0));
        parameters.setTextColor(Color.BLACK);

        // EmojiTextFlow extends TextFlow which extends Pane -> layout for emojis
        EmojiTextFlow flow = new EmojiTextFlow(parameters);
        flow.parseAndAppend(msg.getText());
        flow.setTranslateX(x + p + s);
        flow.setTranslateY(y + p);
        return flow;
    }

    private void setTextLayout(Label l, Paint textColor, int x, int p, int s, int y) {
        l.setFont(textFont);
        l.setTextFill(textColor);
        l.setTranslateX(x + p + s);
        l.setTranslateY(y + p);
    }

    /**
     * set bubble width
     *
     * @param width
     */
    public void setWidth(double width) {
        r.setWidth(width);
    }

    /**
     * set bubble height
     *
     * @param height
     */
    public void setHeight(double height) {
        r.setWidth(height);
    }

}