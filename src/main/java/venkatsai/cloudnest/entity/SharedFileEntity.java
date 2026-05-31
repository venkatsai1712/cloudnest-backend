package venkatsai.cloudnest.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "shared_files")
public class SharedFileEntity {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private FilePermission permission;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
