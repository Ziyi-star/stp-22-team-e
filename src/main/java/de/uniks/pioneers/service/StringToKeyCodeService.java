package de.uniks.pioneers.service;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import javax.inject.Inject;
import java.util.Objects;

public class StringToKeyCodeService {

    @Inject
    public StringToKeyCodeService() {

    }

    public Object stringToKeyCode(String s) {
        // helps translate Strings to KeyCodes
        if (s.contains(" + ")) {
            String[] data = s.split(" \\+ ");
            KeyCode keyCode = KeyCode.getKeyCode(data[1]);
            if (Objects.equals(data[0], "Alt")) {
                return new KeyCodeCombination(keyCode, KeyCombination.ALT_DOWN);
            } else {
                return new KeyCodeCombination(keyCode, KeyCombination.CONTROL_DOWN);
            }
        } else {
            return KeyCode.getKeyCode(s);
        }
    }
}
