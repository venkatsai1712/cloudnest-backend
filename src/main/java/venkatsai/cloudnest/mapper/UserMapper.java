package venkatsai.cloudnest.mapper;

import org.springframework.stereotype.Component;
import venkatsai.cloudnest.dto.response.UserProfileResponse;
import venkatsai.cloudnest.entity.UserEntity;

@Component
public class UserMapper {
    public UserProfileResponse toProfileResponse(UserEntity user) {
        return UserProfileResponse.builder()
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
