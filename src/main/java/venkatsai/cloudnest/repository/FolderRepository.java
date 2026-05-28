package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.FolderEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, String> {
    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndParentIsNull(String name, String email);

    boolean existsByNameEqualsIgnoreCaseAndUser_EmailIgnoreCaseAndParent_Id(String name, String email, String parentId);

    List<FolderEntity> findAllByUser_EmailIgnoreCaseAndParentIsNull(String email);

    List<FolderEntity> findAllByUser_EmailIgnoreCaseAndParent_Id(String email, String parentId);

    long countByParent_IdAndUser_EmailIgnoreCase(String parentId, String email);

    Optional<FolderEntity> findByIdAndUser_EmailIgnoreCase(String id, String email);
}
