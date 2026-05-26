package venkatsai.cloudnest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import venkatsai.cloudnest.service.UserDetailsServiceImple;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImple userDetails;
    public SecurityConfig(UserDetailsServiceImple userDetails){
        this.userDetails = userDetails;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(req -> req
                                .requestMatchers("/api/auth/*").permitAll()
                        .anyRequest().authenticated())
                .userDetailsService(userDetails)
                .build();
    }

}
