package com.enset.sdia.springjwt.web;

import com.enset.sdia.springjwt.Services.AccountService;
import com.enset.sdia.springjwt.dtos.ChangePasswordDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@Slf4j
public class SecurityController {
    private JwtEncoder jwtEncoder;
    private AuthenticationManager authenticationManager;
    private PasswordEncoder passwordEncoder;
    private UserDetailsService userDetailsService;
    private AccountService accountService;

    @GetMapping("/profile")
    public Authentication infos(Authentication authentication){
        return authentication;
    }

    @PostMapping("/auth")
    public Map<String,String> token(@RequestParam String username, @RequestParam String password){
        try {
            log.info("Attempting authentication for username: {}", username);
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );

            Instant now = Instant.now();
            String scope = authentication.getAuthorities()
                    .stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.joining(" "));

            JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                    .issuedAt(now)
                    .subject(authentication.getName())
                    .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                    .claim("scope", scope)
                    .build();

            JwtEncoderParameters jwtEncoderParameters = JwtEncoderParameters.from(
                    JwsHeader.with(MacAlgorithm.HS512).build(),
                    jwtClaimsSet
            );

            Jwt jwt = jwtEncoder.encode(jwtEncoderParameters);
            log.info("Authentication successful for username: {}", username);
            return Map.of("access-token", jwt.getTokenValue());
        } catch (Exception e) {
            log.error("Authentication failed for username: {}, error: {}", username, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/change-password")
    public void changePassword(Authentication authentication, @RequestBody ChangePasswordDTO changePasswordDTO) {
        accountService.changePassword(
            authentication.getName(),
            changePasswordDTO.getOldPassword(),
            changePasswordDTO.getNewPassword()
        );
    }
}
