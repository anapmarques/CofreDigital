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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;
import br.com.cofredigital.database.LogDAO;

public class Cadastro extends JFrame {
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
        this.total_users = user.getTotalUsers();
        this.groups = groups;
        this.user = user;

        openRegisterScreen();
    }

    private void openRegisterScreen() {
        setTitle("Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JLabel headerLabel = new JLabel("<html>Login: " + loginName + "<br>" + "Grupo: " + groupName + "<br>" + "Nome: " + userName + "</html>");
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(headerLabel);

        JLabel labelCorpo1 = new JLabel("<html>Total de usuários: " + total_users + "</html>");
        labelCorpo1.setAlignmentX(Component.CENTER_ALIGNMENT);
        labelCorpo1.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPane.add(labelCorpo1);

        JPanel painelCadastro = new JPanel(new GridLayout(6, 2, 5, 5));
        painelCadastro.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblCertificado = new JLabel("Caminho do Certificado:");
        JTextField txtCertificado = new JTextField(20);

        JLabel lblChavePrivada = new JLabel("Caminho da Chave Privada:");
        JTextField txtChavePrivada = new JTextField(20);

        JLabel lblFraseSecreta = new JLabel("Frase Secreta:");
        JPasswordField txtFraseSecreta = new JPasswordField(20);

        JLabel lblGrupo = new JLabel("Grupo:");
        JComboBox<String> comboGrupo = new JComboBox<>(groups);

        JLabel lblSenha = new JLabel("Senha Pessoal:");
        JPasswordField txtSenha = new JPasswordField(10);

        JLabel lblConfirmacaoSenha = new JLabel("Confirmação Senha:");
        JPasswordField txtConfirmacaoSenha = new JPasswordField(10);

        painelCadastro.add(lblCertificado);
        painelCadastro.add(txtCertificado);
        painelCadastro.add(lblChavePrivada);
        painelCadastro.add(txtChavePrivada);
        painelCadastro.add(lblFraseSecreta);
        painelCadastro.add(txtFraseSecreta);
        painelCadastro.add(lblGrupo);
        painelCadastro.add(comboGrupo);
        painelCadastro.add(lblSenha);
        painelCadastro.add(txtSenha);
        painelCadastro.add(lblConfirmacaoSenha);
        painelCadastro.add(txtConfirmacaoSenha);

        contentPane.add(painelCadastro);

        JPanel painelBotoesCadastro = new JPanel(new FlowLayout());
        JButton btnCadastrar = new JButton("Cadastrar");
        btnCadastrar.setEnabled(false);
        JButton btnVoltar = new JButton("Voltar");
        if (user == null){
            btnVoltar.setEnabled(false);
        }

        DocumentListener passwordLengthListener = createPasswordLengthListener(txtSenha, txtConfirmacaoSenha, btnCadastrar, 8);
        txtSenha.getDocument().addDocumentListener(passwordLengthListener);
        txtConfirmacaoSenha.getDocument().addDocumentListener(passwordLengthListener);

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

                JPanel painelConfirmacao = new JPanel(new GridLayout(7, 2, 5, 5));
                painelConfirmacao.add(new JLabel("Vers\u00e3o:"));
                painelConfirmacao.add(new JLabel(versao));
                painelConfirmacao.add(new JLabel("S\u00e9rie:"));
                painelConfirmacao.add(new JLabel(serie));
                painelConfirmacao.add(new JLabel("Validade:"));
                painelConfirmacao.add(new JLabel(valInicio + " a " + valFim));
                painelConfirmacao.add(new JLabel("Tipo de Assinatura:"));
                painelConfirmacao.add(new JLabel(tipoAssinatura));
                painelConfirmacao.add(new JLabel("Emissor:"));
                painelConfirmacao.add(new JLabel(emissor));
                painelConfirmacao.add(new JLabel("Sujeito (Friendly Name):"));
                painelConfirmacao.add(new JLabel(friendlyName));
                painelConfirmacao.add(new JLabel("E-mail:"));
                painelConfirmacao.add(new JLabel(email));

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

                    JPanel painelTOTP = new JPanel(new BorderLayout(10, 10));
                    JPanel painelInfo = new JPanel(new GridLayout(4, 1, 5, 5));
                    painelInfo.add(new JLabel("<html><b>Configura\u00e7\u00e3o do Google Authenticator</b></html>"));
                    painelInfo.add(new JLabel("Secret (BASE32): " + base32Secret));
                    painelInfo.add(new JLabel("URI: " + totpUri));
                    painelInfo.add(new JLabel("<html><i>Use o secret acima ou escaneie o QRCode com o Google Authenticator.</i></html>"));
                    painelTOTP.add(painelInfo, BorderLayout.NORTH);

                    try {
                        BufferedImage qrImage = QRCodeUtil.generateQRCodeImage(totpUri, 250, 250);
                        JLabel lblQR = new JLabel(new ImageIcon(qrImage));
                        lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);
                        JPanel painelQR = new JPanel();
                        painelQR.add(lblQR);
                        painelTOTP.add(painelQR, BorderLayout.CENTER);
                    } catch (Exception ex) {
                        painelInfo.add(new JLabel("(QRCode n\u00e3o dispon\u00edvel)"));
                    }

                    JOptionPane.showMessageDialog(this, painelTOTP, "TOTP - Google Authenticator", JOptionPane.INFORMATION_MESSAGE);

                    JOptionPane.showMessageDialog(this, "Usu\u00e1rio cadastrado com sucesso! KID: " + novoUsuario.getKid(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);

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

        painelBotoesCadastro.add(btnCadastrar);
        painelBotoesCadastro.add(btnVoltar);

        contentPane.add(painelBotoesCadastro);

        new LogDAO().addLog(6001, loginName, now());
        pack();
    }

    private boolean temSequenciaRepetida(String senha) {
        if (senha.matches("(\\d)\\1{7,}")) return true;
        if (senha.matches(".*(\\d)\\1\\1.*")) return true;
        return false;
    }

    private String extrairEmailDoSujeito(String sujeito) {
        String[] parts = sujeito.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.toUpperCase().startsWith("EMAILADDRESS=")) {
                return part.substring("EMAILADDRESS=".length());
            }
            if (part.startsWith("1.2.840.113549.1.9.1=")) {
                return part.substring("1.2.840.113549.1.9.1=".length());
            }
        }
        return null;
    }

    private String extrairCNDoSujeito(String sujeito) {
        String[] parts = sujeito.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.toUpperCase().startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return null;
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
