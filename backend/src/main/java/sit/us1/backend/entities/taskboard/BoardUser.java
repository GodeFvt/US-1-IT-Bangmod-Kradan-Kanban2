package sit.us1.backend.entities.taskboard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class BoardUser {
    @Id
    @Column(name = "oid")
    private String id;
}
