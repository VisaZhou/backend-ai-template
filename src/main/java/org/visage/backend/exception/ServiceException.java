package org.visage.backend.exception;

import lombok.Getter;import java.io.Serial;

@Getter
public final class ServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;
    private Integer code;
    private String message;
    private String detailMessage;

    public ServiceException() {
    }

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public ServiceException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}