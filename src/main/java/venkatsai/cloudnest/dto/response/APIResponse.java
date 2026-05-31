package venkatsai.cloudnest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class APIResponse<T> {
    private int status;
    private String message;
    private String error;
    private T data;
    private LocalDateTime timeStamp;
}
