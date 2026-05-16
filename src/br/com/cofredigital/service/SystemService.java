package br.com.cofredigital.service;

import br.com.cofredigital.crypto.EnvelopeUtil;
import br.com.cofredigital.crypto.SignatureUtil;
import br.com.cofredigital.util.Base32;
import br.com.cofredigital.database.Database;
import br.com.cofredigital.database.UserDAO;
import br.com.cofredigital.model.User;
import br.com.cofredigital.ui.Login;
import br.com.cofredigital.ui.Cadastro;
import br.com.cofredigital.util.BcryptUtil;
import br.com.cofredigital.util.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Date;
import br.com.cofredigital.database.LogDAO;

public class SystemService {
    private static SecureRandom rnd = new SecureRandom();
    private static String adminPassphrase = null;

    public static void start() throws Exception {
        if (!Database.temUsuarios()) {
            new LogDAO().addLog(1005, now());
            boolean created = showFirstTimeSetup();
            if (!created) return;
        } else {
            String phrase = showAdminPhraseDialog();
            if (phrase == null) return;
            adminPassphrase = phrase;
            new LogDAO().addLog(1006, now());
        }

        Runtime.getRuntime().addShutdownHook(new Thread(UserService::clearAdminPassphrase));

        new LogDAO().addLog(1001, now());
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }

    private static String showAdminPhraseDialog() {
        JPasswordField field = new JPasswordField(20);
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Frase secreta da chave privada do administrador:"), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        while (true) {
            int result = JOptionPane.showConfirmDialog(null, panel,
                "Autenticacao do Administrador", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION) return null;

            String phrase = new String(field.getPassword());
            UserService userService = new UserService();
            if (userService.validateAdminPhrase(phrase)) {
                return phrase;
            }
            JOptionPane.showMessageDialog(null, "Frase secreta invalida.", "Erro", JOptionPane.ERROR_MESSAGE);
            field.setText("");
        }
    }

    private static boolean showFirstTimeSetup() throws Exception {
        JOptionPane.showMessageDialog(null,
            "Nenhum usuario administrador encontrado.\n"
            + "Vamos criar o primeiro administrador do sistema.\n"
            + "Preencha todos os dados abaixo.",
            "Cofre Digital - Primeiro Acesso", JOptionPane.INFORMATION_MESSAGE);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField txtEmail = new JTextField(20);
        JPasswordField txtSenha = new JPasswordField(15);
        JPasswordField txtConfirmar = new JPasswordField(15);
        JTextField txtCert = new JTextField(20);
        JTextField txtKey = new JTextField(20);
        JPasswordField txtPhrase = new JPasswordField(20);
        JButton btnCert = new JButton("...");
        JButton btnKey = new JButton("...");

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        panel.add(new JLabel("Email do administrador:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(txtEmail, gbc);
        gbc.weightx = 0.0;

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(new JLabel("Senha (8-10 digitos):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(txtSenha, gbc);
        gbc.weightx = 0.0;

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Confirmar senha:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(txtConfirmar, gbc);
        gbc.weightx = 0.0;

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Certificado (.cer/.pem):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 1.0;
        panel.add(txtCert, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panel.add(btnCert, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panel.add(new JLabel("Chave Privada (PKCS8):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 1; gbc.weightx = 1.0;
        panel.add(txtKey, gbc);
        gbc.gridx = 2; gbc.gridwidth = 1; gbc.weightx = 0.0;
        panel.add(btnKey, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Frase secreta:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        panel.add(txtPhrase, gbc);
        gbc.weightx = 0.0;

        btnCert.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione o certificado (.cer/.pem)");
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                txtCert.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnKey.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione a chave privada (PKCS8)");
            if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                txtKey.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        new LogDAO().addLog(6001, now());

        while (true) {
            if (JOptionPane.showConfirmDialog(null, panel,
                    "Primeiro Acesso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                String cancelEmail = txtEmail.getText().trim();
                if (!cancelEmail.isEmpty()) new LogDAO().addLog(6009, cancelEmail, now());
                return false;
            }

            String email = txtEmail.getText().trim();
            new LogDAO().addLog(6002, email, now());
            String password = new String(txtSenha.getPassword());
            String confirm = new String(txtConfirmar.getPassword());
            String certPath = txtCert.getText().trim();
            String keyPath = txtKey.getText().trim();
            String phrase = new String(txtPhrase.getPassword());

            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Informe o email do administrador.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!password.matches("\\d{8,10}")) {
                new LogDAO().addLog(6003, email, now());
                JOptionPane.showMessageDialog(panel, "Senha invalida! Deve conter apenas numeros e ter 8 a 10 digitos.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!password.equals(confirm)) {
                new LogDAO().addLog(6003, email, now());
                JOptionPane.showMessageDialog(panel, "As senhas nao conferem.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (certPath.isEmpty() || !Files.exists(Paths.get(certPath))) {
                new LogDAO().addLog(6004, email, now());
                JOptionPane.showMessageDialog(panel, "Selecione um arquivo de certificado valido.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (keyPath.isEmpty() || !Files.exists(Paths.get(keyPath))) {
                new LogDAO().addLog(6005, email, now());
                JOptionPane.showMessageDialog(panel, "Selecione um arquivo de chave privada valido.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (phrase.isEmpty()) {
                new LogDAO().addLog(6006, email, now());
                JOptionPane.showMessageDialog(panel, "Informe a frase secreta da chave privada.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                byte[] certBytes = Files.readAllBytes(Paths.get(certPath));
                byte[] privEncryptedBytes = Files.readAllBytes(Paths.get(keyPath));

                PrivateKey privateKey;
                try {
                    privateKey = SignatureUtil.decryptPrivateKey(privEncryptedBytes, phrase);
                } catch (Exception ex) {
                    new LogDAO().addLog(6006, email, now());
                    JOptionPane.showMessageDialog(panel, ex, "erro", JOptionPane.ERROR_MESSAGE);
                    //JOptionPane.showMessageDialog(panel,
                    //    "Frase secreta invalida ou chave privada nao pode ser decifrada.", "Erro", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                byte[] challenge = new byte[9216];
                rnd.nextBytes(challenge);
                boolean valid;
                try {
                    byte[] sig = SignatureUtil.sign(privateKey, challenge);
                    valid = SignatureUtil.verify(certBytes, challenge, sig);
                } catch (Exception ex) {
                    valid = false;
                }
                if (!valid) {
                    new LogDAO().addLog(6007, email, now());
                    JOptionPane.showMessageDialog(panel,
                        "Falha ao validar a assinatura digital da chave privada com o certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate x509cert;
                try (FileInputStream fis = new FileInputStream(certPath)) {
                    x509cert = (X509Certificate) cf.generateCertificate(fis);
                }

                String certPem = "-----BEGIN CERTIFICATE-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(x509cert.getEncoded()) +
                    "\n-----END CERTIFICATE-----\n";

                byte[] totp = new byte[20];
                rnd.nextBytes(totp);
                String base32Secret = Base32.encode(totp);
                byte[] totpEnc = EnvelopeUtil.encrypt(
                    base32Secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), password);

                User u = new User();
                u.setEmail(email);
                u.setPasswordHash(BcryptUtil.hashpw(password, 8));
                u.setTotpEncrypted(totpEnc);
                u.setFraseSecreta(BcryptUtil.hashpw(phrase, 8));
                u.setGroup("administrador");
                new UserDAO().createUserWithKeyPair(u, certPem, privEncryptedBytes);
                adminPassphrase = phrase;
                UserService.setAdminPassphrase(phrase);

                String totpUri = QRCodeUtil.buildTOTPUri(email, base32Secret);

                JPanel totpPanel = new JPanel(new BorderLayout(10, 10));
                JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
                infoPanel.add(new JLabel("<html><b>Configuracao do Google Authenticator</b></html>"));
                infoPanel.add(new JLabel("Secret (BASE32): " + base32Secret));
                infoPanel.add(new JLabel("URI: " + totpUri));
                infoPanel.add(new JLabel("<html><i>Use o secret acima ou escaneie o QRCode.</i></html>"));
                totpPanel.add(infoPanel, BorderLayout.NORTH);

                try {
                    java.awt.image.BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(totpUri, 250, 250);
                    JLabel lblQR = new JLabel(new ImageIcon(qrImage));
                    lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                    JPanel qrPanel = new JPanel();
                    qrPanel.add(lblQR);
                    totpPanel.add(qrPanel, BorderLayout.CENTER);
                } catch (Exception ex) {
                    infoPanel.add(new JLabel("(QRCode nao disponivel)"));
                }

                JOptionPane.showMessageDialog(null, totpPanel, "TOTP - Google Authenticator", JOptionPane.INFORMATION_MESSAGE);
                JOptionPane.showMessageDialog(null,
                    "Administrador criado com sucesso! KID: " + u.getKid(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                new LogDAO().addLog(6008, email, now());
                return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                    "Erro ao criar administrador: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
