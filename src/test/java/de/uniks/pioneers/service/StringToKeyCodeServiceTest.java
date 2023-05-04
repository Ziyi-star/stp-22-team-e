package de.uniks.pioneers.service;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class StringToKeyCodeServiceTest {

    @InjectMocks
    StringToKeyCodeService stringToKeyCodeService;

    @Test
    void stringToKeyCode() {
        // test stringToKeyCode method
        final Object object0 = stringToKeyCodeService.stringToKeyCode("A");
        assertEquals(KeyCode.A, object0);

        final Object object1 = stringToKeyCodeService.stringToKeyCode("Alt + A");
        final KeyCombination keyCombination0 = new KeyCodeCombination((KeyCode) object0, KeyCombination.ALT_DOWN);
        assertEquals(object1, keyCombination0);

        final Object object2 = stringToKeyCodeService.stringToKeyCode("Strg + A");
        final KeyCombination keyCombination1 = new KeyCodeCombination((KeyCode) object0, KeyCombination.CONTROL_DOWN);
        assertEquals(object2, keyCombination1);

    }
}