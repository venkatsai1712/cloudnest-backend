package venkatsai.cloudnest.mapper;

import org.springframework.stereotype.Component;
import venkatsai.cloudnest.dto.FolderResponse;
import venkatsai.cloudnest.entity.FolderEntity;

@Component
public class FolderMapper {
    public FolderResponse toResponse(FolderEntity folder) {
        if (folder == null) {
            return null;
        }
        return FolderResponse.builder()
                .id(folder.getId())
                .name(folder.getName())
                .parentId(folder.getParent() == null ? null : folder.getParent().getId())
                .createdAt(folder.getCreatedAt())
                .build();
    }
}
