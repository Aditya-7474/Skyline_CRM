# Skyline Real Estate CRM

HTML5, CSS3, Vanilla JavaScript, Spring Boot, MySQL, and JWT real-estate CRM.

## Run

```powershell
cd backend-java
mvn spring-boot:run
```

Or run the built application:

```powershell
cd backend-java
java -jar target\skyline-crm-backend-1.0.0.jar --server.port=8001
```

Start the HTML frontend in a second terminal:

```powershell
python -m http.server 3001 --directory frontend-html
```

Open `http://localhost:3001`.

## Demo Accounts

| Role | Email | Password |
|---|---|---|
| Admin | admin@saivandan.com | Admin@123 |
| Employee | employee@saivandan.com | Employee@123 |
| Agent | agent@saivandan.com | Agent@123 |

The Spring Boot backend preserves the `/api` routes, JSON field names, JWT claims, and role permissions. MySQL uses database `skyline_crm`.
