package authserver.security;

import authserver.config.AuthServerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final AuthServerProperties authServerProperties;
    private final Map<String, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    public RateLimitingFilter(AuthServerProperties authServerProperties) {
        this.authServerProperties = authServerProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Only apply rate limiting to public client creation endpoint
        if ("POST".equals(method) && "/api/clients".equals(path)) {
            String clientIp = getClientIpAddress(request);
            
            if (isRateLimited(clientIp)) {
                logger.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, path);
                sendRateLimitExceededResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientIp) {
        LocalDateTime now = LocalDateTime.now();
        RateLimitInfo rateLimitInfo = rateLimitMap.get(clientIp);

        if (rateLimitInfo == null) {
            rateLimitMap.put(clientIp, new RateLimitInfo(1, now));
            return false;
        }

        // Check if the time window has passed
        LocalDateTime windowStart = now.minusSeconds(authServerProperties.getSecurity().getRateLimitWindowSeconds());
        
        if (rateLimitInfo.getFirstRequestTime().isBefore(windowStart)) {
            // Reset the counter for a new time window
            rateLimitMap.put(clientIp, new RateLimitInfo(1, now));
            return false;
        }

        // Check if rate limit is exceeded
        if (rateLimitInfo.getRequestCount() >= authServerProperties.getSecurity().getRateLimitRequests()) {
            return true;
        }

        // Increment the request count
        rateLimitInfo.incrementRequestCount();
        return false;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429); // HTTP 429 Too Many Requests
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"errorCode\": \"RATE_LIMIT_EXCEEDED\", " +
            "\"message\": \"Too many requests. Please try again later.\", " +
            "\"timestamp\": \"" + LocalDateTime.now() + "\"}"
        );
    }

    private static class RateLimitInfo {
        private int requestCount;
        private final LocalDateTime firstRequestTime;

        public RateLimitInfo(int requestCount, LocalDateTime firstRequestTime) {
            this.requestCount = requestCount;
            this.firstRequestTime = firstRequestTime;
        }

        public int getRequestCount() {
            return requestCount;
        }

        public LocalDateTime getFirstRequestTime() {
            return firstRequestTime;
        }

        public void incrementRequestCount() {
            this.requestCount++;
        }
    }
}