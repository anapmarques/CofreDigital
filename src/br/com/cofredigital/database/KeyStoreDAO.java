// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.database;

import java.sql.*;

public class KeyStoreDAO {

    // Salva par de chaves (certificado PEM e chave privada criptografada) associado a uid e retorna o KID
    public int saveKeyPair(long uid, String certPem, byte[] privateKeyEncrypted) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "INSERT INTO Chaveiro (uid, certificado_digital, chave_privada) VALUES (?, ?, ?)";
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, uid);
            ps.setString(2, certPem);
            ps.setBytes(3, privateKeyEncrypted);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int kid = rs.next() ? rs.getInt(1) : -1;
            ps.close();
            return kid;
        }
    }

    public static int addKeyChain(byte[] chave_privada, String certificado_digital) {
        String sql = "INSERT INTO Chaveiro (certificado_digital, chave_privada) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection()) {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setString(1, certificado_digital);
            ps.setBytes(2, chave_privada);
            ps.executeUpdate();


            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            ps.close();
        } catch(Exception e) {
            System.err.println("Error saving key pair: " + e.getMessage());
        }

        return -1;
    }

    public static int getAdminKeyId() {
        String sql = "SELECT kid FROM Usuarios WHERE uid = 1";
        int kid = -1;
        try (Connection c = ConnectionFactory.getConnection()) {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) kid = rs.getInt("kid");
            st.close();
        } catch(Exception e) {
            System.err.println("Error fetching admin key ID: " + e.getMessage());
            return -1;
        }
        return kid;
    }

    public byte[] getAdminPrivateKey() {
        int kid = getAdminKeyId();
        if (kid == -1) {
            return null;
        }

        String sql = "SELECT chave_privada FROM Chaveiro WHERE kid = ?";
        try (Connection c = ConnectionFactory.getConnection()) {
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, kid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("chave_privada");
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching admin private key: " + e.getMessage());
        }

        return null;
    }

    public static int getUserKeyId(String email) {
        String sql = "SELECT kid FROM Usuarios WHERE email = ?";
        int kid = -1;
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) kid = rs.getInt("kid");
            else return -1;
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching user key ID: " + e.getMessage());
            return -1;
        }
        return kid;
    }

    public byte[] getUserPrivateKey(String email) {
        String sql = "SELECT chave_privada FROM Chaveiro WHERE kid = (SELECT kid FROM Usuarios WHERE email = ?)";
        try (Connection c = ConnectionFactory.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("chave_privada");
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching user private key: " + e.getMessage());
        }

        return null;
    }
}
