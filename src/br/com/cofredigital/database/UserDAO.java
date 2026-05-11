package br.com.cofredigital.database;

import br.com.cofredigital.model.User;
import java.sql.*;

public class UserDAO {

    // Salva ou atualiza um usuário no banco
    public void save(User u) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            if (u.getId() == null) {
                String sql = "INSERT INTO Usuarios (login_name, senha, chave_secreta, certificado, chave_privada) VALUES (?, ?, ?, ?, ?)";
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
        }
    }

    public User findByEmail(String email) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT uid, login_name, senha, chave_secreta, certificado, chave_privada, blocked, blockUntil, passwordErrors, tokenErrors FROM Usuarios WHERE login_name = ?";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            User u = new User();
            u.setId(rs.getLong("uid"));
            u.setEmail(rs.getString("login_name"));
            u.setPasswordHash(rs.getString("senha"));
            u.setTotpEncrypted(rs.getBytes("chave_secreta"));
            u.setCertificate(rs.getBytes("certificado"));
            u.setPrivateKeyEncrypted(rs.getBytes("chave_privada"));
            u.setBlocked(rs.getInt("blocked") == 1);
            u.setBlockUntil(rs.getLong("blockUntil"));
            u.setPasswordErrors(rs.getInt("passwordErrors"));
            u.setTokenErrors(rs.getInt("tokenErrors"));
            ps.close();
            return u;
        }
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
}
