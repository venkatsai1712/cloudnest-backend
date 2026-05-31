package venkatsai.cloudnest.exception;

import io.minio.errors.MinioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import venkatsai.cloudnest.dto.response.APIResponse;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<APIResponse<Void>> unauthorized(RuntimeException ex) {
        return error(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid Credentials");
    }

    @ExceptionHandler(DuplicateFileException.class)
    public ResponseEntity<APIResponse<Void>> fileAlreadyExists(DuplicateFileException ex) {
        return error(HttpStatus.CONFLICT, "Failed", ex.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class, FileNotFoundException.class})
    public ResponseEntity<APIResponse<Void>> fileNotFound(Exception ex){
        return error(HttpStatus.NOT_FOUND, "Failed", ex.getMessage());
    }

    @ExceptionHandler(FileAccessDeniedException.class)
    public ResponseEntity<APIResponse<Void>> fileAccessDenied(FileAccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage());
    }

    @ExceptionHandler({FileStorageValidationException.class, MultipartException.class, IllegalArgumentException.class})
    public ResponseEntity<APIResponse<Void>> badRequest(Exception ex) {
        return error(HttpStatus.BAD_REQUEST, "Failed", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Void>> validationFailed(MethodArgumentNotValidException ex) {
        String error = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, "Validation Failed", error);
    }

    @ExceptionHandler({MinioException.class, IOException.class})
    public ResponseEntity<APIResponse<Void>> storageFailure(Exception ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "Storage service unavailable", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Void>> handleGenericException(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", ex.getMessage());
    }

    private ResponseEntity<APIResponse<Void>> error(HttpStatus status, String message, String error) {
        APIResponse<Void> res = APIResponse.<Void>builder()
                .status(status.value())
                .message(message)
                .error(error)
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(status).body(res);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }
}
