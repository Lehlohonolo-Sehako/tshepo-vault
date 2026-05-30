package app.tshepo.repository;

import app.tshepo.domain.BankConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface BankConnectionRepository extends JpaRepository<BankConnection, Long> {
    Optional<BankConnection> findByHolderLogin(String holderLogin);
}
