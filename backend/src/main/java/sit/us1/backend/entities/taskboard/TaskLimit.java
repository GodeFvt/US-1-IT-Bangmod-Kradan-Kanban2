package sit.us1.backend.entities.taskboard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasklimits")
public class TaskLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskLimitId")
    private Integer id;
    @Column(name = "maximumTask")
    private Integer maximumTask;
    @Column(name = "isLimit",nullable = false, columnDefinition = "TINYINT", length = 1)
    private Boolean isLimit;
    private String boardId;
}
