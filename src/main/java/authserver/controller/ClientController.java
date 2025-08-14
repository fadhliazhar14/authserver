package authserver.controller;

import authserver.dto.CreateClientRequest;
import authserver.dto.CreateClientResponse;
import authserver.service.ClientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public ResponseEntity<CreateClientResponse> create(@Valid @RequestBody CreateClientRequest req) {
        logger.info("Received request to create new client: {}", req.getClientName());
        
        ClientService.CreatedClient created = clientService.createClient(
                req.getClientId(), req.getClientSecret(), req.getClientName(),
                req.getScopes(), req.getAccessTokenTimeToLiveSeconds()
        );
        
        CreateClientResponse resp = new CreateClientResponse();
        resp.setClientId(created.getRegisteredClient().getClientId());
        resp.setClientSecret(created.getRawSecret());
        resp.setClientName(created.getRegisteredClient().getClientName());
        resp.setScopes(created.getRegisteredClient().getScopes());
        resp.setAccessTokenTimeToLiveSeconds(req.getAccessTokenTimeToLiveSeconds());
        
        logger.info("Successfully created client with ID: {}", resp.getClientId());
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<Map<String, Object>> get(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid client ID format")
            @Size(min = 3, max = 100, message = "Client ID must be between 3 and 100 characters")
            String clientId) {
        
        logger.debug("Looking up client with ID: {}", clientId);
        
        var rc = clientService.findByClientId(clientId);
        if (rc == null) {
            logger.warn("Client not found with ID: {}", clientId);
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(Map.of(
                "clientId", rc.getClientId(),
                "clientName", rc.getClientName(),
                "scopes", rc.getScopes(),
                "clientIdIssuedAt", rc.getClientIdIssuedAt()
        ));
    }

    @DeleteMapping("/{clientId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid client ID format")
            @Size(min = 3, max = 100, message = "Client ID must be between 3 and 100 characters")
            String clientId) {
        
        logger.info("Admin request to delete client with ID: {}", clientId);
        
        clientService.deleteByClientId(clientId);
        
        logger.info("Successfully deleted client with ID: {}", clientId);
        return ResponseEntity.noContent().build();
    }
}
