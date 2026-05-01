package util;

import java.io.Console;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utilitário simples para leitura de entrada no terminal.
 */
public class Input {
    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static String readLine() {
        try { return in.readLine(); } catch (Exception e) { return null; }
    }

    public static String readLineMasked() {
        Console c = System.console();
        if (c != null) {
            char[] chars = c.readPassword();
            return chars == null ? null : new String(chars);
        }
        return readLine();
    }
}
