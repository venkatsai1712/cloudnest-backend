package venkatsai.cloudnest.service;

import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.exception.ResourceNotFoundException;
import venkatsai.cloudnest.repository.UserRepository;

public abstract class BaseResourceService {
    protected final UserRepository userRepository;

    protected BaseResourceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    protected UserEntity getOwner(String email) {
        if (email == null || email.isBlank()) {
            throw new ResourceNotFoundException("User identity not provided");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for: " + email));
    }
}
