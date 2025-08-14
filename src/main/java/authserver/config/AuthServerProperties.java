package authserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "app")
public class AuthServerProperties {
    
    @NotBlank(message = "Issuer URL is required")
    private String issuer = "http://localhost:8080";
    
    private Admin admin = new Admin();
    private OAuth oauth = new OAuth();
    private Security security = new Security();
    
    @Getter
    @Setter
    public static class Admin {
        @NotBlank(message = "Admin API key is required")
        private String apiKey = "NjFuSO0Z8k93/x6gtnlMazaecyp4IJHeOIec3YCA/Xw=";
    }
    
    @Getter
    @Setter
    public static class OAuth {
        
        @Min(value = 1024, message = "Key size must be at least 1024")
        @Max(value = 4096, message = "Key size cannot exceed 4096")
        private int defaultKeySize = 2048;
        
        @NotBlank(message = "Default algorithm is required")
        private String defaultAlgorithm = "RS256";
        
        @NotBlank(message = "Default scope is required")
        private String defaultScope = "read";
        
        @Min(value = 16, message = "Secret length must be at least 16 bytes")
        @Max(value = 64, message = "Secret length cannot exceed 64 bytes")
        private int secretLength = 32;
        
        @Min(value = 60, message = "Default access token TTL must be at least 60 seconds")
        @Max(value = 86400, message = "Default access token TTL cannot exceed 24 hours")
        private long defaultAccessTokenTtl = 3600; // 1 hour
        
        @Min(value = 1, message = "Max scopes must be at least 1")
        @Max(value = 20, message = "Max scopes cannot exceed 20")
        private int maxScopes = 10;
        
        @Size(min = 3, max = 100, message = "Client ID length must be between 3 and 100")
        private ClientIdConfig clientId = new ClientIdConfig();
    }
    
    @Getter
    @Setter
    public static class ClientIdConfig {
        private int minLength = 3;
        private int maxLength = 100;
        private String pattern = "^[a-zA-Z0-9_-]+$";
    }
    
    @Getter
    @Setter
    public static class Security {
        
        @Min(value = 1, message = "Rate limit requests must be at least 1")
        @Max(value = 1000, message = "Rate limit requests cannot exceed 1000")
        private int rateLimitRequests = 5;
        
        @Min(value = 60, message = "Rate limit window must be at least 60 seconds")
        @Max(value = 86400, message = "Rate limit window cannot exceed 24 hours")
        private int rateLimitWindowSeconds = 3600; // 1 hour
        
        private boolean enableCors = true;
        private String[] allowedOrigins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:9000"};
        
        private boolean enableSecurityHeaders = true;
        
        @Min(value = 300, message = "HSTS max age must be at least 300 seconds")
        private long hstsMaxAge = 31536000; // 1 year
    }
}