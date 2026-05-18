// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.database;

import br.com.cofredigital.model.User;
import java.sql.*;
import br.com.cofredigital.crypto.EnvelopeUtil;

public class UserDAO {

    // Salva ou atualiza um usuário no banco
    public void save(User u) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            if (u.getId() == null) {
                String sql = "INSERT INTO Usuarios (email, senha, frase_secreta, chave_secreta, certificado, chave_privada, grupo) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, u.getEmail());
                ps.setString(2, u.getPasswordHash());
                ps.setString(3, u.getFraseSecreta());
                ps.setBytes(4, u.getTotpEncrypted());
                ps.setBytes(5, u.getCertificate());
                ps.setBytes(6, u.getPrivateKeyEncrypted());
                ps.setString(7, u.getGroup() != null ? u.getGroup() : "usuario");
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) u.setId(rs.getLong(1));
                ps.close();
            } else {
                String sql = "UPDATE Usuarios SET senha=?, frase_secreta=?, chave_secreta=COALESCE(?, chave_secreta), certificado=COALESCE(?, certificado), chave_privada=COALESCE(?, chave_privada), blocked=?, blockUntil=?, passwordErrors=?, tokenErrors=? WHERE uid=?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, u.getPasswordHash());
                ps.setString(2, u.getFraseSecreta());
                ps.setBytes(3, u.getTotpEncrypted());
                ps.setBytes(4, u.getCertificate());
                ps.setBytes(5, u.getPrivateKeyEncrypted());
                ps.setInt(6, u.isBlocked() ? 1 : 0);
                ps.setLong(7, u.getBlockUntil());
                ps.setInt(8, u.getPasswordErrors());
                ps.setInt(9, u.getTokenErrors());
                ps.setLong(10, u.getId());
                ps.executeUpdate();
                ps.close();
            }
        } catch(Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public User createUserWithKeyPair(User u, String certPem, byte[] encryptedPrivateKey) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "INSERT INTO Usuarios (email, senha, frase_secreta, chave_secreta, grupo) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFraseSecreta());
            ps.setBytes(4, u.getTotpEncrypted());
            ps.setString(5, u.getGroup() != null ? u.getGroup() : "usuario");
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            long uid = -1;
            if (rs.next()) uid = rs.getLong(1);
            ps.close();
            if (uid == -1) throw new RuntimeException("Failed to create user, no UID generated.");

            u.setId(uid);
            KeyStoreDAO kdao = new KeyStoreDAO();
            int kid = kdao.saveKeyPair(uid, certPem, encryptedPrivateKey);
            if (kid == -1) throw new RuntimeException("Failed to save key pair in Chaveiro.");

            String updateSql = "UPDATE Usuarios SET kid = ? WHERE uid = ?";
            PreparedStatement ups = c.prepareStatement(updateSql);
            ups.setInt(1, kid);
            ups.setLong(2, uid);
            ups.executeUpdate();
            ups.close();

            u.setKid(kid);
            return u;
        } catch(Exception e) {
            System.err.println("Error creating user with key pair: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM Usuarios WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User(
                    rs.getString("email"),
                    rs.getString("senha"),
                    rs.getBytes("chave_secreta"),
                    rs.getString("grupo"),
                    rs.getInt("total_acessos"),
                    rs.getInt("total_consultas")
                );
                u.setId(rs.getLong("uid"));
                u.setBlocked(rs.getInt("blocked") == 1);
                u.setBlockUntil(rs.getLong("blockUntil"));
                u.setPasswordErrors(rs.getInt("passwordErrors"));
                u.setTokenErrors(rs.getInt("tokenErrors"));
                u.setFraseSecreta(rs.getString("frase_secreta"));
                u.setTotpEncrypted(rs.getBytes("chave_secreta"));
                return u;
            }
        } catch(Exception e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean hasAny() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT count(*) FROM Usuarios";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            boolean any = rs.next() && rs.getInt(1) > 0;
            st.close();
            return any;
        }
    }
    
    public int getNumUsuarios() {
        String sql = "SELECT count(*) FROM Usuarios";
        try (Connection c = ConnectionFactory.getConnection()) {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            int count = rs.next() ? rs.getInt(1) : 0;
            st.close();
            return count;
        } catch(Exception e) {
            System.err.println("Error counting users: " + e.getMessage());
            return 0;
        }
    }

    public String getUserCertificado(String email) {
        int kid = KeyStoreDAO.getUserKeyId(email);
        if (kid == -1) {
            return null;
        }

        String sql = "SELECT certificado_digital FROM Chaveiro WHERE kid = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, kid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("certificado_digital");
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching user certificate: " + e.getMessage());
        }

        return null;
    }

    public String getUserPasswordHash(String email) {
        String sql = "SELECT senha FROM Usuarios WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("senha");
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching user password hash: " + e.getMessage());
        }

        return null;
    }

    public User getUser(String email) throws Exception {
        return findByEmail(email);
    }

    public boolean userExists(User user) {
        String sql = "SELECT COUNT(*) FROM Usuarios WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next() && rs.getInt(1) > 0;
            ps.close();
            return exists;
        } catch(Exception e) {
            System.err.println("Error checking if user exists: " + e.getMessage());
            return false;
        }
    }

    public static int getUserID(String email) {
        String sql = "SELECT uid FROM Usuarios WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uid");
            ps.close();
        } catch (Exception e) {
            System.err.println("Error fetching user ID: " + e.getMessage());
        }
        return -1;
    }

    public void saveFraseSecreta(Long uid, String fraseSecretaHash) {
        String sql = "UPDATE Usuarios SET frase_secreta = ? WHERE uid = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, fraseSecretaHash);
            ps.setLong(2, uid);
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error saving frase secreta: " + e.getMessage());
        }
    }

    public String getAdminFraseSecreta() throws Exception {
        String sql = "SELECT frase_secreta FROM Usuarios WHERE uid = 1";
        try (Connection c = ConnectionFactory.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString("frase_secreta");
        }
        return null;
    }

    public void incrementAccessCount(User u) throws Exception {
        String sql = "UPDATE Usuarios SET total_acessos = total_acessos + 1 WHERE uid = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, u.getId());
            ps.executeUpdate();
            u.setTotalUsers(u.getTotalUsers() + 1);
        }
    }

    public void addUser(User user, byte[] chave_privada, String certificado) {
        if (userExists(user)) {
            return;
        }

        int kid = KeyStoreDAO.addKeyChain(chave_privada, certificado);

        if(kid == -1) {
            return;
        }

        String base32Secret = br.com.cofredigital.util.Base32.encode(user.getBase32token().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] encryptedSecret;
        try {
            encryptedSecret = EnvelopeUtil.encrypt(
                base32Secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), user.getSenha());
        } catch (Exception e) {
            System.err.println("Error encrypting TOTP secret: " + e.getMessage());
            return;
        }

        String sql = "INSERT INTO Usuarios (email, senha, kid, chave_secreta, grupo, total_acessos, total_consultas) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setInt(3, kid);
            ps.setBytes(4, encryptedSecret);
            ps.setString(5, user.getGroup());
            ps.setInt(6, 0);
            ps.setInt(7, 0);
            ps.executeUpdate();
            ps.close();
        } catch(Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }
}
