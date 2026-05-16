package br.com.cofredigital.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {

    // Anexa um registro referenciando mid (sem usuario)
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

    // Anexa um registro com mid, email do usuario e timestamp
    public void addLog(int mid, String email, String timestamp) {
        addLog(mid, email, timestamp, null);
    }

    // Anexa um registro completo com mid, email, timestamp e arquivo selecionado
    public void addLog(int mid, String email, String timestamp, String arquivo) {
        int uid = UserDAO.getUserID(email);
        if (uid == -1) {
            return;
        }

        String sql = "INSERT INTO Registros (mid, uid, arquivo, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ps.setInt(2, uid);
            if (arquivo != null) {
                ps.setString(3, arquivo);
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setString(4, timestamp);
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error adding log entry: " + e.getMessage());
        }
    }

    // Lista logs em ordem cronologica com data, hora, codigo, usuario e arquivo
    public List<String> listChronological() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT r.rid, r.mid, r.timestamp, r.arquivo, u.email "
                       + "FROM Registros r LEFT JOIN Usuarios u ON r.uid = u.uid "
                       + "ORDER BY r.rid ASC";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<String> out = new ArrayList<>();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append(rs.getString("timestamp"));
                sb.append(" | MID=").append(rs.getInt("mid"));
                if (rs.getString("email") != null) {
                    sb.append(" | ").append(rs.getString("email"));
                }
                if (rs.getString("arquivo") != null) {
                    sb.append(" | arquivo=").append(rs.getString("arquivo"));
                }
                out.add(sb.toString());
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
            if (rs.next()) return rs.getString("conteudo");
        } catch(Exception e) {
            System.err.println("Error fetching message for mid " + mid + ": " + e.getMessage());
        }

        return null;
    }
}
