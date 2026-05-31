package venkatsai.cloudnest.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "files")
public class FileEntity extends BaseEntity {
    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String storagePath;

    private String contentType;

    private long size;

    @OneToMany(mappedBy = "file", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<SharedFileEntity> sharedFiles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private FolderEntity folder;
}
