# identityproviderPOC

Spring Boot 3.5 application that trusts Keycloak for OpenID Connect authentication and authorization. Keycloak brokers user authentication to Auth0; Spring Boot has no direct Auth0 client configuration and uses only standard Spring Security OAuth2 Client support.

## Architecture and token ownership

```text
Browser
  ↓
Spring Boot
  ↓ OIDC
Keycloak
  ↓ identity brokering
Auth0
  ↓
User
```

- Auth0 authenticates the user.
- Keycloak brokers the external identity, creates the application identity session, and issues tokens.
- Spring Boot trusts only Keycloak. Downstream services must validate Keycloak access tokens.
- ID tokens identify the signed-in user and must not be forwarded to APIs.

## Prerequisites and exact values

- Java 21 and Maven 3.9+
- Keycloak at `http://localhost:8081`
- Auth0 configured as an external OIDC identity provider in Keycloak

| Setting | Value |
|---|---|
| Realm | `identity-poc` |
| Client | `identityprovider-poc` |
| Issuer | `http://localhost:8081/realms/identity-poc` |
| Client authentication | On |
| Standard flow | On |
| Valid redirect URI / callback | `http://localhost:8080/login/oauth2/code/keycloak` |
| Valid post logout redirect URI | `http://localhost:8080/` |
| Login initiation | `http://localhost:8080/oauth2/authorization/keycloak` |

## Keycloak realm-role mapper

Spring Boot reads application roles only from `realm_access.roles`. The claim may not be present in Keycloak's ID token by default. In the Keycloak Admin Console:

1. Open realm **identity-poc**.
2. Open **Clients** → **identityprovider-poc**.
3. Open **Client scopes**, then the client's dedicated scope (or another scope assigned to the client).
4. Select **Add mapper** → **By configuration** → **User Realm Role**.
5. Set **Token Claim Name** to `realm_access.roles`.
6. Set **Multivalued**, **Add to ID token**, **Add to access token**, and **Add to userinfo** to **On**, then save.

Assign realm role `ADMIN` to `pradeep@example.com` and `USER` to `user@example.com`. The application maps these to `ROLE_ADMIN` and `ROLE_USER`. It ignores internal roles such as `offline_access`, `uma_authorization`, and `default-roles-identity-poc` in its displayed/application authorities.

## Environment configuration

Copy `.env.example` to the ignored `.env` file and replace only the client-secret placeholder:

```dotenv
KEYCLOAK_CLIENT_ID=identityprovider-poc
KEYCLOAK_CLIENT_SECRET=replace-with-keycloak-client-secret
KEYCLOAK_ISSUER_URI=http://localhost:8081/realms/identity-poc
```

Operating-system environment variables take precedence over `.env`. Never commit a real client secret.

## Run and package

Run `mvn spring-boot:run`, open `http://localhost:8080`, select **Login with Keycloak**, and then choose the Auth0 identity provider on the Keycloak login page.

Package with `mvn clean package`, then run `java -jar target/identityprovider-poc-0.0.1-SNAPSHOT.jar`. Docker remains available through the existing Dockerfile and Compose configuration.

## Logout and diagnostics

Logout is a CSRF-protected POST. It clears the Spring session and `JSESSIONID`, then uses Keycloak's discovered OIDC end-session endpoint and returns to `http://localhost:8080/`. Spring Boot never constructs or calls an Auth0 logout URL. Auth0 can retain its upstream SSO session, so a later Keycloak login may authenticate silently through Auth0.

OAuth2 DEBUG logging is enabled temporarily for local troubleshooting. Do not log client secrets, authorization codes, access tokens, or refresh tokens. The profile page displays complete ID-token claims for this local POC only; raw token claims should not normally be exposed in a production UI.

## Verification matrix

| User | Authentication | Keycloak role | `/profile` | `/admin` |
|---|---|---|---|---|
| `pradeep@example.com` | Through Auth0 | `ADMIN` | Allowed | Allowed |
| `user@example.com` | Through Auth0 | `USER` | Allowed | 403 Forbidden |

Manual verification:

1. Start Keycloak and this application, then use the login-initiation URL above.
2. Confirm the browser visits Keycloak, delegates to Auth0, and returns through `/login/oauth2/code/keycloak`.
3. For each user, confirm the profile issuer is the Keycloak issuer and `realm_access.roles`, displayed application roles, and Spring authorities match the table.
4. Confirm an unauthenticated request to `/profile` or `/admin` starts login, and confirm the USER account receives 403 at `/admin`.
5. POST logout from the UI; confirm Keycloak logout occurs and the browser returns to `/`.
