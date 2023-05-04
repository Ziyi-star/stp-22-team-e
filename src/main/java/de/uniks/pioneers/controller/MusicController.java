package de.uniks.pioneers.controller;

import de.uniks.pioneers.Main;
import de.uniks.pioneers.service.MusicService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;
import java.util.ResourceBundle;

public class MusicController implements Controller {


    private final MusicService musicService;
    private final ResourceBundle resourceBundle;
    @FXML
    public Pane root;
    @FXML
    public ImageView imageview_music;
    @FXML
    public ImageView imageview_sound;
    @FXML
    public Slider slider_music;
    @FXML
    public Slider slider_sound;
    Parent parentFxml;

    public MusicController(MusicService musicService, ResourceBundle resourceBundle) {
        this.musicService = musicService;
        this.resourceBundle = resourceBundle;
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
            final FXMLLoader loader = new FXMLLoader(Main.class.getResource("views/musicMenu.fxml"), resourceBundle);
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

        slider_music.valueProperty().addListener((observable, oldValue, newValue) -> musicService.changeMusicVolume((double) newValue));
        slider_sound.valueProperty().addListener((observable, oldValue, newValue) -> musicService.changeSoundVolume((double) newValue));
        hideMenu();
        return parent;
    }

    public void showMenu() {
        slider_music.setValue(musicService.getCurrentMusicVol());
        slider_sound.setValue(musicService.getCurrentSoundVol());
        setMusicImage();
        setSoundImage();
        root.setVisible(true);
        root.setMouseTransparent(false);
    }

    public void hideMenu() {
        root.setVisible(false);
        root.setMouseTransparent(true);
    }

    public void toggleMenu() {
        if (root.isVisible()) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    public void toggleMusicMute(MouseEvent mouseEvent) {
        boolean mute = musicService.getMusicMuted();
        musicService.muteMusic(!mute);
        setMusicImage();
    }

    private void setMusicImage() {
        if (musicService.getMusicMuted()) {
            imageview_music.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/sound_settings/music_muted.png"))));
        } else {
            imageview_music.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/sound_settings/music_unmuted.png"))));
        }
    }

    public void toggleSoundMute(MouseEvent mouseEvent) {
        boolean mute = musicService.getSoundMuted();
        musicService.muteSound(!mute);
        setSoundImage();
    }

    private void setSoundImage() {
        if (musicService.getSoundMuted()) {
            imageview_sound.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/sound_settings/sound_muted.png"))));
        } else {
            imageview_sound.setImage(new Image(Objects.requireNonNull(Main.class.getResourceAsStream("views/images/sound_settings/sound_unmuted.png"))));
        }
    }

    public void setParent(Parent parentFxml) {
        this.parentFxml = parentFxml;
    }
}
