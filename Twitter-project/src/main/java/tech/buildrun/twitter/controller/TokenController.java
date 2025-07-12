package tech.buildrun.twitter.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tech.buildrun.twitter.dto.LoginRequest;
import tech.buildrun.twitter.dto.LoginResponse;
import tech.buildrun.twitter.service.AuthenticationService;

@RestController
public class TokenController {

    private final AuthenticationService authenticationService;

    public TokenController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        var loginResponse = authenticationService.login(loginRequest);

        return ResponseEntity.ok(loginResponse);
    }
}
