package com.smartstadium.controller;

import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.dto.OtpRequestDto;
import com.smartstadium.dto.OtpVerifyDto;
import com.smartstadium.service.AuthService;
import com.smartstadium.service.JwtService;
import com.smartstadium.service.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/otp/request")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody OtpRequestDto request) {
        otpService.generateAndSend(request.getEmail());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyDto request,
                                                      HttpServletResponse response) {
        AuthResponseDto authResponse = authService.verifyOtpAndLogin(request.getEmail(), request.getOtp());

        // Set refresh token as HttpOnly cookie
        String refreshToken = jwtService.generateRefreshToken(authResponse.getEmail());
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);

        return ResponseEntity.ok(authResponse);
    }
}
