package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotFoundException_ReturnsNotFoundStatus() {
        NotFoundException exception = new NotFoundException("Not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                (ResponseEntity<GlobalExceptionHandler.ErrorResponse>)
                        handler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getError());
    }

    @Test
    void handleValidationException_ReturnsBadRequestStatus() {
        ValidationException exception = new ValidationException("Validation failed");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                (ResponseEntity<GlobalExceptionHandler.ErrorResponse>)
                        handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getError());
    }

    @Test
    void handleException_ReturnsInternalServerError() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                (ResponseEntity<GlobalExceptionHandler.ErrorResponse>)
                        handler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getError());
    }
}