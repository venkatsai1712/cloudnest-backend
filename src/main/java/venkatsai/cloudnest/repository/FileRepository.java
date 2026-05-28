package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.FileEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCase(String name, String email);

    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndFolderIsNull(String name, String email);

    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndFolder_Id(String name, String email, String folderId);

    List<FileEntity> findAllByUser_EmailIgnoreCase(String email);

    List<FileEntity> findAllByUser_EmailIgnoreCaseAndFolderIsNull(String email);

    List<FileEntity> findAllByUser_EmailIgnoreCaseAndFolder_Id(String email, String folderId);

    long countByFolder_IdAndUser_EmailIgnoreCase(String folderId, String email);

    Optional<FileEntity> findByIdAndUser_EmailIgnoreCase(String id, String email);
}
