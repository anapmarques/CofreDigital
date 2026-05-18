// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.service.VaultService;
import br.com.cofredigital.service.VaultService.VaultFileEntry;
import br.com.cofredigital.service.VaultService.VaultResult;
import br.com.cofredigital.database.LogDAO;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Principal extends JFrame {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private User usuario;

    public Principal(User usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 380));
        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        if ("administrador".equals(usuario.getGroup())) {
            configurarTelaAdmin();
        } else if ("usuario".equals(usuario.getGroup())) {
            configurarTelaUser();
        } else {
            JOptionPane.showMessageDialog(this, "Grupo invalido.");
            dispose();
        }
    }

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        return card;
    }

    private JButton criarBotaoPrincipal(String texto, String descricao) {
        JPanel btnPanel = new JPanel(new BorderLayout());
        btnPanel.setBackground(CARD_BG);
        btnPanel.setMaximumSize(new Dimension(340, 52));

        JButton btn = new JButton(texto);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(UIManager.getFont("Label.font"));
        btn.setForeground(TEXT);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        return btn;
    }

    private void configurarTelaAdmin() {
        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Menu do Administrador");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(titleLabel);

        JLabel userInfo = new JLabel("<html><div style='text-align:center'><b>" + usuario.getUserName() + "</b> &middot; " + usuario.getEmail() + "</div></html>");
        userInfo.setFont(UIManager.getFont("Label.font"));
        userInfo.setForeground(MUTED);
        userInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(userInfo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(300, 1));
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JLabel statsLabel = new JLabel("Total de acessos: " + usuario.getTotalUsers());
        statsLabel.setFont(UIManager.getFont("Label.font"));
        statsLabel.setForeground(TEXT);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(statsLabel);

        JButton btnCadastrar = criarBotaoPrincipal("Cadastrar novo usuario", null);
        btnCadastrar.addActionListener(e -> {
            new LogDAO().addLog(5002, usuario.getEmail(), now());
            String[] grupos = new br.com.cofredigital.database.Database().getGrupos();
            new Cadastro(usuario, grupos).setVisible(true);
            dispose();
        });
        card.add(btnCadastrar);
        card.add(Box.createVerticalStrut(10));

        JButton btnConsultar = criarBotaoPrincipal("Consultar arquivos secretos", null);
        btnConsultar.addActionListener(e -> {
            new LogDAO().addLog(5003, usuario.getEmail(), now());
            consultarVault();
        });
        card.add(btnConsultar);
        card.add(Box.createVerticalStrut(16));

        JButton btnSair = criarBotaoSecundario("Sair do Sistema");
        btnSair.addActionListener(e -> {
            new LogDAO().addLog(5004, usuario.getEmail(), now());
            new Sair(usuario).setVisible(true);
            dispose();
        });
        card.add(btnSair);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(card, gbc);

        pack();
        setLocationRelativeTo(null);
        new LogDAO().addLog(5001, usuario.getEmail(), now());
    }

    private void configurarTelaUser() {
        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Menu do Usuario");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(titleLabel);

        JLabel userInfo = new JLabel("<html><div style='text-align:center'><b>" + usuario.getUserName() + "</b> &middot; " + usuario.getEmail() + "</div></html>");
        userInfo.setFont(UIManager.getFont("Label.font"));
        userInfo.setForeground(MUTED);
        userInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(userInfo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(300, 1));
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JLabel statsLabel = new JLabel("Total de acessos: " + usuario.getTotalUsers());
        statsLabel.setFont(UIManager.getFont("Label.font"));
        statsLabel.setForeground(TEXT);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        card.add(statsLabel);

        JButton btnConsultar = criarBotaoPrincipal("Consultar arquivos secretos", null);
        btnConsultar.addActionListener(e -> {
            new LogDAO().addLog(5003, usuario.getEmail(), now());
            consultarVault();
        });
        card.add(btnConsultar);
        card.add(Box.createVerticalStrut(16));

        JButton btnSair = criarBotaoSecundario("Sair do Sistema");
        btnSair.addActionListener(e -> {
            new LogDAO().addLog(5004, usuario.getEmail(), now());
            new Sair(usuario).setVisible(true);
            dispose();
        });
        card.add(btnSair);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(card, gbc);

        pack();
        setLocationRelativeTo(null);
        new LogDAO().addLog(5001, usuario.getEmail(), now());
    }

    private void consultarVault() {
        dispose();
        new ConsultarVault(usuario).setVisible(true);
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
