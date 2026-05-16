package br.com.cofredigital.database;

import br.com.cofredigital.model.Message;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    private static final String LOG_VIEW = "logView/Log.csv";

    public String getContent(int mid) {
        String sql = "SELECT conteudo FROM Mensagens WHERE mid = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, mid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("conteudo");
        } catch (Exception e) {
            System.err.println("Error fetching message for mid " + mid + ": " + e.getMessage());
        }
        return null;
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM Mensagens";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (Exception e) {
            System.err.println("Error checking Mensagens table: " + e.getMessage());
        }
        return true;
    }

    public void seed() {
        if (!isEmpty()) return;
        String sql = "INSERT INTO Mensagens (mid, conteudo) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            InputStream input = new FileInputStream(LOG_VIEW);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ", 2);
                String mid = values[0];
                String mensagem = values[1];
                mensagem = mensagem.replaceAll("<", "{").replaceAll(">", "}");
                ps.setString(1, mid);
                ps.setString(2, mensagem);
                ps.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error filling Mensagens table: " + e.getMessage());
        }
    }
}
