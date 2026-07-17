# Spring Boot + Auth0 RBAC POC

## 1. Purpose

This document captures the complete setup and verification of a Spring
Boot application integrated with Auth0 using OpenID Connect (OIDC) and
Role-Based Access Control (RBAC).

The POC demonstrates:

-   Spring Boot 3 + Spring Security
-   Auth0 Universal Login
-   OpenID Connect authentication
-   Auth0 Database Users
-   Role-Based Access Control (ADMIN / USER)
-   Spring Security role mapping
-   Protected endpoints

------------------------------------------------------------------------

## 2. Application Details

  Item          Value
  ------------- -------------------------------
  Application   identityproviderPOC
  Framework     Spring Boot
  Java          21
  Build Tool    Maven
  Security      Spring Security OAuth2 Client
  Protocol      OpenID Connect

------------------------------------------------------------------------

## 3. Auth0 Configuration

### Application

-   Application Type: Regular Web Application

### Callback URL

``` text
http://localhost:8080/login/oauth2/code/auth0
```

### Logout URL

``` text
http://localhost:8080/
```

### Web Origin

``` text
http://localhost:8080
```

------------------------------------------------------------------------

## 4. Users

  User                  Role
  --------------------- -------
  pradeep@example.com   ADMIN
  user@example.com      USER

------------------------------------------------------------------------

## 5. Roles

Created roles:

-   ADMIN
-   USER

Assigned:

``` text
pradeep@example.com → ADMIN
user@example.com    → USER
```

------------------------------------------------------------------------

## 6. Post Login Action

A Post Login Action adds Auth0 roles into the ID Token.

Custom claim:

``` text
https://identityproviderpoc.example.com/roles
```

Verified token:

``` json
{
  "https://identityproviderpoc.example.com/roles": [
    "ADMIN"
  ]
}
```

------------------------------------------------------------------------

## 7. Spring Security Mapping

Mapped Auth0 roles into Spring authorities.

``` text
ADMIN
    ↓
ROLE_ADMIN

USER
    ↓
ROLE_USER
```

Verified authorities:

``` text
OIDC_USER
SCOPE_openid
SCOPE_profile
SCOPE_email
ROLE_ADMIN
```

------------------------------------------------------------------------

## 8. Endpoint Authorization

  Endpoint   Access
  ---------- ------------------------
  /          Public
  /profile   Any authenticated user
  /admin     ADMIN only

------------------------------------------------------------------------

## 9. Verification Matrix

  User                          /profile     /admin
  ----------------------------- ------------ ------------------
  pradeep@example.com (ADMIN)   ✅ Allowed   ✅ Allowed
  user@example.com (USER)       ✅ Allowed   ❌ 403 Forbidden

------------------------------------------------------------------------

## 10. Current POC Status

Completed:

-   [x] Auth0 development tenant created
-   [x] Regular Web Application created
-   [x] Callback URL configured
-   [x] Logout URL configured
-   [x] Web origin configured
-   [x] Spring Boot OIDC login working
-   [x] Auth0 users created
-   [x] ADMIN role created
-   [x] USER role created
-   [x] Roles assigned
-   [x] Post Login Action created
-   [x] Action deployed
-   [x] Action attached to Login Flow
-   [x] Roles added to ID Token
-   [x] Roles displayed on Profile page
-   [x] Spring Security role mapping verified
-   [x] ROLE_ADMIN verified
-   [x] /admin accessible for ADMIN
-   [x] /profile accessible for USER
-   [x] /admin returns HTTP 403 for USER

Project Status:

**Authentication and Role-Based Access Control are fully implemented and
verified end-to-end.**

------------------------------------------------------------------------

## 11. Possible Future Enhancements

-   JWT-protected REST APIs
-   Method-level security (`@PreAuthorize`)
-   Role-based UI navigation
-   Custom permissions
-   Social login providers
-   Integration tests
-   Docker & Docker Compose
-   Kubernetes deployment
-   OpenTelemetry integration
-   Audit logging
