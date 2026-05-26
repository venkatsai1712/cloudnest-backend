package venkatsai.cloudnest.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import venkatsai.cloudnest.dto.*;
import venkatsai.cloudnest.service.UserService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;
    @Autowired
    public UserController(UserService userService, SecurityContextRepository securityContextRepository){
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<APIResponseDTO<SignUpResponse>> signUp(@RequestBody SignUpRequest req){
        return ResponseEntity.status(HttpStatus.CREATED).body(APIResponseDTO.<SignUpResponse>builder()
                .data(userService.signUp(req))
                .message("Registration Success")
                .status(201)
                .timeStamp(LocalDateTime.now())
                .build());
    }

    @PostMapping("/signin")
    public ResponseEntity<APIResponseDTO<SignInResponse>> signIn(@RequestBody SignInRequest req, HttpServletRequest servletRequest, HttpServletResponse servletResponse){
        Authentication authentication = userService.authenticate(req);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, servletResponse);

        return ResponseEntity.status(HttpStatus.OK).body(APIResponseDTO.<SignInResponse>builder()
                .data(userService.signIn(req))
                .message("Sign In Success")
                .status(200)
                .timeStamp(LocalDateTime.now())
                .build());
    }}
