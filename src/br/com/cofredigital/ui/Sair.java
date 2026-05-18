// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.database.LogDAO;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sair extends JFrame {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private User usuario;

    public Sair(User usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Saida");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(480, 340));
        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));

        JLabel titleLabel = new JLabel("Deseja sair do sistema?");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(titleLabel);

        String grupoDisplay = "usuario".equals(usuario.getGroup()) ? "Usuario" : usuario.getGroup();
        JLabel userInfo = new JLabel("<html><div style='text-align:center'><b>" + usuario.getUserName() + "</b> &middot; " + usuario.getEmail() + " &middot; " + grupoDisplay + "</div></html>");
        userInfo.setFont(UIManager.getFont("Label.font"));
        userInfo.setForeground(MUTED);
        userInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(userInfo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(260, 1));
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(16));

        JLabel statsLabel = new JLabel("Total de acessos: " + usuario.getTotalUsers());
        statsLabel.setFont(UIManager.getFont("Label.font"));
        statsLabel.setForeground(TEXT);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        card.add(statsLabel);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(CARD_BG);

        JButton btnEncerrarSessao = criarBotao("Encerrar Sessao", PRIMARY);
        btnEncerrarSessao.addActionListener(e -> {
            String timestamp = now();
            try {
                new LogDAO().addLog(1004, usuario.getEmail(), timestamp);
                new LogDAO().addLog(8002, usuario.getEmail(), timestamp);
            } catch (Exception ex) {}
            dispose();
            new Login().setVisible(true);
        });
        btnRow.add(btnEncerrarSessao);

        JButton btnEncerrarSistema = criarBotao("Encerrar Sistema", DANGER);
        btnEncerrarSistema.addActionListener(e -> {
            String timestamp = now();
            try {
                new LogDAO().addLog(1002, timestamp);
                new LogDAO().addLog(8003, usuario.getEmail(), timestamp);
            } catch (Exception ex) {}
            UserService.clearAdminPassphrase();
            System.exit(0);
        });
        btnRow.add(btnEncerrarSistema);

        JButton btnVoltar = criarBotao("Voltar", new Color(241, 245, 249));
        btnVoltar.setForeground(TEXT);
        btnVoltar.addActionListener(e -> {
            String timestamp = now();
            try {
                new LogDAO().addLog(8004, usuario.getEmail(), timestamp);
            } catch (Exception ex) {}
            new Principal(usuario).setVisible(true);
            dispose();
        });
        btnRow.add(btnVoltar);

        card.add(btnRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        add(card, gbc);

        pack();
        setLocationRelativeTo(null);

        String ts = now();
        try { new LogDAO().addLog(8001, usuario.getEmail(), ts); } catch (Exception ex) {}
    }

    private JButton criarBotao(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return btn;
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
