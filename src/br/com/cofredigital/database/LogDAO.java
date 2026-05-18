// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
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

    // Lista logs em ordem cronologica com data, hora, codigo e mensagem (com placeholders substituidos)
    public List<String> listChronological() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT r.rid, r.mid, r.timestamp, r.arquivo, u.email, m.conteudo "
                       + "FROM Registros r LEFT JOIN Usuarios u ON r.uid = u.uid "
                       + "LEFT JOIN Mensagens m ON r.mid = m.mid "
                       + "ORDER BY r.rid ASC";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<String> out = new ArrayList<>();
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                sb.append(rs.getString("timestamp"));
                sb.append(" | ").append(rs.getInt("mid"));
                String msg = rs.getString("conteudo");
                if (msg != null) {
                    String email = rs.getString("email");
                    if (email != null) {
                        msg = msg.replace("{login_name}", email);
                    }
                    else {
                        msg = msg.replace("{login_name}", "?");
                    }
                    String arquivo = rs.getString("arquivo");
                    if (arquivo != null) {
                        msg = msg.replace("{arq_name}", arquivo);
                    }
                    else {
                        msg = msg.replace("{arq_name}", "?");
                    }
                    sb.append(" ").append(msg);
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
