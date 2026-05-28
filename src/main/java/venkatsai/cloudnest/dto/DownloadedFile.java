package venkatsai.cloudnest.dto;

import java.io.InputStream;

public record DownloadedFile(
        String name,
        String contentType,
        InputStream content
) {
}
