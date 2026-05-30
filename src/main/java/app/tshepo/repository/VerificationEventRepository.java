package app.tshepo.repository;

import app.tshepo.domain.VerificationEvent;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the VerificationEvent entity.
 */
@Repository
public interface VerificationEventRepository extends JpaRepository<VerificationEvent, Long>, JpaSpecificationExecutor<VerificationEvent> {
    default Optional<VerificationEvent> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<VerificationEvent> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<VerificationEvent> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select verificationEvent from VerificationEvent verificationEvent left join fetch verificationEvent.apiKey",
        countQuery = "select count(verificationEvent) from VerificationEvent verificationEvent"
    )
    Page<VerificationEvent> findAllWithToOneRelationships(Pageable pageable);

    @Query("select verificationEvent from VerificationEvent verificationEvent left join fetch verificationEvent.apiKey")
    List<VerificationEvent> findAllWithToOneRelationships();

    @Query(
        "select verificationEvent from VerificationEvent verificationEvent left join fetch verificationEvent.apiKey where verificationEvent.id =:id"
    )
    Optional<VerificationEvent> findOneWithToOneRelationships(@Param("id") Long id);
}
