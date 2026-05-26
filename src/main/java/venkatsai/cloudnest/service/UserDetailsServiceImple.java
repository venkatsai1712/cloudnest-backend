package venkatsai.cloudnest.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.repository.UserRepository;

@Service
public class UserDetailsServiceImple implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    public UserDetailsServiceImple(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username);
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
