package util;

public class Base32 {
    private static final String ALPH = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    public static String encode(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int i = 0, index = 0;
        int currByte, nextByte;
        while (i < data.length) {
            currByte = data[i] & 0xff;
            if (index > 3) {
                if ((i + 1) < data.length) nextByte = data[i + 1] & 0xff; else nextByte = 0;
                int digit = currByte & (0xff >> index);
                index = (index + 5) % 8;
                digit = (digit << index) | (nextByte >> (8 - index));
                i++;
                sb.append(ALPH.charAt(digit));
            } else {
                int digit = (currByte >> (8 - (index + 5))) & 0x1f;
                index = (index + 5) % 8;
                if (index == 0) i++;
                sb.append(ALPH.charAt(digit));
            }
        }
        return sb.toString();
    }

    public static byte[] decode(String s) {
        s = s.replace("=", "").toUpperCase();
        int len = s.length();
        int byteCount = len * 5 / 8;
        byte[] result = new byte[byteCount];
        int buffer = 0, bitsLeft = 0, index = 0;
        for (char c : s.toCharArray()) {
            int val = ALPH.indexOf(c);
            if (val < 0) continue;
            buffer <<= 5;
            buffer |= val & 0x1f;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                result[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return result;
    }
}
