package venkatsai.cloudnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponseDTO<T> {
    private int status;
    private String message;
    private String error;
    private T data;
    private LocalDateTime timeStamp;
}
