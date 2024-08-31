package sit.us1.backend.repositories.taskboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.us1.backend.entities.taskboard.Board;

import java.util.List;
@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findAllByOwner_Id(String oid);
}
