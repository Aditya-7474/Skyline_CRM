# Skyline CRM Architecture

## Stack

- HTML5/CSS3/Vanilla JavaScript frontend in `frontend-html`
- Spring Boot API in `backend-java`
- MySQL 8 database `skyline_crm`
- JWT bearer authentication

## Java Packages

- `config`: MySQL schema, security, CORS, errors, and demo-user initialization
- `controller`: auth, CRM modules, operations, and dashboard
- `dto`: authentication request and response records
- `model`: JPA user entity
- `repository`: user repository
- `security`: JWT filter, token service, and role checks
- `service`: authentication and relational CRUD services

## Compatibility

The API keeps the existing `/api` paths, payload field names, response shapes, relational table names, and Admin/Employee/Agent permissions. The HTML frontend uses the same contracts without a Node runtime.
