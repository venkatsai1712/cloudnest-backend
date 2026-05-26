package venkatsai.cloudnest.controller;

import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import venkatsai.cloudnest.dto.APIResponseDTO;
import venkatsai.cloudnest.dto.SignInRequest;
import venkatsai.cloudnest.dto.SignUpRequest;
import venkatsai.cloudnest.dto.SignUpResponse;
import venkatsai.cloudnest.service.UserService;

import java.time.LocalDateTime;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;
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

    @PostMapping("/sigin")
    public ResponseEntity<APIResponseDTO<SignUpResponse>> signIn(@RequestBody SignInRequest req){
        return ResponseEntity.status(HttpStatus.OK).body(APIResponseDTO.<SignUpResponse>builder()
                .data(userService.signIn(req))
                .message("Sign In Success")
                .status(200)
                .timeStamp(LocalDateTime.now())
                .build());
    }}
