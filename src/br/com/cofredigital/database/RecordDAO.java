// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.database;

import br.com.cofredigital.model.Record;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecordDAO {

    public void addLog(int mid, String timestamp) throws Exception {
        String sql = "INSERT INTO Registros (mid, timestamp) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ps.setString(2, timestamp);
            ps.executeUpdate();
        }
    }

    public void addLog(int mid, String email, String timestamp) {
        addLog(mid, email, timestamp, null);
    }

    public void addLog(int mid, String email, String timestamp, String arquivo) {
        int uid = UserDAO.getUserID(email);
        if (uid == -1) return;

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
        } catch (Exception e) {
            System.err.println("Error adding log entry: " + e.getMessage());
        }
    }

    public List<Record> listChronological() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT r.rid, r.mid, r.timestamp, r.arquivo, u.email "
                       + "FROM Registros r LEFT JOIN Usuarios u ON r.uid = u.uid "
                       + "ORDER BY r.rid ASC";
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<Record> out = new ArrayList<>();
            while (rs.next()) {
                Record rec = new Record();
                rec.setRid(rs.getInt("rid"));
                rec.setMid(rs.getInt("mid"));
                rec.setTimestamp(rs.getString("timestamp"));
                rec.setArquivo(rs.getString("arquivo"));
                out.add(rec);
            }
            st.close();
            return out;
        }
    }
}
