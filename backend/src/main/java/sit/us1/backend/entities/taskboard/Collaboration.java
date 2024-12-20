package sit.us1.backend.entities.taskboard;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(CollaborationId.class)
@Table(name = "collaboration")
public class Collaboration {
    @JsonIgnore
    @Id
    @Column(name = "boardId")
    private String boardId;
    @Id
    @Column(name = "oid")
    private String oid;
    @Column(name = "access")
    @Enumerated(EnumType.STRING)
    private Access accessRight;
    @Column(name = "isPending",nullable = false, columnDefinition = "TINYINT", length = 1)
    private Boolean isPending;
    @Column(name = "addedOn", updatable = false, insertable = false)
    private ZonedDateTime addedOn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "boardId", insertable = false, updatable = false)
    private Board board;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "oid", insertable = false, updatable = false)
    private BoardUser boardUser;

    public enum Access {
        READ, WRITE
    }
}
