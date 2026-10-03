package com.memora.memora_backend.auth;

import com.memora.memora_backend.auth.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user identity",
            description = "Accepts user signup registration criteria and establishes a persistent account entity profile.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registration successfully executed"),
            @ApiResponse(responseCode = "400", description = "Supplied credentials fail structural schema validation rules")
    })
    @PostMapping("/signup")
    public ResponseEntity<UserDto> register(@RequestBody RegisterUserDto registerUserDto) {
        UserDto registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @Operation(
            summary = "Authenticate user credentials",
            description = "Validates identity claims against secure credential states to yield state-independent authorization Bearer JWT tokens.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Identity validation clear; bearer token provisioned"),
            @ApiResponse(responseCode = "401", description = "Invalid identity pairing or credentials supplied")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        return ResponseEntity.ok(authenticationService.authenticate(loginUserDto));
    }

    @Operation(
            summary = "Refresh token for user",
            description = "Refreshes a user's access token using a valid refresh token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refresh was successful"),
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }
}