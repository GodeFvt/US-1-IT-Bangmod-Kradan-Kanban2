package sit.us1.backend.dtos.statusesDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import sit.us1.backend.validations.*;



@Data
public class SimpleStatusDTO {
    private Integer id;
    @NotNull
    @NotBlank
    @ValidUniqueStatusName(groups = ValidationGroups.OnCreate.class)
    @Size(max = 50)
    private String name;
    @Size(max = 200)
    private String description;
    private String color;

    public void setName(String value) {
        if (value != null) {
            this.name = value.trim();
            if (this.name.toLowerCase().equals("no status")) {
                this.name = "No Status";
            }
        }
    }

    public void setDescription(String value) {
        if (value == null || value.isEmpty()) {
            this.description = null;
        } else {
            this.description = value.trim();
        }

    }

}
