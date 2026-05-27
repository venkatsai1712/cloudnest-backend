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
import venkatsai.cloudnest.dto.APIResponse;
import venkatsai.cloudnest.dto.DownloadedFile;
import venkatsai.cloudnest.dto.FileResponse;
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
                                                                Authentication authentication) throws IOException, MinioException {
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<FileResponse>builder()
                .data(fileService.uploadFile(file, authentication.getName()))
                .status(201)
                .message("File Created Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }


    @GetMapping("/files")
    public ResponseEntity<APIResponse<List<FileResponse>>> getFiles(Authentication authentication) {
        APIResponse<List<FileResponse>> res = APIResponse.<List<FileResponse>>builder()
                .status(200)
                .message("Files Returned Successfully")
                .data(fileService.getFiles(authentication.getName()))
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(res);
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
}

