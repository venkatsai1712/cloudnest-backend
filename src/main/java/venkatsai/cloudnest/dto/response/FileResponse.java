package venkatsai.cloudnest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import venkatsai.cloudnest.entity.FileStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponse {
    private String id;
    private String name;
    private String folderId;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
    private FileStatus status;
}

