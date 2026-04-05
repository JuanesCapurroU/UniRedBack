package com.unired.application.dto.response;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    private int status;
    private String error;
    private Map<String, String> validationErrors;
}
