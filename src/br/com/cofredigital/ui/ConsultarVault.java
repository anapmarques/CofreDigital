// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import br.com.cofredigital.model.User;
import br.com.cofredigital.service.UserService;
import br.com.cofredigital.service.VaultService;
import br.com.cofredigital.service.VaultService.VaultFileEntry;
import br.com.cofredigital.service.VaultService.VaultResult;
import br.com.cofredigital.database.LogDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsultarVault extends JFrame {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color PRIMARY = new Color(79, 70, 229);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);

    private User usuario;
    private JTextField folderField;
    private JPasswordField phraseField;

    public ConsultarVault(User usuario) {
        this.usuario = usuario;

        setTitle("Cofre Digital - Consultar Arquivos Secretos");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(560, 480));
        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        JPanel card = criarCard();

        JLabel titleLabel = new JLabel("Consultar Arquivos Secretos");
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
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        card.add(userInfo);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(340, 1));
        sep.setForeground(BORDER);
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(sep);
        card.add(Box.createVerticalStrut(12));

        JLabel statsLabel = new JLabel("Total de consultas: " + usuario.getTotalConsultas());
        statsLabel.setFont(UIManager.getFont("Label.font"));
        statsLabel.setForeground(TEXT);
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        card.add(statsLabel);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel folderLabel = new JLabel("Caminho da pasta:");
        folderLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        folderLabel.setForeground(TEXT);
        formPanel.add(folderLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        folderField = new JTextField(25);
        folderField.setFont(UIManager.getFont("TextField.font"));
        folderField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        JPanel folderRow = new JPanel(new BorderLayout(6, 0));
        folderRow.setBackground(CARD_BG);
        folderRow.add(folderField, BorderLayout.CENTER);
        JButton btnFolder = new JButton("\uD83D\uDCC1");
        btnFolder.setFont(UIManager.getFont("Button.font"));
        btnFolder.setBackground(new Color(241, 245, 249));
        btnFolder.setFocusPainted(false);
        btnFolder.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        btnFolder.addActionListener(ev -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(ConsultarVault.this) == JFileChooser.APPROVE_OPTION) {
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        folderRow.add(btnFolder, BorderLayout.EAST);
        formPanel.add(folderRow, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 4, 6);
        JLabel phraseLabel = new JLabel("Frase secreta:");
        phraseLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 13f));
        phraseLabel.setForeground(TEXT);
        formPanel.add(phraseLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.insets = new Insets(4, 0, 4, 0);
        phraseField = new JPasswordField(25);
        phraseField.setFont(UIManager.getFont("PasswordField.font"));
        phraseField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        formPanel.add(phraseField, gbc);

        card.add(formPanel);
        card.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setBackground(CARD_BG);

        JButton btnListar = criarBotao("Listar Arquivos", PRIMARY);
        btnListar.addActionListener(e -> {
            new LogDAO().addLog(5003, usuario.getEmail(), now());
            listarArquivos();
        });
        btnRow.add(btnListar);

        JButton btnVoltar = criarBotao("Voltar", new Color(241, 245, 249));
        btnVoltar.setForeground(TEXT);
        btnVoltar.addActionListener(e -> {
            dispose();
            new Principal(usuario).setVisible(true);
        });
        btnRow.add(btnVoltar);

        card.add(btnRow);

        GridBagConstraints gbcRoot = new GridBagConstraints();
        gbcRoot.gridx = 0;
        gbcRoot.gridy = 0;
        gbcRoot.insets = new Insets(20, 20, 20, 20);
        add(card, gbcRoot);

        pack();
        setLocationRelativeTo(null);

        new LogDAO().addLog(7001, usuario.getEmail(), now());
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

    private void listarArquivos() {
        String folderPath = folderField.getText().trim();
        String phrase = new String(phraseField.getPassword());

        if (folderPath.isEmpty() || phrase.isEmpty()) {
            new LogDAO().addLog(7004, usuario.getEmail(), now());
            JOptionPane.showMessageDialog(ConsultarVault.this, "Preencha todos os campos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new LogDAO().addLog(7003, usuario.getEmail(), now());

        String adminPassphrase = UserService.getAdminPassphrase();

        VaultService vs = new VaultService();
        VaultResult vr = vs.listVaultFiles(usuario, folderPath, phrase, adminPassphrase);

        if (!vr.isSuccess()) {
            JOptionPane.showMessageDialog(ConsultarVault.this, vr.error, "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (vr.files.isEmpty()) {
            JOptionPane.showMessageDialog(ConsultarVault.this, "Nenhum arquivo encontrado para o usuario ou grupo.",
                "Resultado", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] colunas = {"Codigo", "Nome", "Dono", "Grupo"};
        Object[][] dados = new Object[vr.files.size()][4];
        for (int i = 0; i < vr.files.size(); i++) {
            VaultFileEntry f = vr.files.get(i);
            dados[i] = new Object[]{f.codigo, f.nome, f.dono, f.grupo};
        }

        DefaultTableModel model = new DefaultTableModel(dados, colunas) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(36);
        table.setFont(UIManager.getFont("Table.font").deriveFont(13f));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(238, 242, 255));
        table.setSelectionForeground(TEXT);
        table.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(UIManager.getFont("TableHeader.font"));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(TEXT);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(600, 260));
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));

        VaultFileEntry[] selectedRef = new VaultFileEntry[1];

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        selectedRef[0] = vr.files.get(row);
                        Window w = SwingUtilities.getWindowAncestor(table);
                        if (w != null) w.dispose();
                    }
                }
            }
        });

        JDialog dialog = new JDialog(ConsultarVault.this, "Arquivos Disponiveis", true);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout(0, 0));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));
        JLabel dialogTitle = new JLabel("Arquivos encontrados no vault");
        dialogTitle.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 15f));
        dialogTitle.setForeground(TEXT);
        headerPanel.add(dialogTitle, BorderLayout.WEST);
        JLabel hintLabel = new JLabel("Duplo clique para selecionar");
        hintLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
        hintLabel.setForeground(MUTED);
        headerPanel.add(hintLabel, BorderLayout.EAST);
        dialog.add(headerPanel, BorderLayout.NORTH);

        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 14, 0));
        JButton btnVoltar = new JButton("Voltar");
        btnVoltar.setFont(UIManager.getFont("Label.font"));
        btnVoltar.setForeground(TEXT);
        btnVoltar.setBackground(new Color(241, 245, 249));
        btnVoltar.setFocusPainted(false);
        btnVoltar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnVoltar.addActionListener(evt -> {
            new LogDAO().addLog(7002, usuario.getEmail(), now());
            dialog.dispose();
        });
        btnPanel.add(btnVoltar);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(ConsultarVault.this);
        dialog.setVisible(true);

        VaultFileEntry selected = selectedRef[0];
        if (selected == null) return;
        new LogDAO().addLog(7010, usuario.getEmail(), now(), selected.nome);

        if (!selected.dono.equals(usuario.getEmail())) {
            new LogDAO().addLog(7012, usuario.getEmail(), now(), selected.nome);
            JOptionPane.showMessageDialog(ConsultarVault.this, "Acesso negado: voce nao e o dono do arquivo.",
                "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new LogDAO().addLog(7011, usuario.getEmail(), now(), selected.nome);

        int confirm = JOptionPane.showConfirmDialog(ConsultarVault.this,
            "Deseja decriptar o arquivo \"" + selected.nome + "\"?",
            "Confirmacao", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String error = vs.decryptFile(usuario, folderPath, selected.codigo, selected.nome, phrase, adminPassphrase);
        if (error != null) {
            JOptionPane.showMessageDialog(ConsultarVault.this, "Erro ao decriptar: " + error, "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(ConsultarVault.this,
                "Arquivo decriptado com sucesso: " + selected.nome + "\nSalvo em: " + selected.nome,
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
