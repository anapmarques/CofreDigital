// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import br.com.cofredigital.auth.AuthService;
import br.com.cofredigital.model.User;
import br.com.cofredigital.database.LogDAO;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Login extends JFrame {
    private JTextField loginField;
    private AuthService authService = new AuthService();

    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    public Login() {
        setTitle("Cofre Digital - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 320);
        setMinimumSize(new Dimension(440, 280));
        setResizable(false);

        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(BG);
        setContentPane(contentPane);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(32, 36, 28, 36)
        ));

        JLabel titleLabel = new JLabel("Acessar Cofre Digital");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        card.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Informe seu e-mail para continuar");
        subtitleLabel.setFont(UIManager.getFont("Label.font"));
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        card.add(subtitleLabel);

        JLabel label = new JLabel("E-mail");
        label.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        label.setForeground(TEXT);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        card.add(label);

        loginField = new JTextField(22);
        loginField.setFont(UIManager.getFont("TextField.font"));
        loginField.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginField.setMaximumSize(new Dimension(320, 36));
        loginField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        card.add(loginField);

        card.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(CARD_BG);

        JButton btnLogin = createPrimaryButton("Entrar");
        btnLogin.addActionListener(e -> fazerLogin());
        btnRow.add(btnLogin);

        JButton btnLimpar = createSecondaryButton("Limpar");
        btnLimpar.addActionListener(e -> loginField.setText(""));
        btnRow.add(btnLimpar);

        JButton btnSair = createSecondaryButton("Sair");
        btnSair.addActionListener(e -> System.exit(0));
        btnRow.add(btnSair);

        card.add(btnRow);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 20, 20, 20);
        contentPane.add(card, gbc);

        setLocationRelativeTo(null);
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIManager.getFont("Label.font"));
        btn.setForeground(TEXT);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void fazerLogin() {
        String email = loginField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o email.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            new LogDAO().addLog(2001, now());

            User user = authService.findUser(email);
            if (user == null) {
                new LogDAO().addLog(2005, email, now());
                JOptionPane.showMessageDialog(this, "Email nao encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (user.isCurrentlyBlocked()) {
                new LogDAO().addLog(2004, email, now());
                JOptionPane.showMessageDialog(this,
                    "Usuario bloqueado. Acesso liberado a partir de: " + formatBlockTime(user.getBlockUntil()),
                    "Bloqueado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new LogDAO().addLog(2003, email, now());
            new LogDAO().addLog(2002, now());

            // Etapa 2 - senha (loop, sai apenas se acertar ou for bloqueado)
            String password;
            new LogDAO().addLog(3001, email, now());
            while (true) {
                password = VirtualKeyboardPanel.showPasswordDialog(this, user.getPasswordHash());
                if (password == null) {
                    new LogDAO().addLog(3002, email, now());
                    return;
                }
                if (!password.isEmpty()) break;

                int errs = user.getPasswordErrors();
                if (errs == 0) new LogDAO().addLog(3004, email, now());
                else if (errs == 1) new LogDAO().addLog(3005, email, now());
                else if (errs == 2) new LogDAO().addLog(3006, email, now());

                JOptionPane.showMessageDialog(this, "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
                authService.registerPasswordError(user);
                if (user.isCurrentlyBlocked()) {
                    new LogDAO().addLog(3007, email, now());
                    JOptionPane.showMessageDialog(this,
                        "Acesso bloqueado. Acesso liberado a partir de: " + formatBlockTime(user.getBlockUntil()),
                        "Bloqueado", JOptionPane.ERROR_MESSAGE);
                    new LogDAO().addLog(3002, email, now());
                    return;
                }
            }

            new LogDAO().addLog(3003, email, now());
            new LogDAO().addLog(3002, email, now());

            // Etapa 3 - TOTP (loop, sai apenas se acertar ou for bloqueado)
            new LogDAO().addLog(4001, email, now());
            while (true) {
                JPanel totpPanel = new JPanel(new GridBagLayout());
                totpPanel.setBackground(Color.WHITE);
                totpPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(4, 0, 4, 0);
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JLabel totpTitle = new JLabel("Autenticacao de dois fatores");
                totpTitle.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 14f));
                totpTitle.setForeground(TEXT);
                totpPanel.add(totpTitle, gbc);

                gbc.gridy = 1;
                JLabel totpDesc = new JLabel("<html>Informe o codigo de 6 digitos<br>do Google Authenticator:</html>");
                totpDesc.setFont(UIManager.getFont("Label.font"));
                totpDesc.setForeground(MUTED);
                totpPanel.add(totpDesc, gbc);

                gbc.gridy = 2;
                gbc.insets = new Insets(8, 0, 4, 0);
                JTextField totpField = new JTextField(12);
                totpField.setFont(UIManager.getFont("TextField.font").deriveFont(Font.BOLD, 20f));
                totpField.setHorizontalAlignment(SwingConstants.CENTER);
                totpField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
                totpPanel.add(totpField, gbc);

                int totpResult = JOptionPane.showConfirmDialog(this, totpPanel,
                    "Autenticacao", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
                if (totpResult != JOptionPane.OK_OPTION) {
                    new LogDAO().addLog(4002, email, now());
                    return;
                }

                String totpCode = totpField.getText().trim();
                if (totpCode.isEmpty()) {
                    new LogDAO().addLog(4002, email, now());
                    continue;
                }

                boolean totpOk;
                try {
                    totpOk = authService.verifyTOTP(user, totpCode, password);
                } catch (Exception ex) {
                    new LogDAO().addLog(4002, email, now());
                    JOptionPane.showMessageDialog(this, "Falha ao validar TOTP.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!totpOk) {
                    int errs = user.getTokenErrors();
                    if (errs == 0) new LogDAO().addLog(4004, email, now());
                    else if (errs == 1) new LogDAO().addLog(4005, email, now());
                    else if (errs == 2) new LogDAO().addLog(4006, email, now());

                    JOptionPane.showMessageDialog(this, "TOTP invalido!", "Erro", JOptionPane.ERROR_MESSAGE);
                    authService.registerTokenError(user);
                    if (user.isCurrentlyBlocked()) {
                        new LogDAO().addLog(4007, email, now());
                        JOptionPane.showMessageDialog(this,
                            "Acesso bloqueado. Acesso liberado a partir de: " + formatBlockTime(user.getBlockUntil()),
                            "Bloqueado", JOptionPane.ERROR_MESSAGE);
                        new LogDAO().addLog(4002, email, now());
                        return;
                    }
                    continue;
                }
                break;
            }

            new LogDAO().addLog(4003, email, now());
            new LogDAO().addLog(4002, email, now());

            authService.resetErrors(user);
            new LogDAO().addLog(1003, email, now());

            new Principal(user).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private static String formatBlockTime(long blockUntilMillis) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(blockUntilMillis));
    }
}
