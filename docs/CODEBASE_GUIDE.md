# Codebase Guide

This document is the fast navigation index for `identityproviderPOC`. Read it before scanning the repository.

## Purpose

The application is a Java 21 and Spring Boot 3.5.x server-rendered web application. It trusts Keycloak through OpenID Connect Authorization Code Flow using Spring Security OAuth2 Client. Keycloak brokers authentication to Auth0; the application does not use the Auth0 Java SDK.

## Request flow

```text
GET /
  -> public home page
  -> /oauth2/authorization/keycloak
  -> Keycloak, which brokers authentication to Auth0
  -> /login/oauth2/code/keycloak
  -> authenticated home page
  -> GET /profile for ID-token details
  -> GET /admin for users with ROLE_ADMIN
  -> POST /logout for local Spring Security logout
  -> redirect to /
```

## Source map

| Concern | Authoritative file | Notes |
|---|---|---|
| Application entry point | `src/main/java/com/example/identityproviderpoc/IdentityProviderPocApplication.java` | Component scanning starts from `com.example.identityproviderpoc`. |
| Access rules, OAuth2 login, logout | `src/main/java/com/example/identityproviderpoc/config/SecurityConfig.java` | Uses `SecurityFilterChain` and lambda DSL. Logout is a CSRF-protected POST. |
| Keycloak realm-role mapping | `src/main/java/com/example/identityproviderpoc/config/KeycloakRealmRoleAuthoritiesMapper.java` | Maps `realm_access.roles` while preserving OIDC and scope authorities. |
| Home and profile models | `src/main/java/com/example/identityproviderpoc/controller/HomeController.java` | Extracts the authenticated `OidcUser`, display name, token claims, and roles. |
| Keycloak/OIDC configuration | `src/main/resources/application.yml` | Registration and provider names are both `keycloak`; secrets come from environment values. |
| Public/authenticated home UI | `src/main/resources/templates/index.html` | Login, profile, and logout controls. |
| Profile UI | `src/main/resources/templates/profile.html` | Displays identity fields, roles, authorities, and pretty JSON claims. |
| Admin UI | `src/main/resources/templates/admin.html` | Available only to users with the `ADMIN` role. |
| Shared styling | `src/main/resources/static/css/app.css` | Responsive styling for both pages. |
| Maven and dependency versions | `pom.xml` | Java version, Spring Boot parent, dependencies, and executable JAR packaging. |
| Local secret template | `.env.example` | Copy to ignored `.env`; never place real secrets in committed files. |
| Container image | `Dockerfile` | Maven build stage and Eclipse Temurin Java 21 JRE runtime. |
| Local container orchestration | `docker-compose.yml` | Loads `.env` and maps port 8080. |
| Operator/setup documentation | `README.md` | Auth0 dashboard URLs and local/Docker commands. |

## Endpoint and security map

| Path | Access | Owner |
|---|---|---|
| `/` | Public | `HomeController.home` |
| `/profile` | Authenticated | `HomeController.profile` |
| `/admin` | `ROLE_ADMIN` | `HomeController.admin` |
| `/oauth2/authorization/keycloak` | Spring Security OAuth2 login initiation | Spring Security |
| `/login/oauth2/code/keycloak` | OIDC callback | Spring Security |
| `/logout` | Authenticated POST with CSRF token | Spring Security |
| `/error` | Public | Spring Boot error handling |
| `/css/**`, `/js/**` | Public | Static resource handling |

All endpoints not explicitly public require authentication.

## Configuration

Required values:

| Environment variable | Meaning |
|---|---|
| `KEYCLOAK_CLIENT_ID` | Keycloak confidential client ID |
| `KEYCLOAK_CLIENT_SECRET` | Keycloak client secret |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URI |

`application.yml` optionally imports `.env` as a properties file. Operating-system environment variables override values from `.env`. Docker Compose also passes values from `.env` into the container.

Keycloak uses issuer discovery at `${KEYCLOAK_ISSUER_URI}`. Requested scopes are `openid`, `profile`, and `email`.

## Identity and roles

The profile page reads claims from the validated ID token. Display name fallback order is:

1. Full name
2. Preferred username
3. Email
4. Subject

Application roles are read from the Keycloak ID-token or UserInfo claim `realm_access.roles`.
Each role is also mapped to a Spring Security authority with the `ROLE_` prefix while
the original OIDC and scope authorities are retained.

## Change routing

- Change authentication or authorization behavior: inspect `SecurityConfig.java` and `application.yml`.
- Change displayed identity data: inspect `HomeController.java` and `profile.html`.
- Change login/home behavior: inspect `HomeController.java`, `index.html`, and `SecurityConfig.java`.
- Change Keycloak credentials, scopes, issuer, callback, broker, or role behavior: inspect `application.yml`, `KeycloakRealmRoleAuthoritiesMapper.java`, and `README.md`.
- Change dependencies or Java/Spring versions: inspect `pom.xml` and `Dockerfile`.
- Change container execution: inspect `Dockerfile`, `docker-compose.yml`, and `.dockerignore`.

## Verification policy

Do not add test cases. Verify changes with:

```bash
mvn clean package
```

For configuration or authentication changes, additionally perform the manual Keycloak-brokered Auth0 login flow documented in `README.md`.
