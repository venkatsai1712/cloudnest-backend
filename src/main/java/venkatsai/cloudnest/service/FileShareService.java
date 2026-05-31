package venkatsai.cloudnest.service;

import io.minio.errors.MinioException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.request.share.FileShareRequest;
import venkatsai.cloudnest.dto.request.share.FileShareResponse;
import venkatsai.cloudnest.dto.response.DownloadedFile;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.entity.FilePermission;
import venkatsai.cloudnest.entity.SharedFileEntity;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.exception.FileAccessDeniedException;
import venkatsai.cloudnest.exception.ResourceNotFoundException;
import venkatsai.cloudnest.repository.FileRepository;
import venkatsai.cloudnest.repository.FileShareRepository;
import venkatsai.cloudnest.repository.UserRepository;
import venkatsai.cloudnest.storage.FileStorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class FileShareService extends BaseResourceService {
    private final FileRepository fileRepository;
    private final FileShareRepository fileShareRepository;
    private final FileStorage fileStorage;

    public FileShareService(FileRepository fileRepository, 
                            UserRepository userRepository, 
                            FileShareRepository fileShareRepository,
                            FileStorage fileStorage) {
        super(userRepository);
        this.fileRepository = fileRepository;
        this.fileShareRepository = fileShareRepository;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public void shareFile(FileShareRequest req, String ownerEmail) throws FileNotFoundException {
        FileEntity fileEntity = fileRepository.findByIdAndUser_EmailIgnoreCase(req.getFileId(), ownerEmail)
                .orElseThrow(() -> new FileNotFoundException("File Not Found"));
        
        UserEntity sharedUser = userRepository.findByEmail(req.getSharedUserEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        SharedFileEntity sharedFileEntity = SharedFileEntity.builder()
                .id(UUID.randomUUID().toString())
                .file(fileEntity)
                .user(sharedUser)
                .permission(req.getPermission())
                .expiresAt(LocalDateTime.now().plusSeconds(req.getExpireSeconds()))
                .build();
        fileShareRepository.save(sharedFileEntity);
    }

    public List<FileShareResponse> getSharedWithMe(String userEmail) {
        return fileShareRepository.findByUser_EmailIgnoreCase(userEmail).stream()
                .filter(s -> s.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::mapToResponse)
                .toList();
    }

    public FileShareResponse getSharedFileMetadata(String shareId, String userEmail) {
        SharedFileEntity sharedFile = validateShareAndPermission(shareId, userEmail, FilePermission.READ);
        return mapToResponse(sharedFile);
    }

    @Transactional(readOnly = true)
    public DownloadedFile downloadSharedFile(String shareId, String userEmail) throws IOException, MinioException {
        SharedFileEntity sharedFile = validateShareAndPermission(shareId, userEmail, FilePermission.DOWNLOAD);
        FileEntity file = sharedFile.getFile();
        return new DownloadedFile(file.getName(), file.getContentType(), fileStorage.load(file.getStoragePath(), file.getId()));
    }

    public String getSharedFileDownloadUrl(String shareId, String userEmail) throws MinioException, IOException {
        SharedFileEntity sharedFile = validateShareAndPermission(shareId, userEmail, FilePermission.DOWNLOAD);
        FileEntity file = sharedFile.getFile();
        return fileStorage.getDownloadPresignedURL(file.getStoragePath(), file.getId(), file.getName(), file.getContentType());
    }

    @Transactional(rollbackFor = Exception.class)
    public FileShareResponse updateSharedFileContent(String shareId, MultipartFile file, String userEmail) throws IOException, MinioException {
        SharedFileEntity sharedFile = validateShareAndPermission(shareId, userEmail, FilePermission.WRITE);
        FileEntity fileEntity = sharedFile.getFile();
        fileStorage.store(fileEntity.getId(), file, file.getContentType());
        fileEntity.setSize(file.getSize());
        fileEntity.setContentType(file.getContentType());
        fileRepository.save(fileEntity);
        return mapToResponse(sharedFile);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSharedFile(String shareId, String userEmail) throws IOException, MinioException {
        SharedFileEntity sharedFile = validateShareAndPermission(shareId, userEmail, FilePermission.DELETE);
        FileEntity file = sharedFile.getFile();
        fileStorage.delete(file.getStoragePath(), file.getId());
        fileRepository.delete(file);
    }

    private SharedFileEntity validateShareAndPermission(String shareId, String userEmail, FilePermission required) {
        SharedFileEntity sharedFile = fileShareRepository.findByIdAndUser_EmailIgnoreCase(shareId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));
        if (sharedFile.getExpiresAt().isBefore(LocalDateTime.now())) throw new FileAccessDeniedException("Expired");
        if (sharedFile.getPermission() != FilePermission.ALL && sharedFile.getPermission() != required && required != FilePermission.READ) throw new FileAccessDeniedException("Denied");
        return sharedFile;
    }

    private FileShareResponse mapToResponse(SharedFileEntity sharedFile) {
        FileEntity file = sharedFile.getFile();
        return FileShareResponse.builder()
                .id(sharedFile.getId())
                .userId(sharedFile.getUser().getEmail())
                .fileId(file.getId())
                .permission(String.valueOf(sharedFile.getPermission()))
                .expiresAt(sharedFile.getExpiresAt())
                .contentType(file.getContentType())
                .size(file.getSize())
                .status(file.getStatus())
                .name(file.getName())
                .build();
    }

    public List<FileShareResponse> getSharedFiles(String mail) {
        return null;
    }
}
