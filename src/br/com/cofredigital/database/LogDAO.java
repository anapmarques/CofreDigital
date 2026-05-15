package br.com.cofredigital.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    // Anexa um registro referenciando mid e uid (sem texto)
    public void addLog(int mid, String timestamp) throws Exception {
        String sql = "INSERT INTO Registros (mid, timestamp) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ps.setString(2, timestamp);
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error adding log entry: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void addLog(int mid, String email, String timestamp) {
        int uid = UserDAO.getUserID(email);
        if (uid == -1) {
            System.out.println("User with email " + email + " not found. Log entry will be added with null uid.");
            return;
        }

        String sql = "INSERT INTO Registros (mid, uid, timestamp) VALUES (?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ps.setInt(2, uid);
            ps.setString(3, timestamp);
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error adding log entry: " + e.getMessage());
        }
    }

    // Lista logs em ordem cronológica com timestamp, mid e uid
    public List<String> listChronological() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT r.rid, r.mid, r.uid, r.timestamp, m.conteudo FROM Registros r LEFT JOIN Mensagens m ON r.mid = m.mid ORDER BY r.rid ASC";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<String> out = new ArrayList<>();
            while (rs.next()) {
                out.add(rs.getString("timestamp") + " | mid=" + rs.getInt("mid") + " | uid=" + rs.getObject("uid"));
            }
            st.close();
            return out;
        }
    }

    public String getMessage(int mid) {
        String sql = "SELECT conteudo FROM Mensagens WHERE mid = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("mensagem");
        } catch(Exception e) {
            System.err.println("Error fetching message for mid " + mid + ": " + e.getMessage());
        }

        return null;
    }
}
