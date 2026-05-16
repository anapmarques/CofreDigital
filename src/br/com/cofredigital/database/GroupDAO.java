package br.com.cofredigital.database;

import br.com.cofredigital.model.Group;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    private static final String[] GROUP_NAMES = {"administrador", "usuario"};

    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT gid, nome FROM Grupos ORDER BY gid";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                groups.add(new Group(rs.getInt("gid"), rs.getString("nome")));
            }
        } catch (Exception e) {
            System.err.println("Error listing groups: " + e.getMessage());
        }
        return groups;
    }

    public String[] getGroupNames() {
        List<Group> groups = findAll();
        String[] names = new String[groups.size()];
        for (int i = 0; i < groups.size(); i++) {
            names[i] = groups.get(i).getNome();
        }
        return names;
    }

    public boolean isEmpty() {
        String sql = "SELECT COUNT(*) AS count FROM Grupos";
        try (Connection c = ConnectionFactory.getConnection();
             Statement stmt = c.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("count") == 0;
            }
        } catch (Exception e) {
            System.err.println("Error checking Grupos table: " + e.getMessage());
        }
        return true;
    }

    public void seed() {
        if (!isEmpty()) return;
        String sql = "INSERT INTO Grupos (gid, nome) VALUES (?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < GROUP_NAMES.length; i++) {
                ps.setInt(1, i + 1);
                ps.setString(2, GROUP_NAMES[i]);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error seeding groups: " + e.getMessage());
        }
    }
}
