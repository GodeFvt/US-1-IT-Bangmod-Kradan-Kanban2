package sit.us1.backend.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.account.User;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}
