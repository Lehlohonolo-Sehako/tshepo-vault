package app.tshepo.repository;

import app.tshepo.domain.Credential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long>, JpaSpecificationExecutor<Credential> {
    Optional<Credential> findByCredentialUuid(UUID credentialUuid);
    Optional<Credential> findByCredentialUuidAndHolderLogin(UUID credentialUuid, String holderLogin);
    Page<Credential> findAllByHolderLoginOrderByIssuedAtDesc(String holderLogin, Pageable pageable);
}
