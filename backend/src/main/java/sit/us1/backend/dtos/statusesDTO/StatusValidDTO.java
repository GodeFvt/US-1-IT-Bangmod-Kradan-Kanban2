package sit.us1.backend.dtos.statusesDTO;

import lombok.Data;
import sit.us1.backend.validations.*;

@ValidStatusId(groups = {ValidationGroups.OnDeleteStatusAndTransfer.class, ValidationGroups.OnDeleteStatus.class,ValidationGroups.OnUpdate.class})
@ValidStatusLimit(groups = ValidationGroups.OnDeleteStatusAndTransfer.class)
@ValidUniqueStatusNameOnCreate(groups = ValidationGroups.OnCreate.class)
@ValidUniqueStatusNameOnUpdate(groups = ValidationGroups.OnUpdate.class)
@Data
public class StatusValidDTO {
    private Integer oldStatusId;
    private Integer newStatusId;
    private Integer onPathStatusId;
    private Integer onStatusDTOId;
    private String boardId;
    private String name;
}
