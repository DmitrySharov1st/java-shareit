package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundStatusForNotFoundException() {
        NotFoundException exception = new NotFoundException("Not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getError());
    }

    @Test
    void shouldReturnBadRequestStatusForValidationException() {
        ValidationException exception = new ValidationException("Validation failed");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getError());
    }

    @Test
    void shouldReturnConflictStatusForConflictException() {
        ConflictException exception = new ConflictException("Conflict occurred");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleConflictException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict occurred", response.getBody().getError());
    }

    @Test
    void shouldReturnBadRequestForMissingRequestHeader() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException("X-Sharer-User-Id", null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleMissingRequestHeaderException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required header: X-Sharer-User-Id", response.getBody().getError());
    }

    @Test
    void shouldReturnInternalServerErrorForGenericException() {
        Exception exception = new Exception("Unexpected error");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getError());
    }
}