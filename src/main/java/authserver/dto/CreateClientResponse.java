package authserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateClientResponse {
    private String clientId;
    private String clientSecret;
    private String clientName;
    private Set<String> scopes;
    private Long accessTokenTimeToLiveSeconds;
    private LocalDateTime createdAt;
    
    public CreateClientResponse(String clientId, String clientSecret, String clientName) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.createdAt = LocalDateTime.now();
    }
}