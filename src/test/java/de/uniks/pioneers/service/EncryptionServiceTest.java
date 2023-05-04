package de.uniks.pioneers.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EncryptionServiceTest {

    @InjectMocks
    EncryptionService encryptionService;

    @Test
    void encryptMe() {
        final String result = encryptionService.encryptMe("test");
        assertEquals("udts", result);
    }

    @Test
    void decryptMe() {
        final String result = encryptionService.decryptMe("udts");
        assertEquals("test", result);
    }
}