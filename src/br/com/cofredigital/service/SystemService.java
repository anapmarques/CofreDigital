// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.service;

import br.com.cofredigital.crypto.EnvelopeUtil;
import br.com.cofredigital.crypto.SignatureUtil;
import br.com.cofredigital.util.Base32;
import br.com.cofredigital.database.Database;
import br.com.cofredigital.database.KeyStoreDAO;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
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

    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private static JPanel criarCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        return card;
    }

    private static String showAdminPhraseDialog() {
        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Autenticacao do Administrador");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 16f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(titleLabel);

        JLabel lbl = new JLabel("Frase secreta da chave privada do administrador:");
        lbl.setFont(UIManager.getFont("Label.font"));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(lbl);

        JPasswordField field = new JPasswordField(20);
        field.setFont(UIManager.getFont("PasswordField.font"));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setMaximumSize(new Dimension(300, 36));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(field);

        KeyStoreDAO keyStoreDAO = new KeyStoreDAO();
        byte[] adminPrivEncrypted = keyStoreDAO.getAdminPrivateKey();
        Database db = new Database();
        String adminCertPem = db.getAdminCertificado();

        if (adminPrivEncrypted == null || adminCertPem == null) {
            JOptionPane.showMessageDialog(null,
                "Dados do administrador nao encontrados no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }

        byte[] adminCertBytes = adminCertPem.getBytes(StandardCharsets.UTF_8);

        int result = JOptionPane.showConfirmDialog(null, card,
            "Autenticacao do Administrador", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        String phrase = new String(field.getPassword());

        try {
            PrivateKey privateKey = SignatureUtil.decryptPrivateKey(adminPrivEncrypted, phrase);
            byte[] challenge = new byte[9216];
            rnd.nextBytes(challenge);
            byte[] sig = SignatureUtil.sign(privateKey, challenge);
            boolean valid = SignatureUtil.verify(adminCertBytes, challenge, sig);
            if (valid) {
                UserService.setAdminPassphrase(phrase);
                return phrase;
            }
        } catch (Exception ex) {
        }

        JOptionPane.showMessageDialog(null,
            "Frase secreta invalida. O sistema sera encerrado.", "Erro", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
        return null;
    }

    private static boolean showFirstTimeSetup() throws Exception {
        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Primeiro Acesso");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(titleLabel);

        JLabel noAdminLabel = new JLabel("Nenhum administrador cadastrado");
        noAdminLabel.setFont(UIManager.getFont("Label.font"));
        noAdminLabel.setForeground(TEXT);
        noAdminLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        noAdminLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(noAdminLabel);

        JLabel subtitleLabel = new JLabel("Criar o primeiro administrador do sistema");
        subtitleLabel.setFont(UIManager.getFont("Label.font"));
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(subtitleLabel);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(360, 1));
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(14));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        JPasswordField txtSenha = new JPasswordField(22);
        txtSenha.setFont(UIManager.getFont("PasswordField.font"));
        txtSenha.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPasswordField txtConfirmar = new JPasswordField(22);
        txtConfirmar.setFont(UIManager.getFont("PasswordField.font"));
        txtConfirmar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JTextField txtCert = new JTextField(22);
        txtCert.setFont(UIManager.getFont("TextField.font"));
        txtCert.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JTextField txtKey = new JTextField(22);
        txtKey.setFont(UIManager.getFont("TextField.font"));
        txtKey.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPasswordField txtPhrase = new JPasswordField(22);
        txtPhrase.setFont(UIManager.getFont("PasswordField.font"));
        txtPhrase.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        gbc.gridx = 0; gbc.gridy = 2;
        JLabel l3 = new JLabel("Certificado (.cer/.pem):"); l3.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f)); l3.setForeground(TEXT);
        formPanel.add(l3, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel certRow = new JPanel(new BorderLayout(6, 0));
        certRow.setBackground(CARD_BG);
        certRow.add(txtCert, BorderLayout.CENTER);
        JButton btnCert = new JButton("\uD83D\uDCC1");
        btnCert.setFont(UIManager.getFont("Button.font"));
        btnCert.setBackground(new Color(241, 245, 249));
        btnCert.setFocusPainted(false);
        btnCert.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnCert.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione o certificado (.cer/.pem)");
            if (chooser.showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
                txtCert.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        certRow.add(btnCert, BorderLayout.EAST);
        formPanel.add(certRow, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 3;
        JLabel l4 = new JLabel("Chave Privada (PKCS8):"); l4.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f)); l4.setForeground(TEXT);
        formPanel.add(l4, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel keyRow = new JPanel(new BorderLayout(6, 0));
        keyRow.setBackground(CARD_BG);
        keyRow.add(txtKey, BorderLayout.CENTER);
        JButton btnKey = new JButton("\uD83D\uDCC1");
        btnKey.setFont(UIManager.getFont("Button.font"));
        btnKey.setBackground(new Color(241, 245, 249));
        btnKey.setFocusPainted(false);
        btnKey.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnKey.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Selecione a chave privada (PKCS8)");
            if (chooser.showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
                txtKey.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        keyRow.add(btnKey, BorderLayout.EAST);
        formPanel.add(keyRow, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel l1 = new JLabel("Senha (8-10 digitos):"); l1.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f)); l1.setForeground(TEXT);
        formPanel.add(l1, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtSenha, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel l2 = new JLabel("Confirmar senha:"); l2.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f)); l2.setForeground(TEXT);
        formPanel.add(l2, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtConfirmar, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        gbc.gridx = 0; gbc.gridy = 4;
        JLabel l5 = new JLabel("Frase secreta:"); l5.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f)); l5.setForeground(TEXT);
        formPanel.add(l5, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtPhrase, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;

        card.add(formPanel);

        new LogDAO().addLog(6001, now());

        while (true) {
            if (JOptionPane.showConfirmDialog(null, card,
                    "Primeiro Acesso", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
                return false;
            }

            String certPath = txtCert.getText().trim();
            String keyPath = txtKey.getText().trim();
            String password = new String(txtSenha.getPassword());
            String confirm = new String(txtConfirmar.getPassword());
            String phrase = new String(txtPhrase.getPassword());

            if (!password.matches("\\d{8,10}")) {
                JOptionPane.showMessageDialog(null, "Senha invalida! Deve conter apenas numeros e ter 8 a 10 digitos.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(null, "As senhas nao conferem.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (temSequenciaRepetida(password)) {
                JOptionPane.showMessageDialog(null, "A senha nao pode conter sequencias de numeros repetidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (certPath.isEmpty() || !Files.exists(Paths.get(certPath))) {
                JOptionPane.showMessageDialog(null, "Selecione um arquivo de certificado valido.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (keyPath.isEmpty() || !Files.exists(Paths.get(keyPath))) {
                JOptionPane.showMessageDialog(null, "Selecione um arquivo de chave privada valido.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }
            if (phrase.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Informe a frase secreta da chave privada.", "Erro", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                byte[] certBytes = Files.readAllBytes(Paths.get(certPath));
                byte[] privEncryptedBytes = Files.readAllBytes(Paths.get(keyPath));

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate x509cert;
                try (FileInputStream fis = new FileInputStream(certPath)) {
                    x509cert = (X509Certificate) cf.generateCertificate(fis);
                }

                String sujeito = x509cert.getSubjectX500Principal().getName();
                String email = extrairEmailDoSujeito(sujeito);
                String friendlyName = extrairCNDoSujeito(sujeito);

                if (email == null || friendlyName == null) {
                    JOptionPane.showMessageDialog(null, "Nao foi possivel extrair o e-mail do campo Sujeito do certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                String versao = "V" + x509cert.getVersion();
                String serie = x509cert.getSerialNumber().toString(16);
                Date valInicio = x509cert.getNotBefore();
                Date valFim = x509cert.getNotAfter();
                String tipoAssinatura = x509cert.getSigAlgName();
                String emissor = x509cert.getIssuerX500Principal().getName();

                JPanel painelConfirmacao = new JPanel(new GridBagLayout());
                painelConfirmacao.setBackground(CARD_BG);
                painelConfirmacao.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
                GridBagConstraints cgbc = new GridBagConstraints();
                cgbc.insets = new Insets(3, 4, 3, 8);
                cgbc.anchor = GridBagConstraints.WEST;

                cgbc.gridx = 0; cgbc.gridy = 0; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblVersao = new JLabel("Versao:"); lblVersao.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblVersao.setForeground(TEXT);
                painelConfirmacao.add(lblVersao, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valVersao = new JLabel(versao); valVersao.setFont(UIManager.getFont("Label.font")); valVersao.setForeground(MUTED);
                painelConfirmacao.add(valVersao, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 1; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblSerie = new JLabel("Serie:"); lblSerie.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblSerie.setForeground(TEXT);
                painelConfirmacao.add(lblSerie, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valSerie = new JLabel(serie); valSerie.setFont(UIManager.getFont("Label.font")); valSerie.setForeground(MUTED);
                painelConfirmacao.add(valSerie, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 2; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblValidade = new JLabel("Validade:"); lblValidade.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblValidade.setForeground(TEXT);
                painelConfirmacao.add(lblValidade, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valValidade = new JLabel(valInicio + " a " + valFim); valValidade.setFont(UIManager.getFont("Label.font")); valValidade.setForeground(MUTED);
                painelConfirmacao.add(valValidade, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 3; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblAssinatura = new JLabel("Assinatura:"); lblAssinatura.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblAssinatura.setForeground(TEXT);
                painelConfirmacao.add(lblAssinatura, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valAssinatura = new JLabel(tipoAssinatura); valAssinatura.setFont(UIManager.getFont("Label.font")); valAssinatura.setForeground(MUTED);
                painelConfirmacao.add(valAssinatura, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 4; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblEmissor = new JLabel("Emissor:"); lblEmissor.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblEmissor.setForeground(TEXT);
                painelConfirmacao.add(lblEmissor, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valEmissor = new JLabel(emissor); valEmissor.setFont(UIManager.getFont("Label.font")); valEmissor.setForeground(MUTED);
                painelConfirmacao.add(valEmissor, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 5; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblNome = new JLabel("Sujeito:"); lblNome.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblNome.setForeground(TEXT);
                painelConfirmacao.add(lblNome, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valNome = new JLabel(friendlyName); valNome.setFont(UIManager.getFont("Label.font")); valNome.setForeground(MUTED);
                painelConfirmacao.add(valNome, cgbc);

                cgbc.gridx = 0; cgbc.gridy = 6; cgbc.fill = GridBagConstraints.NONE;
                JLabel lblEmail = new JLabel("E-mail:"); lblEmail.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD)); lblEmail.setForeground(TEXT);
                painelConfirmacao.add(lblEmail, cgbc);
                cgbc.gridx = 1; cgbc.fill = GridBagConstraints.HORIZONTAL;
                JLabel valEmail = new JLabel(email); valEmail.setFont(UIManager.getFont("Label.font")); valEmail.setForeground(MUTED);
                painelConfirmacao.add(valEmail, cgbc);

                int confirmResult = JOptionPane.showConfirmDialog(null, painelConfirmacao,
                    "Confirmacao do Administrador", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (confirmResult != JOptionPane.YES_OPTION) {
                    continue;
                }

                new LogDAO().addLog(6002, email, now());

                PrivateKey privateKey;
                try {
                    privateKey = SignatureUtil.decryptPrivateKey(privEncryptedBytes, phrase);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,
                        "Frase secreta invalida ou chave privada nao pode ser decifrada.", "Erro", JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(null,
                        "Falha ao validar a assinatura digital da chave privada com o certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    continue;
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

                JPanel totpPanel = new JPanel(new BorderLayout(12, 12));
                totpPanel.setBackground(CARD_BG);
                totpPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBackground(CARD_BG);
                JLabel titulo = new JLabel("Configuracao do Google Authenticator");
                titulo.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 15f));
                titulo.setForeground(PRIMARY);
                titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
                infoPanel.add(titulo);
                infoPanel.add(Box.createVerticalStrut(10));
                JLabel secretLabel = new JLabel("Secret (BASE32): " + base32Secret);
                secretLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                secretLabel.setForeground(TEXT);
                secretLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                infoPanel.add(secretLabel);
                infoPanel.add(Box.createVerticalStrut(4));
                JLabel uriLabel = new JLabel("URI: " + totpUri);
                uriLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
                uriLabel.setForeground(MUTED);
                uriLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                infoPanel.add(uriLabel);
                infoPanel.add(Box.createVerticalStrut(8));
                JLabel instrucao = new JLabel("<html><div style='text-align:center;'>Use o secret acima ou escaneie o QR Code com o Google Authenticator.</div></html>");
                instrucao.setFont(UIManager.getFont("Label.font"));
                instrucao.setForeground(MUTED);
                instrucao.setAlignmentX(Component.CENTER_ALIGNMENT);
                infoPanel.add(instrucao);
                totpPanel.add(infoPanel, BorderLayout.NORTH);

                try {
                    java.awt.image.BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(totpUri, 250, 250);
                    JLabel lblQR = new JLabel(new ImageIcon(qrImage));
                    lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                    JPanel qrPanel = new JPanel();
                    qrPanel.setBackground(CARD_BG);
                    qrPanel.add(lblQR);
                    totpPanel.add(qrPanel, BorderLayout.CENTER);
                } catch (Exception ex) {
                    infoPanel.add(new JLabel("(QRCode nao disponivel)"));
                }

                JOptionPane.showMessageDialog(null, totpPanel, "TOTP - Google Authenticator", JOptionPane.INFORMATION_MESSAGE);
                JOptionPane.showMessageDialog(null,
                    "Administrador criado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                new LogDAO().addLog(6008, email, now());
                return true;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null,
                    "Erro ao criar administrador: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static boolean temSequenciaRepetida(String senha) {
        if (senha.matches("(\\d)\\1{7,}")) return true;
        if (senha.matches(".*(\\d)\\1\\1.*")) return true;
        return false;
    }

    private static String extrairEmailDoSujeito(String sujeito) {
        Pattern p = Pattern.compile(
            "(?:^|[,+])\\s*(?:EMAILADDRESS|emailAddress|1\\.2\\.840\\.113549\\.1\\.9\\.1)=([^,+]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sujeito);
        if (m.find()) {
            return decodeHexValue(m.group(1).trim());
        }
        return null;
    }

    private static String extrairCNDoSujeito(String sujeito) {
        Pattern p = Pattern.compile(
            "(?:^|[,+])\\s*CN=([^,+]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sujeito);
        if (m.find()) {
            return decodeHexValue(m.group(1).trim());
        }
        return null;
    }

    private static String decodeHexValue(String value) {
        if (!value.startsWith("#")) return value;
        try {
            String hex = value.substring(1);
            byte[] der = new byte[hex.length() / 2];
            for (int i = 0; i < der.length; i++) {
                der[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
            }
            int pos = 1;
            int len;
            if ((der[pos] & 0x80) == 0) {
                len = der[pos] & 0xFF;
                pos++;
            } else {
                int numBytes = der[pos] & 0x7F;
                pos++;
                len = 0;
                for (int i = 0; i < numBytes; i++) {
                    len = (len << 8) | (der[pos] & 0xFF);
                    pos++;
                }
            }
            return new String(der, pos, len, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
