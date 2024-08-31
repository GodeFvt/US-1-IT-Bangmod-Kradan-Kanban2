package sit.us1.backend.dtos.statusesDTO;

import lombok.Data;
import sit.us1.backend.validations.ValidStatusId;
import sit.us1.backend.validations.ValidStatusLimit;
import sit.us1.backend.validations.ValidUniqueStatusName;
import sit.us1.backend.validations.ValidationGroups;

@ValidStatusId(groups = {ValidationGroups.OnDeleteStatusAndTransfer.class, ValidationGroups.OnDeleteStatus.class,ValidationGroups.OnUpdate.class})
@ValidStatusLimit(groups = ValidationGroups.OnDeleteStatusAndTransfer.class)
@ValidUniqueStatusName(groups = ValidationGroups.OnCreate.class)
@Data
public class StatusValidDTO {
    private Integer oldStatusId;
    private Integer newStatusId;
    private String boardId;
    private String name;
}
