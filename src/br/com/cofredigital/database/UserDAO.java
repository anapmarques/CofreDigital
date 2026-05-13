package br.com.cofredigital.database;

import br.com.cofredigital.model.User;
import java.sql.*;
import br.com.cofredigital.crypto.EnvelopeUtil;

public class UserDAO {

    // Salva ou atualiza um usuário no banco
    public void save(User u) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            if (u.getId() == null) {
                String sql = "INSERT INTO Usuarios (email, senha, chave_secreta, certificado, chave_privada) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, u.getEmail());
                ps.setString(2, u.getPasswordHash());
                ps.setBytes(3, u.getTotpEncrypted());
                ps.setBytes(4, u.getCertificate());
                ps.setBytes(5, u.getPrivateKeyEncrypted());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) u.setId(rs.getLong(1));
                ps.close();
            } else {
                String sql = "UPDATE Usuarios SET senha=?, chave_secreta=?, certificado=?, chave_privada=?, blocked=?, blockUntil=?, passwordErrors=?, tokenErrors=? WHERE uid=?";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setString(1, u.getPasswordHash());
                ps.setBytes(2, u.getTotpEncrypted());
                ps.setBytes(3, u.getCertificate());
                ps.setBytes(4, u.getPrivateKeyEncrypted());
                ps.setInt(5, u.isBlocked() ? 1 : 0);
                ps.setLong(6, u.getBlockUntil());
                ps.setInt(7, u.getPasswordErrors());
                ps.setInt(8, u.getTokenErrors());
                ps.setLong(9, u.getId());
                ps.executeUpdate();
                ps.close();
            }
        } catch(Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
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
                byte[] token = rs.getBytes("chave_secreta");

                return new User(
                    rs.getString("email"),
                    rs.getString("senha"),
                    token,
                    rs.getString("grupo"),
                    rs.getInt("total_acessos"),
                    rs.getInt("total_consultas")
                );
            }
            ps.close();
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
            System.out.println("User not found or has no key assigned.");
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

    public void addUser(User user, byte[] chave_privada, String certificado) {
        if (userExists(user)) {
            System.out.println("User with email " + user.getEmail() + " already exists. Skipping add.");
            return;
        }

        int kid = KeyStoreDAO.addKeyChain(chave_privada, certificado);

        if(kid == -1) {
            System.out.println("Failed to add key chain for user " + user.getEmail() + ". User will not be added.");
            return;
        }

        byte[] encryptedPrivateKey = EnvelopeUtil.generateEncryptedTokenKey(user.getSenha(), user.getBase32token()); // Aqui você pode adicionar lógica para criptografar a chave privada se necessário

        String sql = "INSERT INTO Usuarios (email, senha, kid, chave_secreta, grupo, total_acessos, total_consultas) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setInt(3, kid);
            ps.setBytes(4, encryptedPrivateKey);
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
