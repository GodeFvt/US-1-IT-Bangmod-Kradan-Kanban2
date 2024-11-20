package sit.us1.backend.entities.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MsUser {
    private String oid;
    private String name;
    private String email;
}
