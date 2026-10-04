# Skyline CRM Quick Reference

## Start

```powershell
cd backend-java
mvn spring-boot:run
```

```powershell
python -m http.server 3001 --directory frontend-html
```

Frontend: `http://localhost:3001`  
Backend: `http://localhost:8001/api/`  
MySQL database: `skyline_crm`

## Roles

- Admin: complete access
- Employee: workflow, vendor payment, and petty cash access
- Agent: enquiry, lead, follow-up, booking, and sales access

## Demo Login

`admin@saivandan.com / Admin@123`  
`employee@saivandan.com / Employee@123`  
`agent@saivandan.com / Agent@123`
