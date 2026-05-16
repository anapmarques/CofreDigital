package br.com.cofredigital.service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import br.com.cofredigital.crypto.KeyDerivationUtil;
import br.com.cofredigital.database.Database;
import br.com.cofredigital.database.KeyStoreDAO;
import br.com.cofredigital.database.LogDAO;
import br.com.cofredigital.model.User;

public class VaultService {

    public static class VaultFileEntry {
        public final String codigo;
        public final String nome;
        public final String dono;
        public final String grupo;

        public VaultFileEntry(String codigo, String nome, String dono, String grupo) {
            this.codigo = codigo;
            this.nome = nome;
            this.dono = dono;
            this.grupo = grupo;
        }
    }

    public static class VaultResult {
        public final List<VaultFileEntry> files;
        public final String error;

        public VaultResult(List<VaultFileEntry> files) {
            this.files = files;
            this.error = null;
        }

        public VaultResult(String error) {
            this.files = null;
            this.error = error;
        }

        public boolean isSuccess() { return error == null; }
    }

    public VaultResult listVaultFiles(User user, String folderPath, String userPhrase, String adminPassphrase) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        File folder = new File(folderPath);
        File idxEnc = new File(folder, "index.enc");
        File idxEnv = new File(folder, "index.env");
        File idxAsd = new File(folder, "index.asd");

        if (!folder.exists() || !folder.isDirectory() || !idxEnc.exists() || !idxEnv.exists() || !idxAsd.exists()) {
            return new VaultResult("Caminho de pasta invalido ou arquivos de indice nao encontrados.");
        }

        if (adminPassphrase == null) {
            return new VaultResult("Frase secreta do administrador nao disponivel.");
        }

        try {
            byte[] userEncryptedPrivKey = new KeyStoreDAO().getUserPrivateKey(user.getEmail());
            if (userEncryptedPrivKey == null) {
                return new VaultResult("Erro ao obter chave privada do usuario.");
            }

            PrivateKey currentUserPrivateKey;
            try {
                currentUserPrivateKey = decryptPrivateKey(userEncryptedPrivKey, userPhrase);
            } catch (Exception ex) {
                return new VaultResult("Frase secreta invalida ou chave privada nao pode ser decifrada.");
            }

            String currentUserCertPem = new br.com.cofredigital.database.UserDAO().getUserCertificado(user.getEmail());
            if (currentUserCertPem == null) {
                return new VaultResult("Erro ao obter certificado do usuario.");
            }

            byte[] adminEncryptedPrivKey = new KeyStoreDAO().getAdminPrivateKey();
            if (adminEncryptedPrivKey == null) {
                return new VaultResult("Erro ao obter chave privada do administrador.");
            }

            PrivateKey adminPrivateKey;
            try {
                adminPrivateKey = decryptPrivateKey(adminEncryptedPrivKey, adminPassphrase);
            } catch (Exception ex) {
                return new VaultResult("Erro ao decriptar chave privada do administrador.");
            }

            byte[] envBytes = Files.readAllBytes(idxEnv.toPath());
            byte[] seed;
            try {
                Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                rsaCipher.init(Cipher.DECRYPT_MODE, adminPrivateKey);
                seed = rsaCipher.doFinal(envBytes);
            } catch (Exception ex) {
                new LogDAO().addLog(7007, user.getEmail(), timestamp);
                return new VaultResult("Falha na decriptacao do arquivo de indice.");
            }

            SecureRandom indexSr = SecureRandom.getInstance("SHA1PRNG");
            indexSr.setSeed(seed);
            KeyGenerator indexKg = KeyGenerator.getInstance("AES");
            indexKg.init(256, indexSr);
            SecretKey indexAesKey = indexKg.generateKey();

            byte[] encData = Files.readAllBytes(idxEnc.toPath());
            byte[] indexData;
            try {
                Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                aesCipher.init(Cipher.DECRYPT_MODE, indexAesKey);
                indexData = aesCipher.doFinal(encData);
            } catch (Exception ex) {
                new LogDAO().addLog(7007, user.getEmail(), timestamp);
                return new VaultResult("Falha na decriptacao do arquivo de indice.");
            }

            new LogDAO().addLog(7005, user.getEmail(), timestamp);

            byte[] asdBytes = Files.readAllBytes(idxAsd.toPath());
            String adminCertPem = new Database().getAdminCertificado();
            if (adminCertPem == null) {
                return new VaultResult("Erro ao obter certificado do administrador.");
            }

            boolean sigValid;
            try {
                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                java.io.ByteArrayInputStream certIn = new java.io.ByteArrayInputStream(adminCertPem.getBytes(StandardCharsets.UTF_8));
                java.security.cert.Certificate cert = cf.generateCertificate(certIn);
                java.security.PublicKey pub = cert.getPublicKey();
                java.security.Signature sig = java.security.Signature.getInstance("SHA1withRSA");
                sig.initVerify(pub);
                sig.update(indexData);
                sigValid = sig.verify(asdBytes);
            } catch (Exception ex) {
                sigValid = false;
            }

            if (!sigValid) {
                new LogDAO().addLog(7008, user.getEmail(), timestamp);
                return new VaultResult("Falha na verificacao (integridade e autenticidade) do arquivo de indice.");
            }

            new LogDAO().addLog(7006, user.getEmail(), timestamp);

            String indexContent = new String(indexData, StandardCharsets.UTF_8);
            String[] lines = indexContent.split("\n");
            String userName = user.getUserName();
            String userGroup = user.getGroup();

            List<VaultFileEntry> entries = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ");
                if (parts.length >= 4) {
                    String dono = parts[2];
                    String grupo = parts[3];
                    if (dono.equals(userName) || grupo.equals(userGroup)) {
                        entries.add(new VaultFileEntry(parts[0], parts[1], dono, grupo));
                    }
                }
            }

            new LogDAO().addLog(7009, user.getEmail(), timestamp);
            new Database().updateTotalConsultas(user);

            return new VaultResult(entries);
        } catch (Exception e) {
            return new VaultResult("Erro: " + e.getMessage());
        }
    }

    public String decryptFile(User user, String folderPath, String nomeSecreto,
                               String userPhrase, String adminPassphrase) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

        try {
            byte[] userEncryptedPrivKey = new KeyStoreDAO().getUserPrivateKey(user.getEmail());
            if (userEncryptedPrivKey == null) return "Erro ao obter chave privada do usuario.";

            PrivateKey currentUserPrivateKey;
            try {
                currentUserPrivateKey = decryptPrivateKey(userEncryptedPrivKey, userPhrase);
            } catch (Exception ex) {
                return "Frase secreta invalida ou chave privada nao pode ser decifrada.";
            }

            String currentUserCertPem = new br.com.cofredigital.database.UserDAO().getUserCertificado(user.getEmail());
            if (currentUserCertPem == null) return "Erro ao obter certificado do usuario.";

            File fileEnc = new File(folderPath, nomeSecreto + ".enc");
            File fileEnv = new File(folderPath, nomeSecreto + ".env");
            File fileAsd = new File(folderPath, nomeSecreto + ".asd");

            if (!fileEnc.exists() || !fileEnv.exists() || !fileAsd.exists()) {
                return "Arquivos do vault nao encontrados para: " + nomeSecreto;
            }

            byte[] fileEnvBytes = Files.readAllBytes(fileEnv.toPath());
            byte[] fileSeed;
            try {
                Cipher fileRsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                fileRsaCipher.init(Cipher.DECRYPT_MODE, currentUserPrivateKey);
                fileSeed = fileRsaCipher.doFinal(fileEnvBytes);
            } catch (Exception ex) {
                new LogDAO().addLog(7015, user.getEmail(), timestamp, nomeSecreto);
                return "Falha na decriptacao do arquivo " + nomeSecreto + ".";
            }

            SecureRandom fileSr = SecureRandom.getInstance("SHA1PRNG");
            fileSr.setSeed(fileSeed);
            KeyGenerator fileKg = KeyGenerator.getInstance("AES");
            fileKg.init(256, fileSr);
            SecretKey fileAesKey = fileKg.generateKey();

            byte[] fileEncData = Files.readAllBytes(fileEnc.toPath());
            byte[] filePlainData;
            try {
                Cipher fileAesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                fileAesCipher.init(Cipher.DECRYPT_MODE, fileAesKey);
                filePlainData = fileAesCipher.doFinal(fileEncData);
            } catch (Exception ex) {
                new LogDAO().addLog(7015, user.getEmail(), timestamp, nomeSecreto);
                return "Falha na decriptacao do arquivo " + nomeSecreto + ".";
            }

            new LogDAO().addLog(7013, user.getEmail(), timestamp, nomeSecreto);

            byte[] fileAsdBytes = Files.readAllBytes(fileAsd.toPath());
            boolean fileSigValid;
            try {
                java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
                java.io.ByteArrayInputStream certIn = new java.io.ByteArrayInputStream(currentUserCertPem.getBytes(StandardCharsets.UTF_8));
                java.security.cert.Certificate cert = cf.generateCertificate(certIn);
                java.security.PublicKey pub = cert.getPublicKey();
                java.security.Signature sig = java.security.Signature.getInstance("SHA1withRSA");
                sig.initVerify(pub);
                sig.update(filePlainData);
                fileSigValid = sig.verify(fileAsdBytes);
            } catch (Exception ex) {
                fileSigValid = false;
            }

            if (!fileSigValid) {
                new LogDAO().addLog(7016, user.getEmail(), timestamp, nomeSecreto);
                return "Falha na verificacao (integridade e autenticidade) do arquivo " + nomeSecreto + ".";
            }

            new LogDAO().addLog(7014, user.getEmail(), timestamp, nomeSecreto);

            java.nio.file.Path outputPath = java.nio.file.Paths.get(nomeSecreto);
            Files.write(outputPath, filePlainData);
            return null; // null = success
        } catch (Exception e) {
            return "Erro: " + e.getMessage();
        }
    }

    private static PrivateKey decryptPrivateKey(byte[] encryptedKeyBytes, String passphrase) throws Exception {
        KeyFactory kf = KeyFactory.getInstance("RSA");
        try {
            javax.crypto.SecretKey aesKey = KeyDerivationUtil.deriveKey(passphrase);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decrypted = cipher.doFinal(encryptedKeyBytes);
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decrypted));
        } catch (Exception e) {
            javax.crypto.SecretKey aesKey = KeyDerivationUtil.deriveKeySHA1PRNG(passphrase);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decrypted = cipher.doFinal(encryptedKeyBytes);
            return kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(decrypted));
        }
    }
}
