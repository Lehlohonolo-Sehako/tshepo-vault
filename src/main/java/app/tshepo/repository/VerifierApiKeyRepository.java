package app.tshepo.repository;

import app.tshepo.domain.VerifierApiKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifierApiKeyRepository extends JpaRepository<VerifierApiKey, Long>, JpaSpecificationExecutor<VerifierApiKey> {
    List<VerifierApiKey> findAllByOwnerLoginAndActiveTrueOrderByCreatedAtDesc(String ownerLogin);
    Optional<VerifierApiKey> findByKeyUuidAndOwnerLogin(UUID keyUuid, String ownerLogin);

    @Modifying
    @Query("UPDATE VerifierApiKey k SET k.callCount = k.callCount + 1 WHERE k.id = :id")
    void incrementCallCount(Long id);
}
