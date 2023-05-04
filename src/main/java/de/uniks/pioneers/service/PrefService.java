package de.uniks.pioneers.service;

import de.uniks.pioneers.model.Game;

import javax.inject.Inject;
import java.util.Locale;
import java.util.prefs.Preferences;

public class PrefService {
    private final Preferences preferences;

    @Inject
    public PrefService(Preferences preferences) {

        this.preferences = preferences;
    }

    // Get your current language
    public Locale getLocale() {
        return Locale.forLanguageTag(preferences.get("language", Locale.getDefault().toLanguageTag()));
    }

    // Set your language
    public void setLocale(Locale locale) {
        preferences.put("language", locale.toLanguageTag());
    }

    public String getUser() {
        return preferences.get("user", "");
    }

    public void setUser(String data) {
        preferences.put("user", data);
    }

    public String getPassword() {
        return preferences.get("password", "");
    }

    public void setPassword(String data) {
        preferences.put("password", data);
    }

    public Boolean getFlag() {
        return preferences.getBoolean("rememberMe", Boolean.parseBoolean(""));
    }

    public void setFlag(Boolean bool) {
        preferences.putBoolean("rememberMe", bool);
    }

    public double getMusicVolume() {
        return preferences.getDouble("musicVolume", 101);
    }

    public void setMusicVolume(double f) {
        preferences.putDouble("musicVolume", f);
    }

    public double getSoundVolume() {
        return preferences.getDouble("soundVolume", 101);
    }

    public void setSoundVolume(double f) {
        preferences.putDouble("soundVolume", f);
    }

    public boolean getMusicMute() {
        return preferences.getBoolean("musicMute", false);
    }

    public void setMusicMute(boolean bool) {
        preferences.putBoolean("musicMute", bool);
    }

    public boolean getSoundMute() {
        return preferences.getBoolean("soundMute", false);
    }

    public void setSoundMute(boolean bool) {
        preferences.putBoolean("soundMute", bool);
    }

    public String getFull() {
        return preferences.get("full", "F11");
    }

    public void setFull(String data) {
        preferences.put("full", data);
    }

    public String getMapCenter() {
        return preferences.get("map", "Alt + Z");
    }

    public void setMapCenter(String data) {
        preferences.put("map", data);
    }

    public void setCurrentGame(String data) {
        preferences.put("game", data);
    }

    public String getCurrentGame() {
       return preferences.get("game", "1");
    }
}
