package com.akaslan.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, Object id) {
        super(
            resourceName + " bulunamadı. ID: " + id,
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }
}
