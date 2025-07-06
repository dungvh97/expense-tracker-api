package com.nvd.expensetracker.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/info")
    public ResponseEntity<String> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("Logged in as: " + userDetails.getUsername());
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminOnly(@AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().toString().contains("ADMIN")) {
            return ResponseEntity.status(403).body("Access denied: Not ADMIN");
        }
        return ResponseEntity.ok("Welcome admin: " + userDetails.getUsername());
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse response = new UserProfileResponse(
                userDetails.getUsername(),
                userDetails.getAuthorities().toString()
                );
        return ResponseEntity.ok(response);
    }

    record UserProfileResponse(String email, String roles) {}
}