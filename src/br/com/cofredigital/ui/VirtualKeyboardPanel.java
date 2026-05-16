package br.com.cofredigital.ui;

import br.com.cofredigital.auth.VirtualKeyboard;
import br.com.cofredigital.util.BcryptUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VirtualKeyboardPanel extends JPanel {
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
        setLayout(new BorderLayout(10, 10));
        rebuild();
    }

    private void rebuild() {
        removeAll();

        JPanel buttonsPanel = new JPanel(new GridLayout(1, VirtualKeyboard.getTotalButtons(), 8, 0));
        for (int i = 0; i < VirtualKeyboard.getTotalButtons(); i++) {
            int[] digits = kb.getButtonDigits(i + 1);
            JButton btn = new JButton(digits[0] + " " + digits[1]);
            btn.setFont(new Font("Monospaced", Font.BOLD, 18));
            btn.setPreferredSize(new Dimension(70, 50));
            btn.addActionListener(e -> onButtonClick(digits));
            buttonsPanel.add(btn);
        }
        add(buttonsPanel, BorderLayout.CENTER);

        JButton btnConfirm = new JButton("Confirmar Senha");
        btnConfirm.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnConfirm.addActionListener(e -> onConfirm());
        add(btnConfirm, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void onButtonClick(int[] digits) {
        mappings.add(new int[]{digits[0], digits[1]});
        passwordField.setText(new String(new char[mappings.size()]).replace('\0', '*'));
        kb.shuffle();
        rebuild();
    }

    private void onConfirm() {
        if (mappings.size() < 8) {
            JOptionPane.showMessageDialog(dialog,
                "A senha deve ter no mínimo 8 dígitos.",
                "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

        JDialog dialog = new JDialog(owner, "Teclado Virtual", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Monospaced", Font.BOLD, 18));
        passwordField.setEditable(false);
        passwordField.setHorizontalAlignment(SwingConstants.CENTER);
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
