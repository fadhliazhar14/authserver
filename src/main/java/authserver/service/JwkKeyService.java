package authserver.service;

import authserver.config.AuthServerProperties;
import authserver.entity.JwkKey;
import authserver.repo.JwkKeyRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.RSAKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Service
public class JwkKeyService {

    private static final Logger logger = LoggerFactory.getLogger(JwkKeyService.class);
    
    private final JwkKeyRepository repo;
    private final AuthServerProperties authServerProperties;

    public JwkKeyService(JwkKeyRepository repo, AuthServerProperties authServerProperties) {
        this.repo = repo;
        this.authServerProperties = authServerProperties;
    }

    @Transactional(readOnly = true)
    public RSAKey getActiveRsaKey() {
        JwkKey jk = repo.findFirstByIsActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active JWK key found"));
        try {
            RSAPublicKey pub = PemUtils.readPublicKeyFromPem(jk.getPublicKeyPem());
            RSAPrivateKey priv = PemUtils.readPrivateKeyFromPem(jk.getPrivateKeyPem());
            return new RSAKey.Builder(pub)
                    .privateKey(priv)
                    .keyID(jk.getKid())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RSAKey from DB", e);
        }
    }

    @Transactional
    public RSAKey getOrCreateActiveRsaKey(int keySize) {
        return repo.findFirstByIsActiveTrue()
                .map(this::convertToRSAKey)
                .orElseGet(() -> {
                    try {
                        int finalKeySize = keySize > 0 ? keySize : authServerProperties.getOauth().getDefaultKeySize();
                        return convertToRSAKey(generateAndSaveRsaKey(finalKeySize));
                    } catch (NoSuchAlgorithmException e) {
                        logger.error("Failed to generate RSA key", e);
                        throw new RuntimeException("Failed to generate RSA key", e);
                    }
                });
    }

    @Transactional
    public JwkKey generateAndSaveRsaKey(int keySize) throws NoSuchAlgorithmException {
        logger.info("Generating new RSA key with size: {}", keySize);
        
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keySize);
        KeyPair kp = gen.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

        String kid = UUID.randomUUID().toString();
        String pubPem = PemUtils.encodePublicKeyToPem(pub);
        String privPem = PemUtils.encodePrivateKeyToPem(priv);

        // deactivate existing active key
        repo.findFirstByIsActiveTrue().ifPresent(existing -> {
            logger.info("Deactivating existing key with ID: {}", existing.getKid());
            existing.setIsActive(false);
            repo.save(existing);
        });

        JwkKey newKey = new JwkKey();
        newKey.setKid(kid);
        newKey.setPublicKeyPem(pubPem);
        newKey.setPrivateKeyPem(privPem);
        newKey.setAlgorithm(authServerProperties.getOauth().getDefaultAlgorithm());
        newKey.setIsActive(true);
        
        JwkKey savedKey = repo.save(newKey);
        logger.info("Successfully generated and saved new RSA key with ID: {}", kid);
        
        return savedKey;
    }

    private RSAKey convertToRSAKey(JwkKey jk) {
        try {
            RSAPublicKey pub = PemUtils.readPublicKeyFromPem(jk.getPublicKeyPem());
            RSAPrivateKey priv = PemUtils.readPrivateKeyFromPem(jk.getPrivateKeyPem());
            return new RSAKey.Builder(pub)
                    .privateKey(priv)
                    .keyID(jk.getKid())
                    .algorithm(JWSAlgorithm.RS256)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RSAKey from DB", e);
        }
    }
}
