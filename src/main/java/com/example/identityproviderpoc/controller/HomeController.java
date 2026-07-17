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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Controller
public class HomeController {

    private static final Set<String> APPLICATION_ROLES = Set.of("ADMIN", "USER");

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
        List<String> roles = applicationRoles(claims);

        model.addAttribute("user", oidcUser);
        model.addAttribute("name", displayName(oidcUser));
        model.addAttribute("roles", roles);
        model.addAttribute("authorities", authentication.getAuthorities());
        model.addAttribute("claims", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(claims));
        return "profile";
    }

    private List<String> applicationRoles(Map<String, Object> claims) {
        Object realmAccessClaim = claims.get("realm_access");
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)
                || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(role -> role.trim().toUpperCase(Locale.ROOT))
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .filter(APPLICATION_ROLES::contains)
                .distinct()
                .toList();
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
