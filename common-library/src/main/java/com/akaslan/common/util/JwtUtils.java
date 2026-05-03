package com.akaslan.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class JwtUtils {

    public String getCurrentUserId() {
        return getJwt()
                .map(Jwt::getSubject)
                .orElseThrow(() -> new IllegalStateException("SecurityContext'te kimlik doğrulanmış kullanıcı bulunamadı"));
    }

    public String getCurrentUserEmail() {
        return getJwt()
                .map(jwt -> jwt.getClaimAsString("email"))
                .orElseThrow(() -> new IllegalStateException("SecurityContext'te kimlik doğrulanmış kullanıcı bulunamadı"));
    }

    @SuppressWarnings("unchecked")
    public List<String> getCurrentUserRoles() {
        return getJwt().map(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null) return Collections.<String>emptyList();
            Object roles = realmAccess.get("roles");
            if (roles instanceof List<?> list) return (List<String>) list;
            return Collections.<String>emptyList();
        }).orElse(Collections.emptyList());
    }

    public boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }

    public Optional<Object> getClaim(String claimName) {
        return getJwt().map(jwt -> jwt.getClaim(claimName));
    }

    private Optional<Jwt> getJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(jwtAuth.getToken());
        }
        return Optional.empty();
    }
}
