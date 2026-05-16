package br.com.cofredigital.model;

import java.time.Instant;

/**
 * Modelo mínimo de usuário para o Cofre Digital.
 * Os campos armazenados correspondem às colunas do banco em UserDAO.
 */
public class User {

    private Long id;
    private String email;
    private String group;
    private String userName;
    private String senha;
    private int total_users;
    private int total_consultas;
    private String frase_secreta;
    private String passwordHash;
    private byte[] certificate; // X.509 bytes
    private byte[] privateKeyEncrypted; // encrypted PKCS8 bytes
    private byte[] totpEncrypted; // AES-encrypted TOTP secret
    private String base32token; // TOTP secret in Base32, for display purposes only (not stored in DB)
    private boolean blocked;
    private long blockUntil; // epoch millis
    private int passwordErrors;
    private int tokenErrors;
    private int kid;

    public User() {}

    public User(String email, String senha, String senhaHash, String token, String frase_secreta, String grupo) {
        setEmail(email);
        setPasswordHash(senhaHash);
        this.senha = senha;
        setFraseSecreta(frase_secreta);
        setBase32token(token);
        this.group = grupo;
    }

    public User(String email, String senha, String frase_secreta) {
        setEmail(email);
        setPasswordHash(senha);
        setFraseSecreta(frase_secreta);
    }

    public User(String email, String senha, byte[] token, String grupo, int total_users, int total_consultas) {
        setEmail(email);
        setPasswordHash(senha);
        this.privateKeyEncrypted = token;
        this.group = grupo;
        setTotalUsers(total_users);
        setTotalConsultas(total_consultas);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; setUserName(); }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getUserName() { return userName; }
    public void setUserName() { this.userName = email.substring(0, email.indexOf('@')); } // Extrai o nome do email

    public int getTotalUsers() { return total_users; }
    public void setTotalUsers(int total_users) { this.total_users = total_users; }

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

    public String getFraseSecreta() { return frase_secreta; }
    public void setFraseSecreta(String frase_secreta) { this.frase_secreta = frase_secreta; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }  

    public int getTotalConsultas() { return total_consultas; }
    public void setTotalConsultas(int total_consultas) { this.total_consultas = total_consultas; }

    public String getBase32token() { return base32token; }
    public void setBase32token(String base32token) { this.base32token = base32token; }

    public int getKid() { return kid; }
    public void setKid(int kid) { this.kid = kid; }
}