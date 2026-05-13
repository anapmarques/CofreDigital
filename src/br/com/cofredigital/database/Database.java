package br.com.cofredigital.database;

import br.com.cofredigital.model.User;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;

public class Database {
    private static final String DB_URL = ConnectionFactory.URL;
    private static final String [] GROUP_NAMES = {"administrador", "usuario"};
    private static final String LOG_VIEW = "logView/Log.csv";

    private static HashMap<String, User> users = new HashMap<>();

    public static boolean temUsuarios() {
        return !users.isEmpty();
    }

    public static User buscarUser(String email) {
        return users.get(email);
    }

    public static void salvar(User user) {
        users.put(user.getEmail(), user);
    }

    public String[] getGrupos() {
        String query = "SELECT nome FROM Grupos";
        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            String[] grupos = new String[GROUP_NAMES.length];
            int i = 0;
            while (rs.next()) {
                grupos[i++] = rs.getString("nome");
            }
            return grupos;
        } catch (SQLException e) {
            System.err.println("Error getting groups: " + e.getMessage());
        }
        return null;
    }

    public Map<String, LocalDateTime> getBlockedUsers() {
        Map<String, LocalDateTime> blockedUsers = new HashMap<>();
        String query = "SELECT email, bloqueado_ultima_vez FROM Usuarios WHERE bloqueado_ultima_vez IS NOT NULL";

        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String email = rs.getString("email");
                LocalDateTime blockedUntil = rs.getTimestamp("bloqueado_ultima_vez").toLocalDateTime();
                blockedUsers.put(email, blockedUntil);
            }
        } catch (SQLException e) {
            System.err.println("Error getting blocked users: " + e.getMessage());
        }

        return blockedUsers;
    }

    public void updateBlockedUsers(Map<String, LocalDateTime> blockedUsers) {
        String updateQuery = "UPDATE Usuarios SET bloqueado_ultima_vez = ? WHERE email = ?";

        try (Connection con = DriverManager.getConnection(DB_URL);
                PreparedStatement pstmt = con.prepareStatement(updateQuery)) {
            for (Map.Entry<String, LocalDateTime> entry : blockedUsers.entrySet()) {
                pstmt.setTimestamp(1, Timestamp.valueOf(entry.getValue()));
                pstmt.setString(2, entry.getKey());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error updating blocked users: " + e.getMessage());
        }
    }

    private boolean isMensagensTableEmpty() {
        String query = "SELECT COUNT(*) AS count FROM Mensagens";
        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking Mensagens table: " + e.getMessage());
        }
        return true;
    }

    private boolean isGruposTableEmpty() {
        String query = "SELECT COUNT(*) AS count FROM Grupos";
        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking Grupos table: " + e.getMessage());
        }
        return true;
    }

    public boolean isAdminRegistered() {
        String query = "SELECT COUNT(*) AS count FROM Usuarios u JOIN Grupos g ON u.gid = g.id WHERE g.nome = 'administrador'";
        try (Connection con = DriverManager.getConnection(DB_URL);
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking admin registration: " + e.getMessage());
        }
        return false;
    }

    private void fillMessages(Connection conn) {
        String sql = "INSERT INTO Mensagens (mid, conteudo) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            InputStream input = getClass().getResourceAsStream(LOG_VIEW);
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

    private void fillGrupos(Connection conn) {
        String sql = "INSERT INTO Grupos (gid, nome) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < GROUP_NAMES.length; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, GROUP_NAMES[i]);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error filling Grupos table: " + e.getMessage());
        }   
    }

    public String getAdminCertificado() {
        int kid = KeyStoreDAO.getAdminKeyId();
        if (kid == -1) {
            System.out.println("Admin user not found or has no key assigned.");
            return null;
        }

        String sql = "SELECT certificado_digital FROM Chaveiro WHERE kid = ?";
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, kid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("certificado_digital");
            ps.close();
        } catch(Exception e) {
            System.err.println("Error fetching admin certificate: " + e.getMessage());
        }

        return null;
    }

    public void updateTotalConsultas(User user) {
        user.setTotalConsultas(user.getTotalConsultas() + 1);

        String sql = "UPDATE Usuarios SET total_consultas = total_consultas + 1 WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error updating total consultas: " + e.getMessage());
        }
    }

    public void updateAcessoCount(User user) {
        user.setTotalUsers(user.getTotalUsers() + 1);

        String sql = "UPDATE Usuarios SET total_users = total_users + 1 WHERE email = ?";
        try (Connection c = ConnectionFactory.getConnection();  
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.executeUpdate();
        } catch(Exception e) {
            System.err.println("Error updating acesso count: " + e.getMessage());
        }
    }
}