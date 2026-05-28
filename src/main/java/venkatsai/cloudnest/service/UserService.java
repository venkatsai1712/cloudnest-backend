package venkatsai.cloudnest.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import venkatsai.cloudnest.dto.SignInRequest;
import venkatsai.cloudnest.dto.SignInResponse;
import venkatsai.cloudnest.dto.SignUpRequest;
import venkatsai.cloudnest.dto.SignUpResponse;
import venkatsai.cloudnest.dto.UpdateProfileRequest;
import venkatsai.cloudnest.dto.UserProfileResponse;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.mapper.UserMapper;
import venkatsai.cloudnest.repository.UserRepository;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       UserMapper userMapper){
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
    }
    @Transactional(rollbackFor = Exception.class)
    public SignUpResponse signUp(SignUpRequest req){
        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }

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

    public Authentication authenticate(SignInRequest req) {
        userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken
                (req.getEmail(),req.getPassword());
        return authenticationManager.authenticate(token);
    }

    @Transactional(readOnly = true)
    public SignInResponse signIn(SignInRequest req) {
        UserEntity user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
        return SignInResponse.builder()
                .name(user.getName())
                .email(req.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        return userMapper.toProfileResponse(getUser(email));
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        UserEntity user = getUser(email);
        String name = request.getName() == null ? null : request.getName().trim();

        if (name != null) {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Name must not be blank");
            }
            user.setName(name);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));
        }

        return userMapper.toProfileResponse(userRepository.save(user));
    }

    @Transactional(rollbackFor = Exception.class)
    public UserProfileResponse deleteProfile(String email) {
        UserEntity user = getUser(email);
        UserProfileResponse response = userMapper.toProfileResponse(user);
        userRepository.delete(user);
        return response;
    }

    private UserEntity getUser(String email) {
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Invalid Credentials");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
    }
}
