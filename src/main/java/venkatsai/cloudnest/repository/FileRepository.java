package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.FileEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, String> {
    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCase(String name, String email);

    List<FileEntity> findAllByUser_EmailIgnoreCase(String email);

    Optional<FileEntity> findByIdAndUser_EmailIgnoreCase(String id, String email);
}
