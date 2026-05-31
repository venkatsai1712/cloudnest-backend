package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.SharedFileEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileShareRepository extends JpaRepository<SharedFileEntity, String> {
    List<SharedFileEntity> findByUser_EmailIgnoreCase(String email);
    Optional<SharedFileEntity> findByIdAndUser_EmailIgnoreCase(String id, String email);
}
