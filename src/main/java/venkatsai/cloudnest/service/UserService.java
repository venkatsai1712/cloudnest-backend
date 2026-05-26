package venkatsai.cloudnest.service;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import venkatsai.cloudnest.dto.SignInRequest;
import venkatsai.cloudnest.dto.SignInResponse;
import venkatsai.cloudnest.dto.SignUpRequest;
import venkatsai.cloudnest.dto.SignUpResponse;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.repository.UserRepository;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    public SignUpResponse signUp(SignUpRequest req){
        String id = String.valueOf(UUID.randomUUID());
        UserEntity user = UserEntity.builder()
                .id(id)
                .name(req.getName())
                .email(req.getEmail())
                .password(bCryptPasswordEncoder.encode(req.getPassword()))
                .build();
        userRepository.save(user);
        return SignUpResponse.builder()
                .email(req.getEmail()).name(req.getName()).build();
    }

    public SignInResponse signIn(SignInRequest req) {
        UserEntity user = userRepository.findByEmail(req.getEmail());
        if(user == null){
            throw new UsernameNotFoundException("Invalid Credentials");
        }
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken
                (req.getEmail(),req.getPassword());
        Authentication auth = authenticationManager.authenticate(token);
        return SignInResponse.builder()
                .name(user.getName())
                .email(req.getEmail())
                .build();
    }
}
