package authserver.service;

import authserver.config.AuthServerProperties;
import jakarta.transaction.Transactional;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
public class ClientService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    
    private final RegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final AuthServerProperties authServerProperties;
    private final SecureRandom random = new SecureRandom();

    public ClientService(RegisteredClientRepository registeredClientRepository,
                         PasswordEncoder passwordEncoder,
                         JdbcTemplate jdbcTemplate,
                         AuthServerProperties authServerProperties) {
        this.registeredClientRepository = registeredClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.authServerProperties = authServerProperties;
    }

    private String genSecret() {
        int secretLength = authServerProperties.getOauth().getSecretLength();
        byte[] b = new byte[secretLength];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Transactional
    public CreatedClient createClient(String clientId, String rawSecret, String clientName, Set<String> scopes, Long accessTtlSec) {
        logger.info("Creating new OAuth client with name: {}", clientName);
        
        String id = UUID.randomUUID().toString();
        String finalClientId = (clientId == null || clientId.isBlank()) ? UUID.randomUUID().toString() : clientId;
        String secretRaw = (rawSecret == null || rawSecret.isBlank()) ? genSecret() : rawSecret;
        String encoded = passwordEncoder.encode(secretRaw);

        var builder = RegisteredClient.withId(id)
                .clientId(finalClientId)
                .clientSecret(encoded)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .clientName(clientName);

        // Handle scopes with configuration defaults
        if (scopes != null && !scopes.isEmpty()) {
            if (scopes.size() > authServerProperties.getOauth().getMaxScopes()) {
                throw new IllegalArgumentException("Too many scopes. Maximum allowed: " + authServerProperties.getOauth().getMaxScopes());
            }
            scopes.forEach(builder::scope);
        } else {
            builder.scope(authServerProperties.getOauth().getDefaultScope());
        }

        // Handle TTL with configuration defaults
        long ttl = (accessTtlSec != null && accessTtlSec > 0) ? accessTtlSec : authServerProperties.getOauth().getDefaultAccessTokenTtl();
        builder.tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofSeconds(ttl)).build());

        RegisteredClient rc = builder.build();
        registeredClientRepository.save(rc);
        
        logger.info("Successfully created OAuth client with ID: {}", finalClientId);
        return new CreatedClient(rc, secretRaw);
    }

    public RegisteredClient findByClientId(String clientId) {
        return registeredClientRepository.findByClientId(clientId);
    }

    @Transactional
    public void deleteByClientId(String clientId) {
        // delete from oauth2_registered_client; JdbcRegisteredClientRepository does not expose deleteByClientId in interface
        jdbcTemplate.update("DELETE FROM oauth2_registered_client WHERE client_id = ?", clientId);
        // also remove possible oauth2_authorization entries
        jdbcTemplate.update("DELETE FROM oauth2_authorization WHERE registered_client_id IN (SELECT id FROM oauth2_registered_client WHERE client_id = ?)", clientId);
    }

    @Getter
    public static class CreatedClient {
        private final RegisteredClient registeredClient;
        private final String rawSecret;
        public CreatedClient(RegisteredClient rc, String rawSecret) {
            this.registeredClient = rc;
            this.rawSecret = rawSecret;
        }
    }
}