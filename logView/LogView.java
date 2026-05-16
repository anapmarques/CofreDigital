import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.SecureRandom;

import javax.swing.*;
import java.awt.*;

import br.com.cofredigital.crypto.SignatureUtil;
import br.com.cofredigital.database.LogDAO;

public class LogView {

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LogView - Visualizador de Registros");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout(10, 10));

            JTextArea textArea = new JTextArea(20, 60);
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

            JPanel topPanel = new JPanel(new FlowLayout());
            JButton btnCarregar = new JButton("Carregar Logs");
            topPanel.add(btnCarregar);
            frame.add(topPanel, BorderLayout.NORTH);

            btnCarregar.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Selecione a chave privada do administrador");
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                String keyPath = chooser.getSelectedFile().getAbsolutePath();

                JPasswordField passField = new JPasswordField(20);
                JPanel passPanel = new JPanel(new BorderLayout(5, 5));
                passPanel.add(new JLabel("Frase secreta da chave privada:"), BorderLayout.NORTH);
                passPanel.add(passField, BorderLayout.CENTER);
                if (JOptionPane.showConfirmDialog(frame, passPanel, "Autenticacao",
                        JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                String passphrase = new String(passField.getPassword());
                if (passphrase.isEmpty()) return;

                try {
                    byte[] privEncryptedBytes = Files.readAllBytes(Paths.get(keyPath));
                    PrivateKey privateKey = SignatureUtil.decryptPrivateKey(privEncryptedBytes, passphrase);

                    String adminCertPem = getAdminCertificate();
                    if (adminCertPem == null) {
                        JOptionPane.showMessageDialog(frame,
                            "Certificado do administrador nao encontrado no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    byte[] challenge = new byte[2048];
                    new SecureRandom().nextBytes(challenge);
                    byte[] signature = SignatureUtil.sign(privateKey, challenge);
                    boolean valid = SignatureUtil.verify(
                        adminCertPem.getBytes(StandardCharsets.UTF_8), challenge, signature);

                    if (!valid) {
                        JOptionPane.showMessageDialog(frame,
                            "Falha na autenticacao: a chave nao corresponde ao certificado do admin.",
                            "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    LogDAO dao = new LogDAO();
                    StringBuilder sb = new StringBuilder();
                    for (String line : dao.listChronological()) {
                        sb.append(line).append("\n");
                    }
                    textArea.setText(sb.toString());
                    JOptionPane.showMessageDialog(frame, "Logs carregados com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
            });

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static String getAdminCertificate() {
        String sql = "SELECT certificado_digital FROM Chaveiro WHERE kid = (SELECT kid FROM Usuarios WHERE uid = 1)";
        try (java.sql.Connection c = java.sql.DriverManager.getConnection("jdbc:sqlite:cofre.db");
             java.sql.Statement st = c.createStatement();
             java.sql.ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getString("certificado_digital");
        } catch (Exception e) {
            System.err.println("Erro ao obter certificado do administrador: " + e.getMessage());
        }
        return null;
    }
}
