package authserver.controller;

import authserver.config.AuthServerProperties;
import authserver.entity.JwkKey;
import authserver.repo.JwkKeyRepository;
import authserver.service.JwkKeyService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/keys")
public class KeyController {

    private static final Logger logger = LoggerFactory.getLogger(KeyController.class);

    private final JwkKeyService jwkKeyService;
    private final JwkKeyRepository jwkKeyRepository;
    private final AuthServerProperties authServerProperties;

    public KeyController(JwkKeyService jwkKeyService, JwkKeyRepository jwkKeyRepository, AuthServerProperties authServerProperties) {
        this.jwkKeyService = jwkKeyService;
        this.jwkKeyRepository = jwkKeyRepository;
        this.authServerProperties = authServerProperties;
    }

    @PostMapping("/rotate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rotate(
            @RequestParam(required = false)
            @Min(value = 1024, message = "Key size must be at least 1024")
            @Max(value = 4096, message = "Key size cannot exceed 4096")
            Integer keySize) {
        
        logger.info("Admin request to rotate JWK key with size: {}", keySize);
        
        try {
            int finalKeySize = keySize != null ? keySize : authServerProperties.getOauth().getDefaultKeySize();
            JwkKey newKey = jwkKeyService.generateAndSaveRsaKey(finalKeySize);
            
            Map<String, Object> response = Map.of(
                    "kid", newKey.getKid(),
                    "createdAt", newKey.getCreatedAt(),
                    "algorithm", newKey.getAlgorithm(),
                    "keySize", finalKeySize
            );
            
            logger.info("Successfully rotated JWK key: {}", newKey.getKid());
            return ResponseEntity.ok(response);
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to rotate JWK key: {}", e.getMessage());
            throw new RuntimeException("Failed to generate RSA key: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> list() {
        logger.debug("Admin request to list all JWK keys");
        
        List<Map<String, Object>> keys = jwkKeyRepository.findAll().stream()
                .map(k -> Map.<String, Object>of(
                        "kid", k.getKid(),
                        "publicKeyPem", k.getPublicKeyPem(),
                        "createdAt", k.getCreatedAt(),
                        "isActive", k.getIsActive(),
                        "algorithm", k.getAlgorithm()
                )).collect(Collectors.toList());
        
        logger.debug("Returning {} JWK keys", keys.size());
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveKey() {
        logger.debug("Request for active JWK key");
        
        try {
            JwkKey activeKey = jwkKeyRepository.findFirstByIsActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No active JWK key found"));
            
            Map<String, Object> response = Map.of(
                    "kid", activeKey.getKid(),
                    "algorithm", activeKey.getAlgorithm(),
                    "createdAt", activeKey.getCreatedAt()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to get active JWK key: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve active key");
        }
    }
}
