package venkatsai.cloudnest.controller;

import io.minio.errors.MinioException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import venkatsai.cloudnest.dto.request.share.FileShareRequest;
import venkatsai.cloudnest.dto.request.share.FileShareResponse;
import venkatsai.cloudnest.dto.response.APIResponse;
import venkatsai.cloudnest.dto.response.DownloadedFile;
import venkatsai.cloudnest.service.FileShareService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileShareController {
    private final FileShareService fileShareService;

    public FileShareController(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }

    @PostMapping("/share/file")
    public ResponseEntity<String> shareFile(@RequestBody FileShareRequest req, Authentication authentication) throws FileNotFoundException {
        fileShareService.shareFile(req, authentication.getName());
        return ResponseEntity.status(201).body("File Shared Successfully");
    }

    @GetMapping("/share/files")
    public ResponseEntity<List<FileShareResponse>> getSharedWithMe(Authentication authentication) {
        return ResponseEntity.status(200).body(fileShareService.getSharedWithMe(authentication.getName()));
    }

    @GetMapping("/share/file/{shareId}")
    public ResponseEntity<APIResponse<FileShareResponse>> getSharedFileMetadata(@PathVariable String shareId, Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<FileShareResponse>builder()
                .status(200)
                .message("Metadata Returned")
                .data(fileShareService.getSharedFileMetadata(shareId, authentication.getName()))
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/share/file/download/{shareId}")
    public ResponseEntity<InputStreamResource> downloadSharedFile(@PathVariable String shareId, Authentication authentication) throws IOException, MinioException {
        DownloadedFile file = fileShareService.downloadSharedFile(shareId, authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.name())
                        .build()
                        .toString())
                .body(new InputStreamResource(file.content()));
    }

    @GetMapping("/share/file/download-url/{shareId}")
    public ResponseEntity<APIResponse<String>> getSharedFileDownloadUrl(@PathVariable String shareId, Authentication authentication) throws MinioException, IOException {
        return ResponseEntity.ok(APIResponse.<String>builder()
                .status(200)
                .message("URL Generated")
                .data(fileShareService.getSharedFileDownloadUrl(shareId, authentication.getName()))
                .timeStamp(LocalDateTime.now())
                .build());
    }


    @DeleteMapping("/share/file/{shareId}")
    public ResponseEntity<APIResponse<Void>> deleteSharedFile(@PathVariable String shareId, Authentication authentication) throws IOException, MinioException {
        fileShareService.deleteSharedFile(shareId, authentication.getName());
        return ResponseEntity.ok(APIResponse.<Void>builder()
                .status(200)
                .message("File Deleted")
                .timeStamp(LocalDateTime.now())
                .build());
    }
}
