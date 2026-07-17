# identityproviderPOC

Spring Boot 3.5 application that authenticates users with Auth0 using OpenID Connect Authorization Code Flow. It uses Spring Security OAuth2 Client only; the Auth0 Java SDK is not used.

## Prerequisites

- Java 21
- Maven 3.9+
- An Auth0 tenant and Regular Web Application
- Docker and Docker Compose (optional)

## Auth0 configuration

Create or open a **Regular Web Application** in the Auth0 Dashboard and configure:

| Setting | Value |
|---|---|
| Allowed Callback URLs | `http://localhost:8080/login/oauth2/code/auth0` |
| Allowed Logout URLs | `http://localhost:8080/` |
| Allowed Web Origins | `http://localhost:8080` |

The application performs local Spring Security logout and returns to `/`. It does not terminate the Auth0 single sign-on session. If Auth0 roles should appear on the profile page, add them to the ID token with an Auth0 Post Login Action under the namespaced `https://identityproviderpoc.example.com/roles` claim. These values are mapped to Spring Security authorities with the `ROLE_` prefix.

## Environment configuration

Copy the committed example file and replace the placeholders:

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Set these values in `.env`:

```dotenv
AUTH0_DOMAIN=your-tenant.us.auth0.com
AUTH0_CLIENT_ID=your-client-id
AUTH0_CLIENT_SECRET=your-client-secret
```

Use the Auth0 domain without `https://` and without a trailing slash. The application loads `.env` as an optional local Spring configuration file. Environment variables with the same names are also supported and take precedence. `.env` is ignored by Git and excluded from the Docker build context.

## Run locally

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` and select **Login with Auth0**.

## Package

```bash
mvn clean package
java -jar target/identityprovider-poc-0.0.1-SNAPSHOT.jar
```

## Docker

Build the image:

```bash
docker build -t identityprovider-poc .
```

Run the image directly:

```bash
docker run --rm --env-file .env -p 8080:8080 identityprovider-poc
```

Or build and run with Docker Compose (which loads `.env`):

```bash
docker compose up --build
```

Stop it with `docker compose down`.

## Security notes

- `/`, favicon, and static asset paths are public; `/profile` is authenticated.
- Login uses OIDC discovery from the configured Auth0 `issuer-uri`.
- Logout requires a CSRF-protected POST request.
- Client secrets are supplied only through environment configuration and must never be committed.
