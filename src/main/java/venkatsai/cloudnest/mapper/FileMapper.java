package venkatsai.cloudnest.mapper;

import org.springframework.stereotype.Component;
import venkatsai.cloudnest.dto.FileResponse;
import venkatsai.cloudnest.entity.FileEntity;

@Component
public class FileMapper {
    public FileResponse toResponse(FileEntity file) {
        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .folderId(file.getFolder() == null ? null : file.getFolder().getId())
                .contentType(file.getContentType())
                .size(file.getSize())
                .createdAt(file.getCreatedAt())
                .status(file.getStatus())
                .build();
    }
}

