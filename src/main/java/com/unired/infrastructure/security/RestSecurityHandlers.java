package com.unired.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.ErrorDetail;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestSecurityHandlers {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeJsonError(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "No autenticado. Inicia sesión nuevamente."
        );
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeJsonError(
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "No tienes permisos para realizar esta acción"
        );
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        ApiResponse<ErrorDetail> body = ApiResponse.<ErrorDetail>builder()
                .success(false)
                .message(message)
                .data(ErrorDetail.builder()
                        .status(status)
                        .error(status == HttpServletResponse.SC_UNAUTHORIZED ? "Unauthorized" : "Forbidden")
                        .build())
                .build();
        objectMapper.writeValue(response.getWriter(), body);
    }
}
