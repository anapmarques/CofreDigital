package crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

/**
 * Implementacao TOTP (RFC 6238) usando HMAC-SHA1 e Base32 (RFC4648).
 */
public class TOTP {
	private byte[] key = null;
	private long timeStepInSeconds = 30;

	// Construtor: recebe chave em Base32 e o intervalo (em segundos)
	public TOTP(String base32EncodedSecret, long timeStepInSeconds) throws Exception {
		if (base32EncodedSecret == null) throw new IllegalArgumentException("secret is null");
		this.timeStepInSeconds = timeStepInSeconds <= 0 ? 30 : timeStepInSeconds;
		// decodifica Base32 usando util.Base32
		this.key = util.Base32.decode(base32EncodedSecret);
		if (this.key == null || this.key.length == 0) throw new IllegalArgumentException("invalid base32 secret");
	}

	// Transforma o hash HMAC-SHA1 no codigo TOTP de 6 digitos (RFC4226 truncation)
	private String getTOTPCodeFromHash(byte[] hash) {
		int offset = hash[hash.length - 1] & 0x0f;
		int binary = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16) | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
		int otp = binary % 1000000;
		String s = Integer.toString(otp);
		while (s.length() < 6) s = "0" + s;
		return s;
	}

	// Calcula HMAC-SHA1 do counter (8 bytes big-endian)
	private byte[] HMAC_SHA1(byte[] counter, byte[] keyByteArray) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec keySpec = new SecretKeySpec(keyByteArray, "HmacSHA1");
		mac.init(keySpec);
		return mac.doFinal(counter);
	}

	// Gera o TOTP para o intervalo de tempo (timeInterval = counter)
	private String TOTPCode(long timeInterval) {
		try {
			long counter = timeInterval;
			byte[] counterBytes = new byte[8];
			for (int i = 7; i >= 0; i--) {
				counterBytes[i] = (byte) (counter & 0xff);
				counter >>= 8;
			}
			byte[] hash = HMAC_SHA1(counterBytes, this.key);
			return getTOTPCodeFromHash(hash);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Gera o codigo atual (para uso diagnostico)
	public String generateCode() {
		long t = new Date().getTime() / 1000L;
		long interval = t / this.timeStepInSeconds;
		return TOTPCode(interval);
	}

	// Valida um codigo TOTP considerando janela de -1,0,+1 intervalos
	public boolean validateCode(String inputTOTP) {
		if (inputTOTP == null) return false;
		inputTOTP = inputTOTP.trim();
		long t = new Date().getTime() / 1000L;
		long interval = t / this.timeStepInSeconds;
		for (long i = -1; i <= 1; i++) {
			String c = TOTPCode(interval + i);
			if (c.equals(inputTOTP)) return true;
		}
		return false;
	}
}