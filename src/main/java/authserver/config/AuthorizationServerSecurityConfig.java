package authserver.config;

import authserver.security.ApiKeyAuthFilter;
import authserver.security.RateLimitingFilter;
import authserver.service.JwkKeyService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class AuthorizationServerSecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;

    public AuthorizationServerSecurityConfig(ApiKeyAuthFilter apiKeyAuthFilter, RateLimitingFilter rateLimitingFilter) {
        this.apiKeyAuthFilter = apiKeyAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityChain(HttpSecurity http) throws Exception {
        org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.build();
    }

    @Bean
    public SecurityFilterChain defaultSecurityChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/api/clients").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/oauth2/jwks").permitAll()
                        
                        // API Documentation endpoints - public access
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        
                        // Actuator endpoints - health and info are public, others need auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").authenticated()
                        
                        // Admin endpoints - require API key authentication
                        .requestMatchers("/api/clients/*/admin", "/api/keys/**").authenticated()
                        .requestMatchers("/api/admin/**").authenticated()
                        
                        // All other requests are permitted
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                    .frameOptions().deny()
                    .contentTypeOptions().and()
                    .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                        .maxAgeInSeconds(31536000)
                        .includeSubDomains(true))
                );

        // Add rate limiting filter first, then API key filter
        http.addFilterBefore(rateLimitingFilter, BasicAuthenticationFilter.class);
        http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository repo) {
        return new JdbcOAuth2AuthorizationService(jdbcTemplate, repo);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JwkKeyService jwkKeyService) {
        RSAKey rsa = jwkKeyService.getOrCreateActiveRsaKey(2048);
        JWKSet set = new JWKSet(rsa);
        return (sel, ctx) -> sel.select(set);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(@Value("${app.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
