package de.uniks.pioneers.service;

import javax.inject.Inject;

public class EncryptionService {
    @Inject
    public EncryptionService() {
    }

    public String encryptMe(String text) {

        byte[] data = text.getBytes();
        byte[] encryptedData = new byte[data.length];

        // encrypt data
        for (int i = 0; i < data.length; i++) {
            encryptedData[i] = (byte) ((i % 2 == 0) ? data[i] + 1 : data[i] - 1);
        }

        return new String(encryptedData);
    }

    public String decryptMe(String text) {

        byte[] data = text.getBytes();
        byte[] decryptedData = new byte[data.length];

        // decrypt data
        for (int i = 0; i < data.length; i++) {
            decryptedData[i] = (byte) ((i % 2 == 0) ? data[i] - 1 : data[i] + 1);
        }

        return new String(decryptedData);
    }
}
