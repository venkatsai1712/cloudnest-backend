package venkatsai.cloudnest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import venkatsai.cloudnest.dto.APIResponseDTO;
import venkatsai.cloudnest.entity.FileEntity;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<APIResponseDTO<FileEntity>> fileAlreadyExists(
            FileAlreadyExistsException ex) {

        APIResponseDTO<FileEntity> res = APIResponseDTO.<FileEntity>builder()
                .status(409)
                .message("Failed")
                .error(ex.getMessage())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
    }
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<APIResponseDTO<FileEntity>> fileNotFound(FileNotFoundException ex){
        APIResponseDTO<FileEntity> res = APIResponseDTO.<FileEntity>builder()
                .status(404)
                .message("Failed")
                .error(ex.getMessage())
                .timeStamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
    }
}