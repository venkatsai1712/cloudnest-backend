package venkatsai.cloudnest.dto.response;

import java.io.InputStream;

public record DownloadedFile(
        String name,
        String contentType,
        InputStream content
) {
}
