# Keycloak-brokered Auth0 RBAC POC

This document supersedes the original direct-Auth0 design. The current application trusts only Keycloak. Auth0 remains an upstream identity provider configured inside Keycloak and is not a Spring Boot OAuth2 client registration.

## Request flow

```text
Browser -> Spring Boot -> Keycloak -> Auth0 -> user
```

- Login initiation: `http://localhost:8080/oauth2/authorization/keycloak`
- Spring Boot callback: `http://localhost:8080/login/oauth2/code/keycloak`
- Trusted issuer: `http://localhost:8081/realms/identity-poc`
- Realm role claim: `realm_access.roles`

Keycloak realm roles `ADMIN` and `USER` become Spring authorities `ROLE_ADMIN` and `ROLE_USER`. See the root `README.md` for complete client, protocol-mapper, logout, and verification instructions.
