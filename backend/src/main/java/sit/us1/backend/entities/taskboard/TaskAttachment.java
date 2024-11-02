package sit.us1.backend.entities.taskboard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "taskattachment")
public class TaskAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer taskId;
    private String filename;
    private String storedName;
    private String contentType;
    @Column(updatable = false, insertable = false)
    private ZonedDateTime uploadedAt;


    public TaskAttachment(Integer taskId, String filename, String storedName, String contentType) {
        this.taskId = taskId;
        this.filename = filename;
        this.storedName = storedName;
        this.contentType = contentType;
    }
}
