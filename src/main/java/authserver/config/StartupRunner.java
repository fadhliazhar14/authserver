package authserver.config;

import authserver.service.JwkKeyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    private final JwkKeyService jwkKeyService;
    private final AuthServerProperties authServerProperties;

    public StartupRunner(JwkKeyService jwkKeyService, AuthServerProperties authServerProperties) {
        this.jwkKeyService = jwkKeyService;
        this.authServerProperties = authServerProperties;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing authorization server...");
        
        try {
            jwkKeyService.getActiveRsaKey();
            logger.info("Active RSA key found and loaded successfully");
        } catch (Exception ex) {
            logger.warn("No active RSA key found, generating new key: {}", ex.getMessage());
            int keySize = authServerProperties.getOauth().getDefaultKeySize();
            jwkKeyService.generateAndSaveRsaKey(keySize);
            logger.info("New RSA key generated successfully with size: {}", keySize);
        }
        
        logger.info("Authorization server initialization completed");
    }
}
