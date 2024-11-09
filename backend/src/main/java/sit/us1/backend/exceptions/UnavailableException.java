package sit.us1.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UnavailableException extends RuntimeException {
    public UnavailableException(String message) {
        super(message);
    }

    public HttpStatus getStatusCode() {
        return HttpStatus.SERVICE_UNAVAILABLE;
    }
}
