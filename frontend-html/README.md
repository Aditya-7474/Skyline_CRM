# Skyline Real Estate CRM HTML Frontend

This frontend is implemented with HTML5, CSS3, and vanilla JavaScript. It uses the existing Spring Boot backend at `http://localhost:8001`.

## Start

From the project root in VS Code PowerShell:

```powershell
python -m http.server 3001 --directory frontend-html
```

Open `http://localhost:3001/login.html`.

For one-click startup, double-click `start_crm.bat` in the project root. It waits for Spring Boot and the HTML server before opening the login page.

The backend and MySQL database are unchanged. Start the backend separately with the existing Spring Boot command from `backend-java`.
