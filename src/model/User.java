package model;

import java.time.Instant;

/**
 * Modelo mínimo de usuário para o Cofre Digital.
 * Os campos armazenados correspondem às colunas do banco em UserDAO.
 */
public class User {

    private Long id;
    private String email;
    private String passwordHash;
    private byte[] certificate; // X.509 bytes
    private byte[] privateKeyEncrypted; // encrypted PKCS8 bytes
    private byte[] totpEncrypted; // AES-encrypted TOTP secret
    private boolean blocked;
    private long blockUntil; // epoch millis
    private int passwordErrors;
    private int tokenErrors;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public byte[] getCertificate() { return certificate; }
    public void setCertificate(byte[] certificate) { this.certificate = certificate; }

    public byte[] getPrivateKeyEncrypted() { return privateKeyEncrypted; }
    public void setPrivateKeyEncrypted(byte[] privateKeyEncrypted) { this.privateKeyEncrypted = privateKeyEncrypted; }

    public byte[] getTotpEncrypted() { return totpEncrypted; }
    public void setTotpEncrypted(byte[] totpEncrypted) { this.totpEncrypted = totpEncrypted; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public long getBlockUntil() { return blockUntil; }
    public void setBlockUntil(long blockUntil) { this.blockUntil = blockUntil; }

    public int getPasswordErrors() { return passwordErrors; }
    public void setPasswordErrors(int passwordErrors) { this.passwordErrors = passwordErrors; }

    public int getTokenErrors() { return tokenErrors; }
    public void setTokenErrors(int tokenErrors) { this.tokenErrors = tokenErrors; }

    public boolean isCurrentlyBlocked() {
        return blocked && Instant.now().toEpochMilli() < blockUntil;
    }
}