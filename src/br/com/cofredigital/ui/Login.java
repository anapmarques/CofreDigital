package br.com.cofredigital.ui;

import br.com.cofredigital.auth.AuthService;
import br.com.cofredigital.model.User;

import javax.swing.*;
import java.awt.*;

public class Login extends JFrame {
    private JTextField loginField;
    private JButton btnLogin;
    private AuthService authService = new AuthService();

    public Login() {
        setTitle("Cofre Digital - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JPanel loginPanel = new JPanel(new FlowLayout());
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Email (do certificado): ");
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        loginField = new JTextField(25);
        loginPanel.add(label);
        loginPanel.add(loginField);
        contentPane.add(loginPanel);

        JPanel painelBotoes = new JPanel(new FlowLayout());
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnLogin = new JButton("Login");
        JButton btnLimpar = new JButton("Limpar");
        JButton btnSair = new JButton("Sair");

        btnLogin.addActionListener(e -> fazerLogin());
        btnLimpar.addActionListener(e -> loginField.setText(""));
        btnSair.addActionListener(e -> System.exit(0));

        painelBotoes.add(btnLogin);
        painelBotoes.add(btnLimpar);
        painelBotoes.add(btnSair);
        contentPane.add(painelBotoes);

        pack();
        setLocationRelativeTo(null);
    }

    private void fazerLogin() {
        String email = loginField.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o email.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User user = authService.findUser(email);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Email nao encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (user.isCurrentlyBlocked()) {
                JOptionPane.showMessageDialog(this, "Usuario bloqueado.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String passwordLengthStr = JOptionPane.showInputDialog(this,
                "Quantos digitos tem sua senha? (8, 9 ou 10):", "Senha",
                JOptionPane.QUESTION_MESSAGE);
            if (passwordLengthStr == null) return;
            int passwordLength;
            try {
                passwordLength = Integer.parseInt(passwordLengthStr.trim());
                if (passwordLength < 8 || passwordLength > 10) {
                    JOptionPane.showMessageDialog(this, "A senha deve ter 8 a 10 digitos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Valor invalido.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String password = VirtualKeyboardPanel.showPasswordDialog(this, passwordLength, user.getPasswordHash());
            if (password == null) {
                JOptionPane.showMessageDialog(this, "Senha incorreta!", "Erro", JOptionPane.ERROR_MESSAGE);
                authService.registerPasswordError(user);
                if (user.isCurrentlyBlocked()) {
                    JOptionPane.showMessageDialog(this,
                        "Acesso bloqueado por 2 minutos devido a multiplos erros.", "Bloqueado",
                        JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            String totpCode = JOptionPane.showInputDialog(this,
                "TOTP (6 digitos):", "Autenticacao", JOptionPane.QUESTION_MESSAGE);
            if (totpCode == null || totpCode.trim().isEmpty()) return;

            boolean totpOk;
            try {
                totpOk = authService.verifyTOTP(user, totpCode.trim(), password);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Falha ao validar TOTP.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!totpOk) {
                JOptionPane.showMessageDialog(this, "TOTP invalido!", "Erro", JOptionPane.ERROR_MESSAGE);
                authService.registerTokenError(user);
                if (user.isCurrentlyBlocked()) {
                    JOptionPane.showMessageDialog(this,
                        "Acesso bloqueado por 2 minutos devido a multiplos erros de token.", "Bloqueado",
                        JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            authService.resetErrors(user);

            new Principal(user).setVisible(true);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
