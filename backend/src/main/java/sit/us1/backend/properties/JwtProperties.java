package sit.us1.backend.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private String secretRefresh;
    private double maxTokenIntervalHour;
    private double maxRefreshTokenIntervalHour;
    private String issuer;
    private String issuerMS;
}
