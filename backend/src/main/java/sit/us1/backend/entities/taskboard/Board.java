package sit.us1.backend.entities.taskboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "boards")
public class Board {

    @Id
    @Column(name = "boardId")
    private String id;
    @Column(name = "boardName")
    private String name;
    @Column(name = "isCustomStatus",nullable = false, columnDefinition = "TINYINT", length = 1)
    private Boolean isCustomStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    @OneToOne
    @JoinColumn(name = "oid")
    private BoardUser owner;

    @OneToMany(mappedBy = "board" , fetch = FetchType.EAGER)
    private List<TaskList> taskList;

    @JsonIgnore
    @OneToMany(mappedBy = "board" , fetch = FetchType.EAGER)
    private List<Collaboration> collaborators;

    @Column(name = "createdOn",updatable = false, insertable = false)
    private ZonedDateTime createdOn;
    @Column(name = "updatedOn",updatable = false, insertable = false)
    private ZonedDateTime updatedOn;

    public enum Visibility {
        PUBLIC, PRIVATE
    }
}
