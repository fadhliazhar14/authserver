package authserver.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateClientRequest {
    
    @Size(min = 3, max = 100, message = "Client ID must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Client ID can only contain alphanumeric characters, hyphens, and underscores")
    public String clientId;
    
    @Size(min = 8, max = 255, message = "Client secret must be between 8 and 255 characters")
    public String clientSecret; // optional; if blank server generates
    
    @NotBlank(message = "Client name is required")
    @Size(min = 2, max = 200, message = "Client name must be between 2 and 200 characters")
    public String clientName;
    
    @Size(max = 10, message = "Maximum 10 scopes allowed")
    public Set<@Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Scope can only contain alphanumeric characters, dots, hyphens, and underscores") String> scopes;
    
    @Min(value = 60, message = "Access token TTL must be at least 60 seconds")
    @Max(value = 86400, message = "Access token TTL cannot exceed 24 hours (86400 seconds)")
    public Long accessTokenTimeToLiveSeconds;
}
