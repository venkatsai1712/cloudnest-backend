package venkatsai.cloudnest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

 @Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLFileChunkUploadRequest {
    private String uploadId;
    private long partNumber;
    private long chunkSize;
}


