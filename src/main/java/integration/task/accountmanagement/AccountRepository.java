package integration.task.accountmanagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {


    @Query("SELECT a FROM Account a WHERE a.lastModified >= :lastModified")
    List<Account> findAllModifiedAfter(LocalDateTime lastModified);
}
