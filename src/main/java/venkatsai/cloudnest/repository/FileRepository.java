package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity,Long> {
    boolean existsByFileNameEqualsIgnoreCase(String fileName);
    FileEntity findById(String id);
}
