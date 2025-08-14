package authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    private final AuthServerProperties authServerProperties;

    public CorsConfig(AuthServerProperties authServerProperties) {
        this.authServerProperties = authServerProperties;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configure allowed origins from properties
        if (authServerProperties.getSecurity().isEnableCors()) {
            String[] allowedOrigins = authServerProperties.getSecurity().getAllowedOrigins();
            if (allowedOrigins != null && allowedOrigins.length > 0) {
                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
            } else {
                // Default allowed origins for development
                configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost:9000"));
            }
        } else {
            // CORS disabled - no origins allowed
            configuration.setAllowedOrigins(List.of());
        }
        
        // Configure allowed methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Configure allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-API-KEY", "Accept", "Origin", 
                "Access-Control-Request-Method", "Access-Control-Request-Headers"
        ));
        
        // Configure exposed headers
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"
        ));
        
        // Allow credentials (cookies, authorization headers, TLS certificates)
        configuration.setAllowCredentials(true);
        
        // Max age for preflight requests (1 hour)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}