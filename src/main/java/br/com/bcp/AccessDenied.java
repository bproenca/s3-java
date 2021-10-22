package br.com.bcp;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDenied extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AccessDenied(String filename) {
        super(String.format("Access to file '%s' denied", filename));
    }
}
