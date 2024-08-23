package sit.us1.backend.dtos.JwtDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class JwtTokenResponseDTO {
    private String access_token;
}
