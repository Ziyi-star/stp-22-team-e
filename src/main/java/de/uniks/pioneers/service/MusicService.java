package de.uniks.pioneers.service;

import de.uniks.pioneers.Constants;
import de.uniks.pioneers.Main;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javafx.animation.KeyValue;
import java.util.Objects;
import java.util.Random;

@Singleton
public class MusicService {
    private final int maxVolume = 100;
    private Constants.MUSIC_CONTEXT currentContext;
    private final PrefService prefService;
    private MediaPlayer mediaPlayer;
    private double currentMusicVol;
    private double currentSoundVol;
    private boolean musicPlaying = false;
    private boolean soundMuted;
    private boolean musicMuted;

    private boolean fade;

    @Inject
    public MusicService(PrefService prefService) {
        this.prefService = prefService;
        //prefService returns 101 when no sound settings are saved yet -> default volume is 50
        currentMusicVol = prefService.getMusicVolume() == 101 ? 50 : prefService.getMusicVolume();
        currentSoundVol = prefService.getSoundVolume() == 101 ? 50 : prefService.getSoundVolume();
        musicMuted = prefService.getMusicMute();
        soundMuted = prefService.getSoundMute();
    }

    public void playMusic(Constants.MUSIC_CONTEXT context) {
        //String musicFile = "music/Daryl Hall & John Oates - Rich Girl.mp3";     // For example
        if(musicPlaying){
            mediaPlayer.stop();
        }

        String musicFile;
        Random random = new Random();

        musicFile = switch (context) {
            case LOBBY -> Constants.MUSIC.LOBBY.toString();
            case INGAME -> Constants.inGameMusicPool[random.nextInt(4)];
            case EDITOR -> Constants.editorMusicPool[random.nextInt(3)];
        };

        currentContext = context;

        Media sound = new Media(Objects.requireNonNull(Main.class.getResource(musicFile)).toExternalForm());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();
        mediaPlayer.setMute(musicMuted);
        musicPlaying = true;
        changeMusicVolume(currentMusicVol);
        //loop audio
        fade = true;
        mediaPlayer.currentTimeProperty().addListener(ov -> {
            if (mediaPlayer.getTotalDuration().toSeconds() - mediaPlayer.getCurrentTime().toSeconds() <= new Duration(8000).toSeconds()) {
                if (fade) {
                    fade = false;
                    Timeline timeline = new Timeline(
                            new KeyFrame(Duration.seconds(8),
                                    new KeyValue(mediaPlayer.volumeProperty(), 0)));
                    timeline.play();
                }
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            switch(context){
                case LOBBY -> {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                }
                case INGAME, EDITOR -> playMusic(context);
            }
        });
    }

    public void stopMusic() {
        mediaPlayer.stop();
        musicPlaying = false;
    }

    public Constants.MUSIC_CONTEXT getMusicContext(){
        return currentContext;
    }

    public void playDiceSound() {
        playSoundEffect("music/dice_roll_v2.mp3");
    }

    public void playMessageSound() {
        playSoundEffect("music/message_sound_v3.mp3");
    }

    public void playBuildingSound() {
        playSoundEffect("music/building_placing.mp3");
    }

    public void playRoundStartSound() {
        playSoundEffect("music/round_start.mp3");
    }


    public void playSoundEffect(String musicFile) {
        //String musicFile = "music/windows-error.mp3";     // For example

        Media sound;
        sound = new Media(Objects.requireNonNull(Main.class.getResource(musicFile)).toExternalForm());
        MediaPlayer soundPlayer = new MediaPlayer(sound);
        soundPlayer.setVolume(currentSoundVol / maxVolume);
        soundPlayer.setMute(soundMuted);
        soundPlayer.play();
    }

    public void changeMusicVolume(double currVolume) {
        currentMusicVol = currVolume;
        double vol = currVolume / maxVolume;
        mediaPlayer.setVolume(vol);
        prefService.setMusicVolume(currentMusicVol);
    }

    public void changeSoundVolume(double currVolume) {
        currentSoundVol = currVolume;
        prefService.setSoundVolume(currentSoundVol);
    }

    public void muteMusic(boolean muted) {
        mediaPlayer.setMute(muted);
        musicMuted = muted;
        prefService.setMusicMute(getMusicMuted());
    }

    public boolean getMusicMuted() {
        return mediaPlayer.isMute();
    }

    public void muteSound(boolean muted) {
        soundMuted = muted;
        prefService.setSoundMute(soundMuted);
    }

    public boolean getSoundMuted() {
        return soundMuted;
    }

    public double getCurrentMusicVol() {
        return currentMusicVol;
    }

    public double getCurrentSoundVol() {
        return currentSoundVol;
    }

    public boolean getMusicPlaying() {
        return musicPlaying;
    }
}
