package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LogDAO {
    // Anexa um registro referenciando mid e uid (sem texto)
    public void append(int mid, Long uid) throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "INSERT INTO Registros (mid, uid, timestamp) VALUES (?, ?, datetime('now'))";
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, mid);
            if (uid == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, uid);
            ps.executeUpdate();
            ps.close();
        }
    }

    // Lista logs em ordem cronológica com timestamp, mid e uid
    public List<String> listChronological() throws Exception {
        try (Connection c = ConnectionFactory.getConnection()) {
            String sql = "SELECT r.rid, r.mid, r.uid, r.timestamp, m.mensagem FROM Registros r LEFT JOIN Mensagens m ON r.mid = m.mid ORDER BY r.rid ASC";
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
}
