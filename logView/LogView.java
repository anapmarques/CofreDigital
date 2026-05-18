// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

import br.com.cofredigital.crypto.SignatureUtil;
import br.com.cofredigital.database.LogDAO;

public class LogView {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("LogView - Visualizador de Registros");
            frame.getContentPane().setBackground(BG);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(820, 520));
            frame.setLayout(new BorderLayout(0, 0));

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(CARD_BG);
            topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(14, 22, 14, 22)
            ));

            JPanel leftTop = new JPanel();
            leftTop.setBackground(CARD_BG);
            leftTop.setLayout(new BoxLayout(leftTop, BoxLayout.Y_AXIS));
            JLabel titleLabel = new JLabel("Visualizador de Logs");
            titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 17f));
            titleLabel.setForeground(TEXT);
            leftTop.add(titleLabel);
            JLabel subtitleLabel = new JLabel("Autentique-se com a chave privada do administrador para visualizar os registros");
            subtitleLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
            subtitleLabel.setForeground(MUTED);
            leftTop.add(subtitleLabel);
            topPanel.add(leftTop, BorderLayout.WEST);

            JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
            rightTop.setBackground(CARD_BG);
            JButton btnCarregar = new JButton("Carregar Logs");
            btnCarregar.setFont(UIManager.getFont("Button.font"));
            btnCarregar.setBackground(PRIMARY);
            btnCarregar.setForeground(Color.WHITE);
            btnCarregar.setFocusPainted(false);
            btnCarregar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btnCarregar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
            rightTop.add(btnCarregar);
            topPanel.add(rightTop, BorderLayout.EAST);

            frame.add(topPanel, BorderLayout.NORTH);

            DefaultTableModel model = new DefaultTableModel(new String[]{"Timestamp", "Codigo", "Mensagem"}, 0) {
                public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable table = new JTable(model);
            table.setRowHeight(32);
            table.setFont(UIManager.getFont("Table.font").deriveFont(13f));
            table.setShowVerticalLines(false);
            table.setShowHorizontalLines(true);
            table.setGridColor(new Color(241, 245, 249));
            table.setIntercellSpacing(new Dimension(0, 1));
            table.setSelectionBackground(new Color(238, 242, 255));
            table.setSelectionForeground(TEXT);
            table.setFillsViewportHeight(true);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
            table.getColumnModel().getColumn(0).setPreferredWidth(160);
            table.getColumnModel().getColumn(0).setMaxWidth(180);
            table.getColumnModel().getColumn(1).setPreferredWidth(70);
            table.getColumnModel().getColumn(1).setMaxWidth(80);
            table.getColumnModel().getColumn(2).setPreferredWidth(500);

            JTableHeader header = table.getTableHeader();
            header.setFont(UIManager.getFont("TableHeader.font"));
            header.setBackground(new Color(248, 250, 252));
            header.setForeground(TEXT);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.getViewport().setBackground(Color.WHITE);
            scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            frame.add(scrollPane, BorderLayout.CENTER);

            JPanel statusPanel = new JPanel(new BorderLayout());
            statusPanel.setBackground(CARD_BG);
            statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(8, 22, 8, 22)
            ));
            JLabel statusLabel = new JLabel("Pronto. Clique em \"Carregar Logs\" para iniciar.");
            statusLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
            statusLabel.setForeground(MUTED);
            statusPanel.add(statusLabel, BorderLayout.WEST);
            frame.add(statusPanel, BorderLayout.SOUTH);

            btnCarregar.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Selecione a chave privada do administrador");
                if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;
                String keyPath = chooser.getSelectedFile().getAbsolutePath();

                JPasswordField passField = new JPasswordField(20);
                passField.setFont(UIManager.getFont("PasswordField.font"));
                passField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                JPanel passPanel = new JPanel(new GridBagLayout());
                passPanel.setBackground(Color.WHITE);
                passPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(2, 0, 6, 0);
                JLabel passLabel = new JLabel("Informe a frase secreta da chave privada:");
                passLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
                passLabel.setForeground(TEXT);
                passPanel.add(passLabel, gbc);
                gbc.gridy = 1;
                passPanel.add(passField, gbc);

                if (JOptionPane.showConfirmDialog(frame, passPanel, "Autenticacao",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
                String passphrase = new String(passField.getPassword());
                if (passphrase.isEmpty()) return;

                try {
                    statusLabel.setText("Autenticando...");
                    statusLabel.setForeground(MUTED);

                    byte[] privEncryptedBytes = Files.readAllBytes(Paths.get(keyPath));
                    PrivateKey privateKey = SignatureUtil.decryptPrivateKey(privEncryptedBytes, passphrase);

                    String adminCertPem = getAdminCertificate();
                    if (adminCertPem == null) {
                        JOptionPane.showMessageDialog(frame,
                            "Certificado do administrador nao encontrado no banco.", "Erro", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("Erro: certificado nao encontrado.");
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
                        statusLabel.setText("Erro: autenticacao falhou.");
                        return;
                    }

                    LogDAO dao = new LogDAO();
                    List<String> lines = dao.listChronological();

                    model.setRowCount(0);
                    for (String line : lines) {
                        String[] parts = line.split(" \\| ", 2);
                        if (parts.length == 2) {
                            String ts = parts[0];
                            String rest = parts[1];
                            int firstSpace = rest.indexOf(' ');
                            String codigo = firstSpace > 0 ? rest.substring(0, firstSpace) : rest;
                            String mensagem = firstSpace > 0 ? rest.substring(firstSpace + 1) : "";
                            model.addRow(new Object[]{ts, codigo, mensagem});
                        } else {
                            model.addRow(new Object[]{line, "", ""});
                        }
                    }

                    statusLabel.setText(lines.size() + " registro(s) carregados com sucesso.");
                    statusLabel.setForeground(new Color(16, 185, 129));

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Erro ao carregar logs.");
                    statusLabel.setForeground(new Color(239, 68, 68));
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
