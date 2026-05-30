package app.tshepo.repository;

import app.tshepo.domain.IssuedClaim;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the IssuedClaim entity.
 */
@Repository
public interface IssuedClaimRepository extends JpaRepository<IssuedClaim, Long> {
    default Optional<IssuedClaim> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<IssuedClaim> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<IssuedClaim> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select issuedClaim from IssuedClaim issuedClaim left join fetch issuedClaim.credential",
        countQuery = "select count(issuedClaim) from IssuedClaim issuedClaim"
    )
    Page<IssuedClaim> findAllWithToOneRelationships(Pageable pageable);

    @Query("select issuedClaim from IssuedClaim issuedClaim left join fetch issuedClaim.credential")
    List<IssuedClaim> findAllWithToOneRelationships();

    @Query("select issuedClaim from IssuedClaim issuedClaim left join fetch issuedClaim.credential where issuedClaim.id =:id")
    Optional<IssuedClaim> findOneWithToOneRelationships(@Param("id") Long id);
}
