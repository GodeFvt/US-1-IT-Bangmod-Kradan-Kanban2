package sit.us1.backend.entities.taskboard;

import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasklists")
public class TaskList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, insertable = false)
    private Integer id;

    @Column(length = 100)
    private String title;
    @Column(length = 500)
    private String description;
    @Column(length = 30)
    private String assignees;

    @ManyToOne
    @JoinColumn(name = "statusId")
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "boardId")
    private Board board;

    @Column(updatable = false, insertable = false)
    private ZonedDateTime createdOn;
    @Column(updatable = false, insertable = false)
    private ZonedDateTime updatedOn;


}
