package sit.us1.backend.entities.taskboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaborationId implements Serializable {
    private String boardId;
    private String oid;
}
