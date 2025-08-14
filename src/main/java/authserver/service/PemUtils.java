package authserver.service;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PemUtils {

    public static String encodePublicKeyToPem(RSAPublicKey publicKey) {
        String b64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + chunk(b64) + "\n-----END PUBLIC KEY-----";
    }

    public static String encodePrivateKeyToPem(RSAPrivateKey privateKey) {
        String b64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + chunk(b64) + "\n-----END PRIVATE KEY-----";
    }

    private static String chunk(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int end = Math.min(i + 64, s.length());
            sb.append(s, i, end).append("\n");
            i = end;
        }
        return sb.toString().trim();
    }

    public static RSAPublicKey readPublicKeyFromPem(String pem) throws Exception {
        String clean = pem.replaceAll("-----\\w+ PUBLIC KEY-----", "").replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    public static RSAPrivateKey readPrivateKeyFromPem(String pem) throws Exception {
        String clean = pem.replaceAll("-----\\w+ PRIVATE KEY-----", "").replaceAll("\\s", "");
        byte[] der = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }
}
