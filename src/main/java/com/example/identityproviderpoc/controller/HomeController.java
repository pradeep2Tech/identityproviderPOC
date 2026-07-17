package com.example.identityproviderpoc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private static final String ROLES_CLAIM = "https://identityproviderpoc.example.com/roles";

    private final ObjectMapper objectMapper;

    public HomeController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    String home(@AuthenticationPrincipal OidcUser user, Model model) {
        if (user != null) {
            model.addAttribute("username", displayName(user));
        }
        return "index";
    }

    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal OidcUser oidcUser,
            Authentication authentication,
            Model model) throws JsonProcessingException {
        Map<String, Object> claims = oidcUser.getIdToken().getClaims();
        List<String> roles = oidcUser.getClaimAsStringList(ROLES_CLAIM);

        model.addAttribute("user", oidcUser);
        model.addAttribute("name", displayName(oidcUser));
        model.addAttribute("roles", roles == null || roles.isEmpty() ? List.of() : roles);
        model.addAttribute("authorities", authentication.getAuthorities());
        model.addAttribute("claims", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(claims));
        return "profile";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    private String displayName(OidcUser user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getPreferredUsername() != null && !user.getPreferredUsername().isBlank()) {
            return user.getPreferredUsername();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return user.getSubject();
    }

}
