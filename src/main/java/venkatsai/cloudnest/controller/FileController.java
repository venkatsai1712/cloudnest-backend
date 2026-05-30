package venkatsai.cloudnest.controller;

import io.minio.errors.MinioException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.*;
import venkatsai.cloudnest.service.FileService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService){
        this.fileService = fileService;
    }

    @PostMapping("/file/upload")
    public ResponseEntity<APIResponse<FileResponse>> uploadFile(@RequestParam("file") MultipartFile file,
                                                                @RequestParam(required = false) String folderId,
                                                                Authentication authentication) throws IOException, MinioException {
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<FileResponse>builder()
                .data(fileService.uploadFile(file, authentication.getName(), folderId))
                .status(201)
                .message("File Created Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }


    @GetMapping("/files")
    public ResponseEntity<APIResponse<List<FileResponse>>> getFiles(@RequestParam(required = false) String folderId,
                                                                    Authentication authentication) {
        APIResponse<List<FileResponse>> res = APIResponse.<List<FileResponse>>builder()
                .status(200)
                .message("Files Returned Successfully")
                .data(fileService.getFiles(authentication.getName(), folderId))
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/file/upload-url")
    public ResponseEntity<APIResponse<String>> getUploadPresignedURL(@RequestBody URLFileUploadRequest req, Authentication authentication) throws MinioException, IOException {
        String url = fileService.getUploadPresignedURL(req.getName(), req.getContentType(), Long.parseLong(req.getSize()), authentication.getName());
        return ResponseEntity.ok(APIResponse.<String>builder()
                .status(200)
                .message("Upload URL Generated")
                .data(url)
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/file/download-url/{id}")
    public ResponseEntity<APIResponse<String>> getDownloadPresignedURL(@PathVariable String id, Authentication authentication) throws MinioException, IOException {
        String url = fileService.getDownloadPresignedURL(id, authentication.getName());
        return ResponseEntity.ok(APIResponse.<String>builder()
                .status(200)
                .message("Download URL Generated")
                .data(url)
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/file/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String id,
                                                            Authentication authentication) throws IOException, MinioException {
        DownloadedFile file = fileService.downloadFile(id, authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.name())
                        .build()
                        .toString())
                .body(new InputStreamResource(file.content()));
    }


    @DeleteMapping("/file/{id}")
    public ResponseEntity<APIResponse<FileResponse>> deleteFile(@PathVariable String id,
                                                                Authentication authentication) throws IOException, MinioException {
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.<FileResponse>builder()
                .message("File Deleted Successfully")
                .status(200)
                .timeStamp(LocalDateTime.now())
                .data(fileService.deleteFile(id, authentication.getName()))
                .build());
    }


    @PostMapping("/file/upload/chunks-initiate")
    public ResponseEntity<APIResponse<String>> uploadChunksInitiate(@RequestBody URLFileUploadRequest metadata, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(APIResponse.<String>builder()
                .message("Upload Id Returned Successfully")
                .status(200)
                .timeStamp(LocalDateTime.now())
                .data(fileService.uploadChunksInitiate(metadata, authentication.getName()))
                .build());
    }

    @PostMapping("/file/upload/chunks-url")
    public ResponseEntity<APIResponse<String>> getUploadChunkPresignedURL(@RequestBody URLFileChunkUploadRequest req, Authentication authentication) throws MinioException, IOException {
        String url = fileService.getUploadChunkPresignedURL(req.getUploadId(), req.getPartNumber(), req.getChunkSize(), authentication.getName());
        return ResponseEntity.ok(APIResponse.<String>builder()
                .status(200)
                .message("Upload URL Generated")
                .data(url)
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/file/upload/chunks-complete")
    public ResponseEntity<APIResponse<String>> chunksUploadComplete(@RequestBody ChunksUploadedRequest req, Authentication authentication) throws MinioException {
         return ResponseEntity.ok(APIResponse.<String>builder()
                .status(200)
                .message("Upload Completed")
                .data(fileService.chunksUploadComplete(req.getUploadId(),req.getPartNumbers(),authentication.getName()))
                .timeStamp(LocalDateTime.now())
                .build());
    }
}

