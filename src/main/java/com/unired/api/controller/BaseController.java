package com.unired.api.controller;

import com.unired.application.dto.response.ApiResponse;
import com.unired.util.constants.SecurityConstants;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {

    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .correlationId(MDC.get(SecurityConstants.CORRELATION_ID_KEY))
                .build());
    }

    protected ResponseEntity<ApiResponse<Void>> ok(String message) {
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .correlationId(MDC.get(SecurityConstants.CORRELATION_ID_KEY))
                .build());
    }
}
