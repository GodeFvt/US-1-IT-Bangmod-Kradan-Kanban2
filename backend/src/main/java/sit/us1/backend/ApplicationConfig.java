package sit.us1.backend;


import org.modelmapper.ModelMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.us1.backend.properties.FileStorageProperties;
import sit.us1.backend.properties.JwtProperties;
import sit.us1.backend.services.ListMapper;


@Configuration
@EnableConfigurationProperties({FileStorageProperties.class , JwtProperties.class})
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ListMapper listMapper() {
        return ListMapper.getInstance();
    }

}
