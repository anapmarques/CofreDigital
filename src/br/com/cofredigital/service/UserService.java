package br.com.cofredigital.service;

import br.com.cofredigital.database.UserDAO;
import br.com.cofredigital.model.User;
import br.com.cofredigital.util.BcryptUtil;
import br.com.cofredigital.util.Input;
import java.security.SecureRandom;
import br.com.cofredigital.database.Database;

public class UserService {
    private UserDAO dao = new UserDAO();
    private SecureRandom rnd = new SecureRandom();
    private static Database database = new Database();

    // Verifica se existe algum usuário cadastrado
    public boolean hasAnyUser() {
        try { return dao.hasAny(); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void createAdminInteractive() throws Exception {
        System.out.print("Admin email: ");
        String email = Input.readLine();
        System.out.print("Admin senha: ");
        String pw = Input.readLineMasked();
        // create TOTP secret (20 bytes)
        byte[] totp = new byte[20]; rnd.nextBytes(totp);
        byte[] totpEnc = br.com.cofredigital.crypto.EnvelopeUtil.encrypt(totp);

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(BcryptUtil.hashpw(pw, 8));
        u.setTotpEncrypted(totpEnc);
        // certificate and privateKey left null for admin when created interactively
        dao.save(u);
        System.out.println("Administrador criado com sucesso.");
    }

    public static String[] getGrupos() {
        return database.getGrupos();
    }

    public boolean validateAdminPhrase(String phrase) {
        // simple validation: check phrase equals file content of vault/Files/index.env? For demo accept any non-empty phrase
        return phrase != null && !phrase.trim().isEmpty();
    }

    public User findByEmail(String email) {
        try { return dao.findByEmail(email); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void registerInteractive() throws Exception {
        System.out.print("Novo usuário - email: ");
        String email = Input.readLine();
        System.out.print("Senha: ");
        String pw = Input.readLineMasked();
        System.out.print("Caminho para certificado (.cer/.pem): ");
        String certPath = Input.readLine();
        byte[] cert = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(certPath));
        System.out.print("Caminho para chave privada (PKCS8): ");
        String keyPath = Input.readLine();
        byte[] priv = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(keyPath));

        // validate private key by signing random challenge
        byte[] challenge = new byte[32]; rnd.nextBytes(challenge);
        boolean valid = false;
        try {
            byte[] sig = br.com.cofredigital.crypto.SignatureUtil.sign(priv, challenge);
            valid = br.com.cofredigital.crypto.SignatureUtil.verify(cert, challenge, sig);
        } catch (Exception ex) {
            valid = false;
        }
        if (!valid) {
            System.out.println("Falha ao validar chave/certificado. Registro abortado.");
            return;
        }

        byte[] totp = new byte[20]; rnd.nextBytes(totp);
        byte[] totpEnc = br.com.cofredigital.crypto.EnvelopeUtil.encrypt(totp);
        byte[] privEnc = br.com.cofredigital.crypto.EnvelopeUtil.encrypt(priv);

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(BcryptUtil.hashpw(pw, 8));
        u.setCertificate(cert);
        u.setPrivateKeyEncrypted(privEnc);
        u.setTotpEncrypted(totpEnc);
        dao.save(u);
        System.out.println("Usuário criado com sucesso.");
    }

    public void registerPasswordError(User u) {
        u.setPasswordErrors(u.getPasswordErrors() + 1);
        if (u.getPasswordErrors() >= 3) {
            u.setBlocked(true);
            u.setBlockUntil(System.currentTimeMillis() + 2 * 60 * 1000);
            u.setPasswordErrors(0);
        }
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void registerTokenError(User u) {
        u.setTokenErrors(u.getTokenErrors() + 1);
        if (u.getTokenErrors() >= 3) {
            u.setBlocked(true);
            u.setBlockUntil(System.currentTimeMillis() + 2 * 60 * 1000);
            u.setTokenErrors(0);
        }
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }

    public void resetErrorCounts(User u) {
        u.setPasswordErrors(0); u.setTokenErrors(0); u.setBlocked(false); u.setBlockUntil(0);
        try { dao.save(u); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
