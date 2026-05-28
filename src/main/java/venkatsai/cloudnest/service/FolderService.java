package venkatsai.cloudnest.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import venkatsai.cloudnest.dto.FileResponse;
import venkatsai.cloudnest.dto.FolderContentsResponse;
import venkatsai.cloudnest.dto.FolderCreateRequest;
import venkatsai.cloudnest.dto.FolderResponse;
import venkatsai.cloudnest.entity.FolderEntity;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.exception.DuplicateFileException;
import venkatsai.cloudnest.exception.FileStorageValidationException;
import venkatsai.cloudnest.exception.ResourceNotFoundException;
import venkatsai.cloudnest.mapper.FileMapper;
import venkatsai.cloudnest.mapper.FolderMapper;
import venkatsai.cloudnest.repository.FileRepository;
import venkatsai.cloudnest.repository.FolderRepository;
import venkatsai.cloudnest.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FolderService {
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final FolderMapper folderMapper;
    private final FileMapper fileMapper;

    public FolderService(FolderRepository folderRepository,
                         FileRepository fileRepository,
                         UserRepository userRepository,
                         FolderMapper folderMapper,
                         FileMapper fileMapper) {
        this.folderRepository = folderRepository;
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.folderMapper = folderMapper;
        this.fileMapper = fileMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public FolderResponse createFolder(FolderCreateRequest request, String ownerEmail) {
        UserEntity owner = getOwner(ownerEmail);
        String name = validateFolderName(request.getName());
        FolderEntity parent = resolveParent(request.getParentId(), ownerEmail);

        if (folderExists(name, ownerEmail, parent)) {
            throw new DuplicateFileException("Folder already exists");
        }

        FolderEntity folder = FolderEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .createdAt(LocalDateTime.now())
                .user(owner)
                .parent(parent)
                .build();

        return folderMapper.toResponse(folderRepository.save(folder));
    }

    @Transactional(readOnly = true)
    public List<FolderResponse> getFolders(String parentId, String ownerEmail) {
        getOwner(ownerEmail);
        List<FolderEntity> folders = parentId == null || parentId.isBlank()
                ? folderRepository.findAllByUser_EmailIgnoreCaseAndParentIsNull(ownerEmail)
                : folderRepository.findAllByUser_EmailIgnoreCaseAndParent_Id(ownerEmail, findOwnedFolder(parentId, ownerEmail).getId());

        return folders.stream()
                .map(folderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FolderContentsResponse getFolderContents(String folderId, String ownerEmail) {
        getOwner(ownerEmail);
        FolderEntity folder = folderId == null || folderId.isBlank() ? null : findOwnedFolder(folderId, ownerEmail);

        List<FolderResponse> folders = (folder == null
                ? folderRepository.findAllByUser_EmailIgnoreCaseAndParentIsNull(ownerEmail)
                : folderRepository.findAllByUser_EmailIgnoreCaseAndParent_Id(ownerEmail, folder.getId()))
                .stream()
                .map(folderMapper::toResponse)
                .toList();

        List<FileResponse> files = (folder == null
                ? fileRepository.findAllByUser_EmailIgnoreCaseAndFolderIsNull(ownerEmail)
                : fileRepository.findAllByUser_EmailIgnoreCaseAndFolder_Id(ownerEmail, folder.getId()))
                .stream()
                .map(fileMapper::toResponse)
                .toList();

        return FolderContentsResponse.builder()
                .folder(folderMapper.toResponse(folder))
                .folders(folders)
                .files(files)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public FolderResponse deleteFolder(String folderId, String ownerEmail) {
        FolderEntity folder = findOwnedFolder(folderId, ownerEmail);
        long childFolders = folderRepository.countByParent_IdAndUser_EmailIgnoreCase(folderId, ownerEmail);
        long childFiles = fileRepository.countByFolder_IdAndUser_EmailIgnoreCase(folderId, ownerEmail);
        if (childFolders > 0 || childFiles > 0) {
            throw new FileStorageValidationException("Folder must be empty before deletion");
        }

        folderRepository.delete(folder);
        return folderMapper.toResponse(folder);
    }

    public FolderEntity findOwnedFolder(String folderId, String ownerEmail) {
        if (folderId == null || folderId.isBlank()) {
            throw new ResourceNotFoundException("Folder not found");
        }
        getOwner(ownerEmail);
        return folderRepository.findByIdAndUser_EmailIgnoreCase(folderId, ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found"));
    }

    private FolderEntity resolveParent(String parentId, String ownerEmail) {
        if (parentId == null || parentId.isBlank()) {
            return null;
        }
        return findOwnedFolder(parentId, ownerEmail);
    }

    private boolean folderExists(String name, String ownerEmail, FolderEntity parent) {
        return parent == null
                ? folderRepository.existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndParentIsNull(name, ownerEmail)
                : folderRepository.existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndParent_Id(name, ownerEmail, parent.getId());
    }

    private UserEntity getOwner(String ownerEmail) {
        if (ownerEmail == null || ownerEmail.isBlank()) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String validateFolderName(String name) {
        String folderName = name == null ? "" : name.trim();
        if (folderName.isBlank()) {
            throw new FileStorageValidationException("Folder name is required");
        }
        if (folderName.length() > 255) {
            throw new FileStorageValidationException("Folder name must not exceed 255 characters");
        }
        if (folderName.contains("/") || folderName.contains("\\")) {
            throw new FileStorageValidationException("Folder name must not contain path separators");
        }
        return folderName;
    }
}
