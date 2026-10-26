package sit.us1.backend.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.account.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, String> {
}
