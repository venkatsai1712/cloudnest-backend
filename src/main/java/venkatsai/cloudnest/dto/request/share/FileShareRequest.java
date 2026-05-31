package venkatsai.cloudnest.dto.request.share;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import venkatsai.cloudnest.entity.FilePermission;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileShareRequest {
    private String fileId;
    private String sharedUserEmail;
    private FilePermission permission;
    private long expireSeconds;
}
