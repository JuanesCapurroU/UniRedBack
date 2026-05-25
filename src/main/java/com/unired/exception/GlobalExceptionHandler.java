package com.unired.exception;

import com.unired.application.dto.response.ApiResponse;
import com.unired.application.dto.response.ErrorDetail;
import com.unired.exception.custom.AccountLockedException;
import com.unired.exception.custom.CancelacionFueraDeplazoException;
import com.unired.exception.custom.DominioNoPermitidoException;
import com.unired.exception.custom.MentorSinCapacidadException;
import com.unired.exception.custom.RecursoNoEncontradoException;
import com.unired.exception.custom.SinCuposException;
import com.unired.exception.custom.TokenInvalidoException;
import com.unired.util.constants.SecurityConstants;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> b));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                ErrorDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validation Error")
                        .validationErrors(errors)
                        .build(),
                request
        );
    }

    @ExceptionHandler(DominioNoPermitidoException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleBadRequest(DominioNoPermitidoException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .build(),
                request
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .build(),
                request
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Bad Request")
                        .build(),
                request
        );
    }

    @ExceptionHandler({BadCredentialsException.class, TokenInvalidoException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUnauthorized(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error("Unauthorized")
                        .build(),
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "No tienes permisos para realizar esta acción",
                ErrorDetail.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("Forbidden")
                        .build(),
                request
        );
    }

    @ExceptionHandler({EntityNotFoundException.class, RecursoNoEncontradoException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleNotFound(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error("Not Found")
                        .build(),
                request
        );
    }

    @ExceptionHandler({SinCuposException.class, MentorSinCapacidadException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleConflict(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error("Conflict")
                        .build(),
                request
        );
    }

    @ExceptionHandler({CancelacionFueraDeplazoException.class})
    public ResponseEntity<ApiResponse<ErrorDetail>> handleUnprocessable(RuntimeException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error("Unprocessable Entity")
                        .build(),
                request
        );
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleLocked(AccountLockedException ex, HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.LOCKED,
                ex.getMessage(),
                ErrorDetail.builder()
                        .status(HttpStatus.LOCKED.value())
                        .error("Locked")
                        .build(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDetail>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception. correlationId={}", resolveCorrelationId(request), ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno",
                ErrorDetail.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error("Internal Server Error")
                        .build(),
                request
        );
    }

    private ResponseEntity<ApiResponse<ErrorDetail>> buildErrorResponse(
            HttpStatus status,
            String message,
            ErrorDetail detail,
            HttpServletRequest request
    ) {
        ApiResponse<ErrorDetail> response = ApiResponse.<ErrorDetail>builder()
                .success(false)
                .message(message)
                .data(detail)
                .correlationId(resolveCorrelationId(request))
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        Object requestValue = request.getAttribute(SecurityConstants.CORRELATION_ID_KEY);
        if (requestValue != null) {
            return requestValue.toString();
        }
        String mdcValue = MDC.get(SecurityConstants.CORRELATION_ID_KEY);
        return mdcValue == null ? "N/A" : mdcValue;
    }
}
