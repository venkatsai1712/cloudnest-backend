package venkatsai.cloudnest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderContentsResponse {
    private FolderResponse folder;
    private List<FolderResponse> folders;
    private List<FileResponse> files;
}
