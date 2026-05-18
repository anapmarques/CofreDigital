// Ana Luiza Pinto Marques - 2211960
// Marcos Turo Fernandes Junior - 2211712
package br.com.cofredigital.ui;

import br.com.cofredigital.auth.VirtualKeyboard;
import br.com.cofredigital.util.BcryptUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VirtualKeyboardPanel extends JPanel {
    private static final Color BG = new Color(248, 250, 252);
    private static final Color BTN_BG = new Color(79, 70, 229);
    private static final Color CONFIRM_BG = new Color(16, 185, 129);

    private final VirtualKeyboard kb;
    private final List<int[]> mappings;
    private final JPasswordField passwordField;
    private final JDialog dialog;
    private final String storedHash;
    private String foundPassword = null;

    public VirtualKeyboardPanel(VirtualKeyboard kb, List<int[]> mappings,
                                 JPasswordField passwordField, JDialog dialog, String storedHash) {
        this.kb = kb;
        this.mappings = mappings;
        this.passwordField = passwordField;
        this.dialog = dialog;
        this.storedHash = storedHash;
        setBackground(BG);
        setLayout(new BorderLayout(12, 12));
        rebuild();
    }

    private void rebuild() {
        removeAll();

        JPanel buttonsPanel = new JPanel(new GridLayout(1, VirtualKeyboard.getTotalButtons(), 10, 0));
        buttonsPanel.setBackground(BG);
        for (int i = 0; i < VirtualKeyboard.getTotalButtons(); i++) {
            int[] digits = kb.getButtonDigits(i + 1);
            JButton btn = new JButton(digits[0] + " " + digits[1]);
            btn.setFont(UIManager.getFont("Button.font").deriveFont(Font.BOLD, 20f));
            btn.setPreferredSize(new Dimension(80, 58));
            btn.setBackground(BTN_BG);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            btn.addActionListener(e -> onButtonClick(digits));
            buttonsPanel.add(btn);
        }
        add(buttonsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 6));
        bottomPanel.setBackground(BG);
        JButton btnConfirm = new JButton("Confirmar Senha");
        btnConfirm.setFont(UIManager.getFont("Button.font"));
        btnConfirm.setBackground(CONFIRM_BG);
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnConfirm.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btnConfirm.addActionListener(e -> onConfirm());
        bottomPanel.add(btnConfirm);
        JButton btnLimpar = new JButton("Limpar Senha");
        btnLimpar.setFont(UIManager.getFont("Label.font"));
        btnLimpar.setForeground(new Color(30, 41, 59));
        btnLimpar.setBackground(new Color(241, 245, 249));
        btnLimpar.setFocusPainted(false);
        btnLimpar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpar.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btnLimpar.addActionListener(e -> limparSenha());
        bottomPanel.add(btnLimpar);
        add(bottomPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void onButtonClick(int[] digits) {
        mappings.add(new int[]{digits[0], digits[1]});
        passwordField.setText(new String(new char[mappings.size()]).replace('\0', '*'));
        kb.shuffle();
        rebuild();
    }

    private void limparSenha() {
        mappings.clear();
        passwordField.setText("");
        kb.shuffle();
        rebuild();
    }

    private void onConfirm() {
//        if (mappings.size() < 8) {
//            JOptionPane.showMessageDialog(dialog,
//                "A senha está incorreta.",
//                "Aviso", JOptionPane.WARNING_MESSAGE);
//            return;
//        }

        String result = tryCombinations(mappings, 0, new StringBuilder(), storedHash, mappings.size());
        foundPassword = result.isEmpty() ? "" : result;
        dialog.dispose();
    }

    public static String showPasswordDialog(Component parent, String storedHash) {
        JFrame owner = null;
        if (parent instanceof JFrame) {
            owner = (JFrame) parent;
        } else if (parent != null) {
            Window w = SwingUtilities.getWindowAncestor(parent);
            if (w instanceof JFrame) owner = (JFrame) w;
        }

        JDialog dialog = new JDialog(owner, "Teclado Virtual - Digite sua senha", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setBackground(BG);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(14, 14));
        mainPanel.setBackground(BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 22, 18, 22));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(BG);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Informe sua senha");
        titleLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 15f));
        titleLabel.setForeground(new Color(30, 41, 59));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Clique nos botoes para digitar cada par de digitos");
        subtitleLabel.setFont(UIManager.getFont("Label.font").deriveFont(11f));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 10, 0));
        topPanel.add(subtitleLabel);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(UIManager.getFont("PasswordField.font").deriveFont(Font.BOLD, 22f));
        passwordField.setEditable(false);
        passwordField.setHorizontalAlignment(SwingConstants.CENTER);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        mainPanel.add(passwordField, BorderLayout.NORTH);

        List<int[]> mappings = new ArrayList<>();
        VirtualKeyboard kb = new VirtualKeyboard();
        VirtualKeyboardPanel panel = new VirtualKeyboardPanel(kb, mappings, passwordField, dialog, storedHash);

        mainPanel.add(panel, BorderLayout.CENTER);
        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);

        return panel.foundPassword;
    }

    private static String tryCombinations(List<int[]> mappings, int index,
                                           StringBuilder current, String storedHash, int maxIndex) {
        if (index == maxIndex) {
            String candidate = current.toString();
            if (BcryptUtil.verify(candidate, storedHash)) {
                return candidate;
            }
            return "";
        }
        int[] pair = mappings.get(index);
        current.append(pair[0]);
        String r = tryCombinations(mappings, index + 1, current, storedHash, maxIndex);
        if (!r.isEmpty()) return r;
        current.deleteCharAt(current.length() - 1);

        current.append(pair[1]);
        r = tryCombinations(mappings, index + 1, current, storedHash, maxIndex);
        if (!r.isEmpty()) return r;
        current.deleteCharAt(current.length() - 1);

        return "";
    }
}
