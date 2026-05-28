package venkatsai.cloudnest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class URLFileUploadRequest {
    private String name;
    private String size;
    private String contentType;
}
