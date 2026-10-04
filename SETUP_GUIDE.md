# Skyline Real Estate CRM Setup

## Requirements

- Java 17+
- Maven 3.9+
- MySQL 8

## MySQL

The Java backend uses:

```env
MYSQL_URL=jdbc:mysql://localhost:3306/skyline_crm
MYSQL_USERNAME=root
MYSQL_PASSWORD=root
```

## Start Backend

```powershell
cd backend-java
mvn spring-boot:run
```

The API runs at `http://localhost:8001`; health check: `http://localhost:8001/api/`.

## Start HTML Frontend

```powershell
python -m http.server 3001 --directory frontend-html
```

The vanilla JavaScript client points to `http://localhost:8001`.

## Login

- Admin: `admin@saivandan.com` / `Admin@123`
- Employee: `employee@saivandan.com` / `Employee@123`
- Agent: `agent@saivandan.com` / `Agent@123`
