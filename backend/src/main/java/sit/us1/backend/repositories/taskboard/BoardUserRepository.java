package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.us1.backend.entities.taskboard.BoardUser;

public interface BoardUserRepository extends JpaRepository<BoardUser, String> {

}
