package com.LunaLink.application.web.exception;

import com.LunaLink.application.web.dto.StandardErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata os erros de regra de negócio (ex: Data já reservada)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<StandardErrorDTO> handleBusinessRuleException(IllegalStateException ex, HttpServletRequest request) {
        StandardErrorDTO error = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Conflict - Regra de Negócio",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Trata os erros de dados não encontrados (ex: Usuário ou Espaço não existe)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardErrorDTO> handleNotFoundException(IllegalArgumentException ex, HttpServletRequest request) {
        StandardErrorDTO error = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(), // 400 Bad Request
                "Bad Request - Dados inválidos",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Tratamento genério para evitar vazando de stacktrace no client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        StandardErrorDTO error = new StandardErrorDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocorreu um erro inesperado no servidor.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

}
