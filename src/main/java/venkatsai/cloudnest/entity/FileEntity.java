package venkatsai.cloudnest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileEntity {
    @Id
    private String id;
    private String fileName;
    @JsonIgnore
    private String storagePath;
    private String mime;
    private long size;
    private LocalDateTime createdAt;
    private  LocalDateTime modifiedAt;
}
