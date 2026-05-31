package venkatsai.cloudnest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    private String parentId;
}
