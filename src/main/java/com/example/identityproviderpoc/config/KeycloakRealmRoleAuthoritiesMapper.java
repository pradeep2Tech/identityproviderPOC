package com.example.identityproviderpoc.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class KeycloakRealmRoleAuthoritiesMapper implements GrantedAuthoritiesMapper {

    private static final Set<String> APPLICATION_ROLES = Set.of("ADMIN", "USER");

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(
            Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new LinkedHashSet<>(authorities);

        authorities.stream()
                .filter(OidcUserAuthority.class::isInstance)
                .map(OidcUserAuthority.class::cast)
                .forEach(authority -> {
                    addRealmRoles(authority.getIdToken().getClaims(), mappedAuthorities);
                    if (authority.getUserInfo() != null) {
                        addRealmRoles(authority.getUserInfo().getClaims(), mappedAuthorities);
                    }
                });

        return mappedAuthorities;
    }

    private void addRealmRoles(Map<String, Object> claims, Set<GrantedAuthority> authorities) {
        Object realmAccessClaim = claims.get("realm_access");
        if (!(realmAccessClaim instanceof Map<?, ?> realmAccess)) {
            return;
        }

        Object rolesClaim = realmAccess.get("roles");
        if (!(rolesClaim instanceof Collection<?> roles)) {
            return;
        }

        roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> role.toUpperCase(Locale.ROOT))
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .filter(APPLICATION_ROLES::contains)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .forEach(authorities::add);
    }
}
