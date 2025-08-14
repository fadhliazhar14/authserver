package authserver.repo;

import authserver.entity.JwkKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwkKeyRepository extends JpaRepository<JwkKey, Long> {
    Optional<JwkKey> findFirstByIsActiveTrue();
}
