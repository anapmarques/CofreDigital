package crypto;

import java.security.*;

/**
 * Utilitário para assinatura RSA (SHA1withRSA) conforme requisitos.
 */
public class SignatureUtil {
    public static byte[] sign(byte[] privateKeyBytes, byte[] data) throws Exception {
        // privateKeyBytes must be a PKCS#8 encoded private key
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey pk = kf.generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes));
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(pk);
        sig.update(data);
        return sig.sign();
    }

    public static boolean verify(byte[] certBytes, byte[] data, byte[] signatureBytes) throws Exception {
        java.security.cert.CertificateFactory cf = java.security.cert.CertificateFactory.getInstance("X.509");
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(certBytes);
        java.security.cert.Certificate cert = cf.generateCertificate(in);
        PublicKey pub = cert.getPublicKey();
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(pub);
        sig.update(data);
        return sig.verify(signatureBytes);
    }
}
