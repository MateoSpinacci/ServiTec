
package com.servitec.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : "Error en la solicitud",
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ApiError error = new ApiError(
                400,
                "El parámetro '" + ex.getName() + "' tiene un valor inválido",
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        String path = ((ServletWebRequest) request)
                .getRequest()
                .getRequestURI();

        String message = switch (statusCode.value()) {
            case 400 -> "La solicitud contiene datos inválidos";
            case 404 -> "Recurso no encontrado";
            case 405 -> "Método HTTP no permitido";
            case 415 -> "Tipo de contenido no soportado";
            default -> "Error al procesar la solicitud";
        };

        ApiError error = new ApiError(
                statusCode.value(),
                message,
                LocalDateTime.now(),
                path
        );

        return new ResponseEntity<>(error, headers, statusCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error(
                "Error inesperado en {}",
                request.getRequestURI(),
                ex
        );

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ocurrió un error interno en el servidor",
                LocalDateTime.now(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}
