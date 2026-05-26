package venkatsai.cloudnest.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import venkatsai.cloudnest.dto.*;
import venkatsai.cloudnest.service.UserService;

import java.time.LocalDateTime;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
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
    public ResponseEntity<APIResponseDTO<SignInResponse>> signIn(@RequestBody SignInRequest req, HttpServletRequest servletRequest){
        servletRequest.getSession(true);
        return ResponseEntity.status(HttpStatus.OK).body(APIResponseDTO.<SignInResponse>builder()
                .data(userService.signIn(req))
                .message("Sign In Success")
                .status(200)
                .timeStamp(LocalDateTime.now())
                .build());
    }}
