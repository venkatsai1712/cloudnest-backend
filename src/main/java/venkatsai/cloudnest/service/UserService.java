package venkatsai.cloudnest.service;

import org.apache.catalina.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import venkatsai.cloudnest.dto.SignInRequest;
import venkatsai.cloudnest.dto.SignUpRequest;
import venkatsai.cloudnest.dto.SignUpResponse;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.repository.UserRepository;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public SignUpResponse signUp(SignUpRequest req){
        String id = String.valueOf(UUID.randomUUID());
        UserEntity user = UserEntity.builder()
                .id(id)
                .name(req.getName())
                .email(req.getEmail())
                .password(req.getPassword())
                .build();
        userRepository.save(user);
        return SignUpResponse.builder()
                .email(req.getEmail()).name(req.getName()).build();
    }

    public SignUpResponse signIn(SignInRequest req) {
        UserEntity user = userRepository.findByEmail(req.getEmail());
        if(user == null){
            throw new UsernameNotFoundException("Invalid Credentials");
        }

    }
}
