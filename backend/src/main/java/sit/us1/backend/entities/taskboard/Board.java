package sit.us1.backend.entities.taskboard;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(name = "visibility")
    private String visibility;
    @OneToOne
    @JoinColumn(name = "oid")
    private BoardUser owner;

    @OneToMany(mappedBy = "board" , fetch = FetchType.EAGER)
    private List<TaskList> taskList;

}
