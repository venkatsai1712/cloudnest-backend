package venkatsai.cloudnest.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.APIResponseDTO;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.service.FileService;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileController {
    private FileService fileService;
    @Autowired
    public FileController(FileService fileService){
        this.fileService = fileService;
    }

    @PostMapping("/file/upload")
    public ResponseEntity<APIResponseDTO<FileEntity>> uploadFile(@RequestParam MultipartFile file) throws IOException, MinioException {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileService.uploadFiles(file));
    }
    @GetMapping("/files")
    public ResponseEntity<APIResponseDTO<List<FileEntity>>> getFiles() throws FileNotFoundException {
        APIResponseDTO<List<FileEntity>> res = APIResponseDTO.<List<FileEntity>>builder().status(200).message("Files Returned Successfully").data(fileService.getFiles())
                .timeStamp(LocalDateTime.now()).build();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
    @GetMapping("/file/download/{id}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String id) throws IOException, MinioException {
        InputStream inputStream = fileService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\" "+ fileService.getFileName(id) + "\"")
                .body(new InputStreamResource(inputStream));
    }
    @DeleteMapping("/file/{id}")
    public ResponseEntity<APIResponseDTO<FileEntity>> deleteFile(@PathVariable String id) throws IOException, MinioException {
        return ResponseEntity.status(HttpStatus.OK).body(fileService.deleteFile(id));
    }
}

