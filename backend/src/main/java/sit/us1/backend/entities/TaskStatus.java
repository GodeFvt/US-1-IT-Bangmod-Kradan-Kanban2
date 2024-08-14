package sit.us1.backend.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "taskstatus")
public class TaskStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, insertable = false ,name = "statusId")
    private Integer id;
    @Column(name = "statusName")
    private String name;
    @Column(name = "statusDescription")
    private String description;
    @Column(name = "statusColor")
    private String color;

    @OneToMany(mappedBy = "status" , fetch = FetchType.EAGER)
    private List<TaskList> taskList;


}
