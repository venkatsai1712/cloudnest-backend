package venkatsai.cloudnest.service;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import venkatsai.cloudnest.entity.UserEntity;
import venkatsai.cloudnest.repository.UserRepository;

@Service
public class UserDetailsServiceImple implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImple(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid Credentials"));
        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }
}
