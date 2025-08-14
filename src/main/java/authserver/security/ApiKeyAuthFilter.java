package authserver.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;

import java.io.IOException;
import java.util.List;


@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    @Value("${app.admin.api-key}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        String path = req.getRequestURI();
        
        // Only apply API key authentication to admin endpoints
        if (requiresApiKeyAuth(path)) {
            String key = req.getHeader("X-API-KEY");
            
            logger.debug("API key authentication required for path: {}", path);
            
            if (key == null || key.isBlank()) {
                logger.warn("Missing API key for admin endpoint: {}", path);
                sendUnauthorizedResponse(res, "Missing API key");
                return;
            }
            
            if (!key.equals(adminApiKey)) {
                logger.warn("Invalid API key attempted for admin endpoint: {}", path);
                sendUnauthorizedResponse(res, "Invalid API key");
                return;
            }
            
            // Set authentication for admin endpoints
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("api-key-admin", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("API key authentication successful for admin endpoint: {}", path);
        }

        chain.doFilter(req, res);
    }

    private boolean requiresApiKeyAuth(String path) {
        return path.matches("^/api/keys/rotate") ||
               path.matches("^/api/clients/.*/admin$") ||
               path.matches("^/api/admin/.*");
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}
