// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.database.UserDAO;
import br.com.cofredigital.util.BcryptUtil;
import br.com.cofredigital.crypto.EnvelopeUtil;
import br.com.cofredigital.crypto.SignatureUtil;
import br.com.cofredigital.util.QRCodeUtil;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;
import br.com.cofredigital.database.LogDAO;

public class Cadastro extends JFrame {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SUCCESS = new Color(16, 185, 129);

    private String loginName;
    private String groupName;
    private String userName;
    private int total_users;
    private String[] groups;

    private User user;
    private SecureRandom rnd = new SecureRandom();

    public Cadastro(String loginName, String groupName, String userName, int total_users, String[] groups) {
        this.loginName = loginName;
        this.groupName = groupName;
        this.userName = userName;
        this.total_users = total_users;
        this.groups = groups;

        openRegisterScreen();
    }

    public Cadastro(User user, String[] groups) {
        this.loginName = user.getEmail();
        this.groupName = user.getGroup();
        this.userName = user.getUserName();
        this.total_users = new UserDAO().getNumUsuarios();
        this.groups = groups;
        this.user = user;

        openRegisterScreen();
    }

    private JPanel criarCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)
        ));
        return card;
    }

    private JButton criarBotaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(UIManager.getFont("Button.font"));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private JButton criarBotaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(UIManager.getFont("Label.font"));
        btn.setForeground(TEXT);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void openRegisterScreen() {
        setTitle("Cofre Digital - Cadastro de Usuario");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(580, 520));
        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Cadastrar novo usuario");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Login: " + loginName + "  |  Grupo: " + groupName);
        subtitleLabel.setFont(UIManager.getFont("Label.font"));
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        card.add(subtitleLabel);

        JLabel statsLabel = new JLabel("Total de usuarios: " + total_users);
        statsLabel.setFont(UIManager.getFont("Label.font"));
        statsLabel.setForeground(MUTED);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(statsLabel);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(400, 1));
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

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lblCertificado = new JLabel("Certificado:");
        lblCertificado.setForeground(TEXT);
        lblCertificado.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblCertificado, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JTextField txtCertificado = new JTextField(22);
        txtCertificado.setFont(UIManager.getFont("TextField.font"));
        txtCertificado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPanel painelCert = new JPanel(new BorderLayout(6, 0));
        painelCert.setBackground(CARD_BG);
        painelCert.add(txtCertificado, BorderLayout.CENTER);
        JButton btnCert = new JButton("\uD83D\uDCC1");
        btnCert.setFont(UIManager.getFont("Button.font"));
        btnCert.setBackground(new Color(241, 245, 249));
        btnCert.setFocusPainted(false);
        btnCert.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnCert.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(Cadastro.this) == JFileChooser.APPROVE_OPTION) {
                txtCertificado.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        painelCert.add(btnCert, BorderLayout.EAST);
        formPanel.add(painelCert, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(4, 4, 4, 6);
        JLabel lblChavePrivada = new JLabel("Chave Privada:");
        lblChavePrivada.setForeground(TEXT);
        lblChavePrivada.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblChavePrivada, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JTextField txtChavePrivada = new JTextField(22);
        txtChavePrivada.setFont(UIManager.getFont("TextField.font"));
        txtChavePrivada.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPanel painelKey = new JPanel(new BorderLayout(6, 0));
        painelKey.setBackground(CARD_BG);
        painelKey.add(txtChavePrivada, BorderLayout.CENTER);
        JButton btnKey = new JButton("\uD83D\uDCC1");
        btnKey.setFont(UIManager.getFont("Button.font"));
        btnKey.setBackground(new Color(241, 245, 249));
        btnKey.setFocusPainted(false);
        btnKey.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnKey.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(Cadastro.this) == JFileChooser.APPROVE_OPTION) {
                txtChavePrivada.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        painelKey.add(btnKey, BorderLayout.EAST);
        formPanel.add(painelKey, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(4, 4, 4, 6);
        JLabel lblFraseSecreta = new JLabel("Frase Secreta:");
        lblFraseSecreta.setForeground(TEXT);
        lblFraseSecreta.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblFraseSecreta, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JPasswordField txtFraseSecreta = new JPasswordField(22);
        txtFraseSecreta.setFont(UIManager.getFont("PasswordField.font"));
        txtFraseSecreta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(txtFraseSecreta, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(4, 4, 4, 6);
        JLabel lblGrupo = new JLabel("Grupo:");
        lblGrupo.setForeground(TEXT);
        lblGrupo.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblGrupo, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JComboBox<String> comboGrupo = new JComboBox<>(groups);
        comboGrupo.setFont(UIManager.getFont("Label.font"));
        comboGrupo.setBackground(Color.WHITE);
        comboGrupo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        formPanel.add(comboGrupo, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(4, 4, 4, 6);
        JLabel lblSenha = new JLabel("Senha Pessoal:");
        lblSenha.setForeground(TEXT);
        lblSenha.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblSenha, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JPasswordField txtSenha = new JPasswordField(22);
        txtSenha.setFont(UIManager.getFont("PasswordField.font"));
        txtSenha.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(txtSenha, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; gbc.insets = new Insets(4, 4, 4, 6);
        JLabel lblConfirmacaoSenha = new JLabel("Confirmar Senha:");
        lblConfirmacaoSenha.setForeground(TEXT);
        lblConfirmacaoSenha.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        formPanel.add(lblConfirmacaoSenha, gbc);

        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(4, 0, 4, 0);
        JPasswordField txtConfirmacaoSenha = new JPasswordField(22);
        txtConfirmacaoSenha.setFont(UIManager.getFont("PasswordField.font"));
        txtConfirmacaoSenha.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(txtConfirmacaoSenha, gbc);

        card.add(formPanel);
        card.add(Box.createVerticalStrut(14));

        JPanel painelBotoesCadastro = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        painelBotoesCadastro.setBackground(CARD_BG);
        JButton btnCadastrar = criarBotaoPrimario("Cadastrar");
        btnCadastrar.setEnabled(false);
        JButton btnVoltar = criarBotaoSecundario("Voltar");
        if (user == null){
            btnVoltar.setEnabled(false);
        }

        DocumentListener passwordLengthListener = createPasswordLengthListener(txtSenha, txtConfirmacaoSenha, btnCadastrar, 8);
        txtSenha.getDocument().addDocumentListener(passwordLengthListener);
        txtConfirmacaoSenha.getDocument().addDocumentListener(passwordLengthListener);

        painelBotoesCadastro.add(btnCadastrar);
        painelBotoesCadastro.add(btnVoltar);
        card.add(painelBotoesCadastro);

        GridBagConstraints gbcRoot = new GridBagConstraints();
        gbcRoot.gridx = 0;
        gbcRoot.gridy = 0;
        gbcRoot.insets = new Insets(16, 16, 16, 16);
        getContentPane().add(card, gbcRoot);

        btnCadastrar.addActionListener((ActionEvent e) -> {
            new LogDAO().addLog(6002, loginName, now());

            String senha = new String(txtSenha.getPassword());
            String confirmacao = new String(txtConfirmacaoSenha.getPassword());

            if (!senha.equals(confirmacao)) {
                new LogDAO().addLog(6003, loginName, now());
                JOptionPane.showMessageDialog(this, "As senhas digitadas n\u00e3o conferem.", "Erro de Valida\u00e7\u00e3o", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!senha.matches("\\d{8,10}")) {
                new LogDAO().addLog(6003, loginName, now());
                JOptionPane.showMessageDialog(this, "A senha deve conter apenas n\u00fameros e ter de 8 a 10 d\u00edgitos.", "Erro de Valida\u00e7\u00e3o", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (temSequenciaRepetida(senha)) {
                new LogDAO().addLog(6003, loginName, now());
                JOptionPane.showMessageDialog(this, "A senha n\u00e3o pode conter sequ\u00eancias de n\u00fameros repetidos.", "Erro de Valida\u00e7\u00e3o", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String certPath = txtCertificado.getText().trim();
                if (certPath.isEmpty()) {
                    new LogDAO().addLog(6004, loginName, now());
                    JOptionPane.showMessageDialog(this, "Informe o caminho do certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert;
                try (FileInputStream fis = new FileInputStream(certPath)) {
                    cert = (X509Certificate) cf.generateCertificate(fis);
                }

                String versao = "V" + cert.getVersion();
                String serie = cert.getSerialNumber().toString(16);
                Date valInicio = cert.getNotBefore();
                Date valFim = cert.getNotAfter();
                String tipoAssinatura = cert.getSigAlgName();
                String emissor = cert.getIssuerX500Principal().getName();
                String sujeito = cert.getSubjectX500Principal().getName();
                String email = extrairEmailDoSujeito(sujeito);
                String friendlyName = extrairCNDoSujeito(sujeito);

                if (email == null || friendlyName == null) {
                    JOptionPane.showMessageDialog(this, "N\u00e3o foi poss\u00edvel extrair o e-mail ou o nome do campo Sujeito do certificado.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                    JPanel painelConfirmacao = new JPanel(new GridBagLayout());
                painelConfirmacao.setBackground(CARD_BG);
                painelConfirmacao.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
                GridBagConstraints cgbc = new GridBagConstraints();
                cgbc.insets = new Insets(3, 4, 3, 8);
                cgbc.anchor = GridBagConstraints.WEST;
                cgbc.gridx = 0; cgbc.gridy = 0; cgbc.fill = GridBagConstraints.NONE;
                addConfirmRow(painelConfirmacao, cgbc, "Vers\u00e3o:", versao, 0);
                addConfirmRow(painelConfirmacao, cgbc, "S\u00e9rie:", serie, 1);
                addConfirmRow(painelConfirmacao, cgbc, "Validade:", valInicio + " a " + valFim, 2);
                addConfirmRow(painelConfirmacao, cgbc, "Assinatura:", tipoAssinatura, 3);
                addConfirmRow(painelConfirmacao, cgbc, "Emissor:", emissor, 4);
                addConfirmRow(painelConfirmacao, cgbc, "Sujeito:", friendlyName, 5);
                addConfirmRow(painelConfirmacao, cgbc, "E-mail:", email, 6);

                int confirm = JOptionPane.showConfirmDialog(this, painelConfirmacao, "Confirma\u00e7\u00e3o de Cadastro", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

                if (confirm == JOptionPane.NO_OPTION) {
                    new LogDAO().addLog(6009, email, now());
                }
                if (confirm == JOptionPane.YES_OPTION) {
                    new LogDAO().addLog(6008, email, now());
                    UserDAO dao = new UserDAO();
                    if (dao.findByEmail(email) != null) {
                        JOptionPane.showMessageDialog(this, "O login name (e-mail) " + email + " j\u00e1 est\u00e1 cadastrado no sistema.", "Erro de Cadastro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String keyPath = txtChavePrivada.getText().trim();
                    if (keyPath.isEmpty()) {
                        new LogDAO().addLog(6005, email, now());
                        JOptionPane.showMessageDialog(this, "Informe o caminho da chave privada.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String fraseSecreta = new String(txtFraseSecreta.getPassword());
                    if (fraseSecreta.isEmpty()) {
                        new LogDAO().addLog(6006, email, now());
                        JOptionPane.showMessageDialog(this, "Informe a frase secreta da chave privada.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    byte[] certBytes = Files.readAllBytes(Paths.get(certPath));
                    byte[] privEncryptedBytes = Files.readAllBytes(Paths.get(keyPath));

                    PrivateKey privateKey;
                    try {
                        privateKey = SignatureUtil.decryptPrivateKey(privEncryptedBytes, fraseSecreta);
                    } catch (Exception ex) {
                        new LogDAO().addLog(6006, email, now());
                        JOptionPane.showMessageDialog(this, "Frase secreta inv\u00e1lida ou chave privada n\u00e3o p\u00f4de ser decifrada.", "Erro de Valida\u00e7\u00e3o", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    byte[] challenge = new byte[9216];
                    rnd.nextBytes(challenge);
                    boolean valid = false;
                    try {
                        byte[] sig = SignatureUtil.sign(privateKey, challenge);
                        valid = SignatureUtil.verify(certBytes, challenge, sig);
                    } catch (Exception ex) {
                        valid = false;
                    }
                    if (!valid) {
                        new LogDAO().addLog(6007, email, now());
                        JOptionPane.showMessageDialog(this, "Falha ao validar a assinatura digital da chave privada com o certificado. Registro abortado.", "Erro de Valida\u00e7\u00e3o", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String certPem = converterParaPEM(cert.getEncoded());

                    byte[] totp = new byte[20];
                    rnd.nextBytes(totp);
                    String base32Secret = br.com.cofredigital.util.Base32.encode(totp);
                    byte[] totpEnc = EnvelopeUtil.encrypt(
                        base32Secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), senha);

                    String grupo = (String) comboGrupo.getSelectedItem();

                    User novoUsuario = new User();
                    novoUsuario.setEmail(email);
                    novoUsuario.setPasswordHash(BcryptUtil.hashpw(senha, 8));
                    novoUsuario.setTotpEncrypted(totpEnc);
                    novoUsuario.setFraseSecreta(BcryptUtil.hashpw(fraseSecreta, 8));
                    novoUsuario.setGroup(grupo);

                    dao.createUserWithKeyPair(novoUsuario, certPem, privEncryptedBytes);

                    String totpUri = QRCodeUtil.buildTOTPUri(email, base32Secret);

                    JPanel painelTOTP = new JPanel(new BorderLayout(14, 14));
                    painelTOTP.setBackground(CARD_BG);
                    painelTOTP.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
                    JPanel painelInfo = new JPanel();
                    painelInfo.setLayout(new BoxLayout(painelInfo, BoxLayout.Y_AXIS));
                    painelInfo.setBackground(CARD_BG);
                    JLabel tituloTotp = new JLabel("Configuracao do Google Authenticator");
                    tituloTotp.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 15f));
                    tituloTotp.setForeground(PRIMARY);
                    tituloTotp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    painelInfo.add(tituloTotp);
                    painelInfo.add(Box.createVerticalStrut(10));
                    JLabel secretLabel = new JLabel("Secret (BASE32): " + base32Secret);
                    secretLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                    secretLabel.setForeground(TEXT);
                    secretLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    painelInfo.add(secretLabel);
                    painelInfo.add(Box.createVerticalStrut(4));
                    JLabel uriLabel = new JLabel("URI: " + totpUri);
                    uriLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
                    uriLabel.setForeground(MUTED);
                    uriLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    painelInfo.add(uriLabel);
                    painelInfo.add(Box.createVerticalStrut(8));
                    JLabel instrucao = new JLabel("<html><div style='text-align:center;'>Use o secret acima ou escaneie o QR Code<br>com o Google Authenticator.</div></html>");
                    instrucao.setFont(UIManager.getFont("Label.font"));
                    instrucao.setForeground(MUTED);
                    instrucao.setAlignmentX(Component.CENTER_ALIGNMENT);
                    painelInfo.add(instrucao);
                    painelTOTP.add(painelInfo, BorderLayout.NORTH);

                    try {
                        BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(totpUri, 250, 250);
                        JLabel lblQR = new JLabel(new ImageIcon(qrImage));
                        lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                        JPanel painelQR = new JPanel();
                        painelQR.setBackground(CARD_BG);
                        painelQR.add(lblQR);
                        painelTOTP.add(painelQR, BorderLayout.CENTER);
                    } catch (Exception ex) {
                        painelInfo.add(new JLabel("(QRCode n\u00e3o dispon\u00edvel)"));
                    }

                    JOptionPane.showMessageDialog(this, painelTOTP, "TOTP - Google Authenticator", JOptionPane.INFORMATION_MESSAGE);

                    JOptionPane.showMessageDialog(this, "Usu\u00e1rio cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                    txtCertificado.setText("");
                    txtChavePrivada.setText("");
                    txtFraseSecreta.setText("");
                    comboGrupo.setSelectedIndex(0);
                    txtSenha.setText("");
                    txtConfirmacaoSenha.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao processar o cadastro: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnVoltar.addActionListener(e -> {
            new LogDAO().addLog(6010, loginName, now());
            new Principal(user).setVisible(true);
            dispose();
        });

        pack();
        setLocationRelativeTo(null);
        new LogDAO().addLog(6001, loginName, now());
    }

    private void addConfirmRow(JPanel panel, GridBagConstraints gbc, String label, String value, int row) {
        gbc.gridy = row; gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
        lbl.setForeground(TEXT);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel val = new JLabel(value);
        val.setFont(UIManager.getFont("Label.font"));
        val.setForeground(MUTED);
        panel.add(val, gbc);
    }

    private boolean temSequenciaRepetida(String senha) {
        if (senha.matches("(\\d)\\1{7,}")) return true;
        if (senha.matches(".*(\\d)\\1\\1.*")) return true;
        return false;
    }

    private String extrairEmailDoSujeito(String sujeito) {
        Pattern p = Pattern.compile(
            "(?:^|[,+])\\s*(?:EMAILADDRESS|emailAddress|1\\.2\\.840\\.113549\\.1\\.9\\.1)=([^,+]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sujeito);
        if (m.find()) {
            return decodeHexValue(m.group(1).trim());
        }
        return null;
    }

    private String extrairCNDoSujeito(String sujeito) {
        Pattern p = Pattern.compile(
            "(?:^|[,+])\\s*CN=([^,+]+)",
            Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sujeito);
        if (m.find()) {
            return decodeHexValue(m.group(1).trim());
        }
        return null;
    }

    private String decodeHexValue(String value) {
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

    private String converterParaPEM(byte[] certDerBytes) {
        String b64 = Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(certDerBytes);
        return "-----BEGIN CERTIFICATE-----\n" + b64 + "\n-----END CERTIFICATE-----\n";
    }

    private static DocumentListener createPasswordLengthListener(JPasswordField passwordField, JPasswordField confirmPasswordField, JButton button, int minLength) {
        return new DocumentListener() {
            private void updateButtonState() {
                button.setEnabled(passwordField.getPassword().length >= minLength && confirmPasswordField.getPassword().length >= minLength);
            }

            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        };
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
