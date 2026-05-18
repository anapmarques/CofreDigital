// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;

public class KeyStoreService {

    private static final String KEYSTORE_FILE = "keystore.jks";
    private char[] password;
    private KeyStore keyStore;

    public KeyStoreService(String password) {
        this.password = password.toCharArray();
        loadOrCreateKeyStore();
    }

    private void loadOrCreateKeyStore() {
        try {
            keyStore = KeyStore.getInstance("JKS");
            File file = new File(KEYSTORE_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    keyStore.load(fis, password);
                }
            } else {
                // Keystore does not exist, create a new one
                keyStore.load(null, password);
                storeKeyStore();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load or create keystore", e);
        }
    }

    private void storeKeyStore() {
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE_FILE)) {
            keyStore.store(fos, password);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store keystore", e);
        }
    }

    public void setCertificate(String alias, Certificate cert) throws Exception {
        keyStore.setCertificateEntry(alias, cert);
        storeKeyStore();
    }

    public Certificate getCertificate(String alias) throws Exception {
        return keyStore.getCertificate(alias);
    }

    public void setKey(String alias, Key key, Certificate[] chain) throws Exception {
        keyStore.setKeyEntry(alias, key, password, chain);
        storeKeyStore();
    }

    public Key getKey(String alias) throws Exception {
        return keyStore.getKey(alias, password);
    }

    public boolean containsAlias(String alias) throws Exception {
        return keyStore.containsAlias(alias);
    }
}
