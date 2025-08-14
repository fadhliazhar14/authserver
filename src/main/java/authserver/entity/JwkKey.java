package authserver.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jwk_keys", indexes = {
    @Index(name = "idx_jwk_kid", columnList = "kid"),
    @Index(name = "idx_jwk_active", columnList = "is_active")
})
public class JwkKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kid", nullable = false, unique = true, length = 100)
    private String kid;

    @Lob
    @Column(name = "public_key_pem", columnDefinition = "LONGTEXT", nullable = false)
    private String publicKeyPem;

    @Lob
    @Column(name = "private_key_pem", columnDefinition = "LONGTEXT", nullable = false)
    private String privateKeyPem;

    @Column(name = "algorithm", nullable = false, length = 20)
    private String algorithm;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
}
