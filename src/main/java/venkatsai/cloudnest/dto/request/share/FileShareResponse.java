package venkatsai.cloudnest.dto.request.share;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import venkatsai.cloudnest.entity.FileStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShareResponse {
    private String id;
    private String fileId;
    private String userId;
    private String permission;
    private LocalDateTime expiresAt;
    private String name;
    private String folderId;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
    private FileStatus status;
}
