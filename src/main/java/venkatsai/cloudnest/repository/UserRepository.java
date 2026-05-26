package venkatsai.cloudnest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import venkatsai.cloudnest.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    UserEntity findByEmail(String email);
}
