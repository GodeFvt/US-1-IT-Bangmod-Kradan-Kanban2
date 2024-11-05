package sit.us1.backend.entities.taskboard;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Integer id;
    @JsonIgnore
    private Integer taskId;
    private String filename;
    @JsonIgnore
    private String storedName;
    private String contentType;
    @Column(updatable = false, insertable = false)
    private ZonedDateTime uploadedAt;


    @ManyToOne
    @JoinColumn(name = "taskId", insertable = false, updatable = false)
    @JsonBackReference
    private TaskList task;


    public TaskAttachment(Integer taskId, String filename, String storedName, String contentType) {
        this.taskId = taskId;
        this.filename = filename;
        this.storedName = storedName;
        this.contentType = contentType;
    }
}
