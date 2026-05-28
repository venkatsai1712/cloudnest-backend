package venkatsai.cloudnest.service;

import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import venkatsai.cloudnest.dto.DownloadedFile;
import venkatsai.cloudnest.dto.FileResponse;
import venkatsai.cloudnest.entity.FileEntity;
import venkatsai.cloudnest.entity.FolderEntity;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.exception.DuplicateFileException;
import venkatsai.cloudnest.exception.FileStorageValidationException;
import venkatsai.cloudnest.exception.ResourceNotFoundException;
import venkatsai.cloudnest.mapper.FileMapper;
import venkatsai.cloudnest.repository.FileRepository;
import venkatsai.cloudnest.repository.UserRepository;
import venkatsai.cloudnest.storage.FileStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileService {
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;
    private final FileMapper fileMapper;
    private final FolderService folderService;

    public FileService(FileRepository fileRepository,
                       UserRepository userRepository,
                       FileStorage fileStorage,
                       FileMapper fileMapper,
                       FolderService folderService) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.fileStorage = fileStorage;
        this.fileMapper = fileMapper;
        this.folderService = folderService;
    }

    @Transactional(rollbackFor = Exception.class)
    public FileResponse uploadFile(MultipartFile file, String ownerEmail) throws IOException, MinioException {
        return uploadFile(file, ownerEmail, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public FileResponse uploadFile(MultipartFile file, String ownerEmail, String folderId) throws IOException, MinioException {
        validateFile(file);
        UserEntity owner = getOwner(ownerEmail);
        String filename = getOriginalFilename(file);
        FolderEntity folder = resolveFolder(folderId, ownerEmail);

        if (fileExists(filename, ownerEmail, folder)) {
            throw new DuplicateFileException("File already exists");
        }

        String fileId = UUID.randomUUID().toString();
        String contentType = resolveContentType(file);
        FileEntity fileEntity = buildEntity(file, fileId, filename, contentType, owner, folder);

        fileStorage.store(fileId, file, contentType);
        return fileMapper.toResponse(fileRepository.save(fileEntity));
    }

    @Transactional(readOnly = true)
    public List<FileResponse> getFiles(String ownerEmail, String folderId) {
        getOwner(ownerEmail);
        List<FileEntity> files;
        if (folderId == null || folderId.isBlank()) {
            files = fileRepository.findAllByUser_EmailIgnoreCase(ownerEmail);
        } else {
            FolderEntity folder = folderService.findOwnedFolder(folderId, ownerEmail);
            files = fileRepository.findAllByUser_EmailIgnoreCaseAndFolder_Id(ownerEmail, folder.getId());
        }
        if (files.isEmpty()) {
            throw new ResourceNotFoundException("Files not found");
        }
        return files.stream()
                .map(fileMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile downloadFile(String id, String ownerEmail) throws IOException, MinioException {
        FileEntity file = findOwnedFile(id, ownerEmail);
        return new DownloadedFile(
                file.getName(),
                resolveStoredContentType(file),
                fileStorage.load(file.getStoragePath(), file.getId())
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public FileResponse deleteFile(String id, String ownerEmail) throws IOException, MinioException {
        FileEntity file = findOwnedFile(id, ownerEmail);
        fileStorage.delete(file.getStoragePath(), file.getId());
        fileRepository.delete(file);
        return fileMapper.toResponse(file);
    }

    FileEntity buildEntity(MultipartFile file, String fileId, String filename, String contentType, UserEntity owner, FolderEntity folder) {
        return FileEntity.builder()
                .id(fileId)
                .name(filename)
                .contentType(contentType)
                .createdAt(LocalDateTime.now())
                .storagePath(fileStorage.bucketName())
                .size(file.getSize())
                .user(owner)
                .folder(folder)
                .build();
    }

    private FileEntity findOwnedFile(String id, String ownerEmail) {
        getOwner(ownerEmail);
        return fileRepository.findByIdAndUser_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    private UserEntity getOwner(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageValidationException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new FileStorageValidationException("File size must not exceed 10MB");
        }
        getOriginalFilename(file);
    }

    private String getOriginalFilename(MultipartFile file) {
        String filename = Objects.requireNonNullElse(file.getOriginalFilename(), "").trim();
        if (filename.isBlank()) {
            throw new FileStorageValidationException("File name is required");
        }
        return filename;
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null || contentType.isBlank() ? DEFAULT_CONTENT_TYPE : contentType;
    }

    private String resolveStoredContentType(FileEntity file) {
        return file.getContentType() == null || file.getContentType().isBlank()
                ? DEFAULT_CONTENT_TYPE
                : file.getContentType();
    }

    private FolderEntity resolveFolder(String folderId, String ownerEmail) {
        if (folderId == null || folderId.isBlank()) {
            return null;
        }
        return folderService.findOwnedFolder(folderId, ownerEmail);
    }

    private boolean fileExists(String filename, String ownerEmail, FolderEntity folder) {
        return folder == null
                ? fileRepository.existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndFolderIsNull(filename, ownerEmail)
                : fileRepository.existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndFolder_Id(filename, ownerEmail, folder.getId());
    }
}
