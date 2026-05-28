package venkatsai.cloudnest.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import venkatsai.cloudnest.dto.APIResponse;
import venkatsai.cloudnest.dto.FolderContentsResponse;
import venkatsai.cloudnest.dto.FolderCreateRequest;
import venkatsai.cloudnest.dto.FolderResponse;
import venkatsai.cloudnest.service.FolderService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FolderController {
    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping("/folder")
    public ResponseEntity<APIResponse<FolderResponse>> createFolder(@Valid @RequestBody FolderCreateRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponse.<FolderResponse>builder()
                .data(folderService.createFolder(request, authentication.getName()))
                .status(201)
                .message("Folder Created Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/folders")
    public ResponseEntity<APIResponse<List<FolderResponse>>> getFolders(@RequestParam(required = false) String parentId,
                                                                        Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<List<FolderResponse>>builder()
                .data(folderService.getFolders(parentId, authentication.getName()))
                .status(200)
                .message("Folders Returned Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @GetMapping("/folder/contents")
    public ResponseEntity<APIResponse<FolderContentsResponse>> getFolderContents(@RequestParam(required = false) String folderId,
                                                                                Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<FolderContentsResponse>builder()
                .data(folderService.getFolderContents(folderId, authentication.getName()))
                .status(200)
                .message("Folder Contents Returned Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @DeleteMapping("/folder/{id}")
    public ResponseEntity<APIResponse<FolderResponse>> deleteFolder(@PathVariable String id,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(APIResponse.<FolderResponse>builder()
                .data(folderService.deleteFolder(id, authentication.getName()))
                .status(200)
                .message("Folder Deleted Successfully")
                .timeStamp(LocalDateTime.now())
                .build());
    }
}
