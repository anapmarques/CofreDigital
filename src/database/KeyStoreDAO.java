package database;

import java.sql.*;

public class KeyStoreDAO {
    // Salva par de chaves (certificado e chave privada criptografada) associado a uid
    public void saveKeyPair(long uid, byte[] cert, byte[] privateKeyEncrypted) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "INSERT INTO Chaveiro (uid, certificado_digital, chave_privada) VALUES (?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setLong(1, uid);
            ps.setBytes(2, cert);
            ps.setBytes(3, privateKeyEncrypted);
            ps.executeUpdate();
            ps.close();
        }
    }
}
