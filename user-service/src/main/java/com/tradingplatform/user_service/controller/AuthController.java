package com.tradingplatform.user_service.controller;

import com.tradingplatform.user_service.dto.AuthResponse;
import com.tradingplatform.user_service.dto.LoginDto;
import com.tradingplatform.user_service.dto.RegisterDto;
import com.tradingplatform.user_service.dto.UserResponseDto;
import com.tradingplatform.user_service.model.User;
import com.tradingplatform.user_service.repository.UserRepository;
import com.tradingplatform.user_service.security.JwtTokenProvider;
import com.tradingplatform.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDto request) {
        UserResponseDto response = authService.register(request);

        String token = jwtTokenProvider.generateToken(response.getUsername(), response.getId(),
                response.getRole().name());

        AuthResponse authResponse = new AuthResponse(token, response.getId(),
                response.getUsername(), response.getEmail(), response.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        Optional<User> user = Optional.ofNullable(userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getPassword())
                .orElseThrow(() -> new RuntimeException("User not found")));

        String token = jwtTokenProvider.generateToken(
                user.get().getUsername(),
                user.get().getId(),
                user.get().getRole().name()
        );

        AuthResponse authResponse = new AuthResponse(
                token,
                user.get().getId(),
                user.get().getUsername(),
                user.get().getEmail(),
                user.get().getRole().name()
        );

        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("User Service is running!");
    }
}
