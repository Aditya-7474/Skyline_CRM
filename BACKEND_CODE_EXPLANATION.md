# Skyline Real Estate CRM Backend Code Explanation

This document explains the backend source code in simple language.

Project backend path:

```text
backend-java/
```

Technology stack:

- Java 17
- Spring Boot
- Spring Security
- JWT authentication
- MySQL
- JDBC and JPA
- Maven

## 1. Backend Folder Structure

```text
backend-java/
|- pom.xml
|- src/main/resources/
|  |- application.properties
|  `- schema.sql
`- src/main/java/com/skylinecrm/
   |- SkylineCrmApplication.java
   |- controller/
   |- config/
   |- dto/
   |- model/
   |- repository/
   |- security/
   `- service/
```

The backend has four main responsibilities:

1. Receive requests from the frontend.
2. Authenticate the user and check the role.
3. Apply CRM business rules.
4. Read and write data in MySQL.

## 2. `pom.xml`

File:

```text
backend-java/pom.xml
```

This is the Maven configuration file. It downloads and configures the libraries used by the backend.

Important dependencies:

- `spring-boot-starter-web`: REST controllers and HTTP APIs.
- `spring-boot-starter-security`: login security and request protection.
- `spring-boot-starter-validation`: validation of incoming requests.
- `spring-boot-starter-data-jpa`: JPA entities and repositories.
- `spring-boot-starter-jdbc`: direct SQL access through `JdbcTemplate`.
- `mysql-connector-j`: MySQL database driver.
- `jjwt`: creation and validation of JWT tokens.
- `spring-boot-starter-test`: backend testing tools.

The Spring Boot Maven plugin creates the executable backend JAR.

## 3. Main Application File

### `SkylineCrmApplication.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/SkylineCrmApplication.java
```

This is the entry point of the application.

```java
SpringApplication.run(SkylineCrmApplication.class, args);
```

When this method runs, Spring Boot starts:

- Embedded Tomcat.
- REST controllers.
- Security filters.
- Database connections.
- Services and repositories.
- Startup configuration components.

## 4. Controller Folder

Controllers receive frontend requests and return JSON responses.

### `RootController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/RootController.java
```

Provides the API health check:

```text
GET /api/
```

Example response:

```json
{
  "message": "SKYLINE REAL ESTATE CRM API",
  "status": "ok"
}
```

This endpoint is used by the startup batch file to confirm that the backend is ready.

### `AuthController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/AuthController.java
```

Handles authentication and user management.

Important endpoints:

```text
POST   /api/auth/login
GET    /api/auth/me
POST   /api/auth/logout
POST   /api/auth/register
POST   /api/auth/register-public
GET    /api/auth/users
PUT    /api/auth/users/{id}
DELETE /api/auth/users/{id}
```

Important methods:

- `login()`: validates email and password and returns a JWT token.
- `me()`: returns the currently logged-in user.
- `logout()`: confirms logout on the frontend side.
- `register()`: Admin-only user creation.
- `registerPublic()`: public Employee or Agent registration.
- `users()`: Admin-only user list.
- `update()`: Admin-only user update.
- `delete()`: Admin-only user deletion.

The controller delegates actual logic to `AuthService`.

### `ModuleController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/ModuleController.java
```

This is the generic CRUD controller for CRM modules.

Examples:

```text
GET    /api/leads
POST   /api/leads
PUT    /api/leads/{id}
DELETE /api/leads/{id}
```

The same pattern is used for:

```text
leads
followups
units
site-visits
negotiations
bookings
documents
loans
agreements
payments
possessions
support
employees
vendors
purchase-orders
vendor-bills
vendor-payments
petty-cash
salary-runs
attendance
```

Important methods:

- `list()`: reads module records.
- `create()`: creates a new record.
- `get()`: reads one record.
- `update()`: updates a record.
- `delete()`: deletes a record.
- `readRoles()`: decides which roles can read a module.
- `writeRoles()`: decides which roles can create or update a module.
- `deleteRoles()`: decides which roles can delete a module.

The controller calls `RoleUtil.require()` before processing the request.

### `DashboardController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/DashboardController.java
```

Provides calculated dashboard information.

Endpoints:

```text
GET /api/dashboard/kpis
GET /api/dashboard/pipeline
GET /api/dashboard/inventory
GET /api/dashboard/accounts
GET /api/dashboard/monthly-trends
GET /api/dashboard/customer/{leadId}
```

Important methods:

- `kpis()`: counts visits, units, bookings, employees and agents.
- `pipeline()`: groups leads by status and source.
- `inventory()`: groups units by flat type and status.
- `accounts()`: calculates collections, receivables, vendor outstanding, salary and petty cash totals.
- `monthlyTrends()`: calculates booking and collection trends.
- `customer()`: returns related customer activity.

The dashboard values are calculated from MySQL records, not hard-coded frontend values.

### `OperationsController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/OperationsController.java
```

Contains special operations that are more complex than normal CRUD.

Important endpoints:

```text
GET  /api/petty-cash/summary/report
GET  /api/payments/summary/{bookingId}
GET  /api/payroll/salary-runs
POST /api/payroll/salary-runs
PUT  /api/payroll/salary-runs/{id}
POST /api/payroll/generate/{month}
GET  /api/attendance
POST /api/attendance
POST /api/attendance/bulk
```

Unit photo endpoints:

```text
GET    /api/units/{unitId}/photos
POST   /api/units/{unitId}/photos
DELETE /api/units/photos/{photoId}
```

This controller handles:

- Payroll generation.
- Attendance entry.
- Attendance bulk update.
- Payment summaries.
- Petty cash reports.
- Unit photo storage.

### `DocumentUploadController.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/controller/DocumentUploadController.java
```

This controller is only for document file upload and download.

Upload endpoint:

```text
POST /api/documents/{id}/upload?type=pan
```

Valid document types:

```text
pan
aadhaar
passport_photo
address_proof
income_proof
bank_statement
```

Download endpoint:

```text
GET /api/documents/{id}/file/{type}
```

Important methods:

- `upload()`: receives a multipart file and saves it.
- `download()`: returns an already uploaded file.
- `normalizeType()`: validates the document type.
- `validate()`: checks file size, extension and empty files.
- `extension()`: extracts the file extension.
- `deleteExisting()`: removes the old file during replacement.

Allowed extensions:

```text
pdf, png, jpg, jpeg, webp, doc, docx
```

Maximum file size:

```text
10 MB
```

Physical storage folder:

```text
backend-java/uploads/documents/
```

The database stores the uploaded file path, filename, MIME type and file size.

## 5. Service Folder

Services contain reusable business logic called by controllers.

### `AuthService.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/service/AuthService.java
```

Handles:

- Finding users by email.
- Checking active status.
- Verifying encrypted passwords.
- Creating JWT tokens.
- Registering users.
- Updating users.
- Deleting users.
- Returning safe user data.

Important code operations:

```java
users.findByEmailIgnoreCase()
encoder.matches()
jwtService.create()
users.save()
users.deleteById()
```

Passwords are stored as hashes and are never returned to the frontend.

### `CrudService.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/service/CrudService.java
```

Contains common CRM business logic.

Important methods:

- `table()`: maps an API module to a MySQL table.
- `list()`: loads and filters records.
- `get()`: loads one record.
- `create()`: validates and creates a record.
- `update()`: merges and updates a record.
- `delete()`: deletes a record.
- `normalizeBusinessData()`: prepares readable relation data.
- `attachLead()`: copies customer information from a lead.
- `attachUnit()`: copies unit information.
- `attachBooking()`: copies booking information.
- `attachVendorBill()`: copies vendor bill information.

Important business rules:

- A unit cannot have multiple active bookings.
- Confirmed bookings change a unit to `Sold`.
- Cancelled bookings can return a unit to `Available`.
- Vendor payment totals recalculate the vendor bill balance.
- Agent records can be filtered by assignment.

### `RelationalStore.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/service/RelationalStore.java
```

This is the main JDBC persistence class.

Important methods:

```java
all()
query()
search()
count()
find()
findOrNull()
first()
exists()
insert()
update()
upsert()
delete()
```

It uses `JdbcTemplate` for MySQL operations.

It also:

- Defines valid columns for every CRM table.
- Prevents unknown fields from being inserted.
- Creates missing tables.
- Adds missing columns.
- Migrates old JSON records into relational columns.
- Generates internal UUIDs for new records.
- Adds `created_at` and `updated_at` timestamps.

## 6. Security Folder

### `JwtService.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/security/JwtService.java
```

Creates and validates JWT tokens.

The token contains:

- User ID.
- Email.
- Role.
- Name.
- Issued time.
- Expiration time.

Important methods:

```java
create()
parse()
```

### `JwtAuthFilter.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/security/JwtAuthFilter.java
```

Runs before protected requests.

Process:

1. Reads the `Authorization` header.
2. Extracts the Bearer token.
3. Validates the JWT.
4. Reads the user role and identity.
5. Creates the authenticated Spring Security principal.

Expected header:

```text
Authorization: Bearer YOUR_TOKEN
```

### `RoleUtil.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/security/RoleUtil.java
```

Provides role and authentication helpers.

Important methods:

- `normalize()`: converts legacy role names into current roles.
- `principal()`: confirms that the request is authenticated.
- `require()`: checks whether the current role is allowed.

Example:

```java
RoleUtil.require(authentication, "Admin");
```

Unauthorized requests receive HTTP `401`.

Unauthorized roles receive HTTP `403`.

## 7. Config Folder

### `SecurityConfig.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/config/SecurityConfig.java
```

Configures Spring Security.

It defines:

- Public login endpoint.
- Public registration endpoint.
- Protected CRM endpoints.
- JWT filter placement.
- Password encoder.
- CORS configuration.
- Stateless authentication.

### `DemoDataInitializer.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/config/DemoDataInitializer.java
```

Runs after the application starts.

Creates demo records only if they are missing:

- Admin user.
- Employee user.
- Agent user.
- Leads.
- Units.
- Follow-ups.
- Bookings.
- Sales records.
- Vendors.
- Vendor bills and payments.
- Petty cash records.
- Employees.

This class prevents duplicate demo data by checking existing records first.

### `ApiExceptionHandler.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/config/ApiExceptionHandler.java
```

Converts Java exceptions into frontend-friendly JSON.

Example:

```json
{
  "detail": "Unit already has an active booking"
}
```

Handles:

- `ResponseStatusException`.
- Validation errors.
- Unexpected server errors.

## 8. DTO Folder

### `AuthDtos.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/dto/AuthDtos.java
```

Contains request and response data structures for authentication.

Important records include:

- `LoginRequest`.
- `LoginResponse`.
- `UserCreateRequest`.
- `UserUpdateRequest`.
- `UserPublic`.
- `RegistrationRequest`.

DTOs control which fields are accepted and returned by authentication APIs.

## 9. Model Folder

### `UserEntity.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/model/UserEntity.java
```

JPA entity for the `users` table.

Contains:

- ID.
- Email.
- Name.
- Role.
- Phone.
- Password hash.
- Status.
- Created timestamp.
- Updated timestamp.

### `LeadEntity.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/model/LeadEntity.java
```

JPA entity for lead/customer data.

Contains information such as:

- Customer name.
- Mobile.
- Email.
- Budget.
- Lead status.
- Source.
- Notes.

## 10. Repository Folder

### `UserRepository.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/repository/UserRepository.java
```

Spring Data repository for users.

Provides methods such as:

```java
findById()
findByEmailIgnoreCase()
findAll()
save()
deleteById()
```

### `LeadRepository.java`

File:

```text
backend-java/src/main/java/com/skylinecrm/repository/LeadRepository.java
```

Spring Data repository for `LeadEntity`.

It provides standard JPA CRUD operations for leads. The active generic CRM modules mainly use `RelationalStore` for database operations.

## 11. Resources Folder

### `application.properties`

File:

```text
backend-java/src/main/resources/application.properties
```

Contains runtime configuration:

```properties
server.port=8001
spring.datasource.url=...
spring.datasource.username=...
spring.datasource.password=...
app.jwt.secret=...
app.jwt.expiration-minutes=1440
```

Default database:

```text
skyline_crm
```

### `schema.sql`

File:

```text
backend-java/src/main/resources/schema.sql
```

Defines the MySQL tables used by the CRM:

```text
users
leads
follow_ups
units
site_visits
negotiations
bookings
documents
loans
agreements
payments
possessions
support_tickets
employees
attendance
salary_runs
vendors
purchase_orders
vendor_bills
vendor_payments
petty_cash_entries
unit_photos
```

The `RelationalStore` also checks for missing tables and columns when the backend starts.

## 12. Runtime Folders

### `backend-java/uploads/documents/`

Created automatically when the first document is uploaded.

Uploaded files are saved physically in this folder. The `documents` table stores their metadata.

### `backend-java/target/`

Generated Maven build folder.

It contains compiled classes, dependencies and the executable JAR.

Do not edit files inside `target` manually.

## 13. Complete Request Flow

### Normal CRUD request

```text
Frontend
  -> ModuleController
  -> JwtAuthFilter
  -> RoleUtil
  -> CrudService
  -> RelationalStore
  -> MySQL
  -> JSON response
```

### Login request

```text
Frontend
  -> AuthController
  -> AuthService
  -> UserRepository
  -> PasswordEncoder
  -> JwtService
  -> JWT response
```

### Document upload request

```text
Frontend file input
  -> DocumentUploadController
  -> File saved in uploads/documents/
  -> RelationalStore updates documents table
  -> Upload response
```

## 14. Running the Backend

From the project root:

```bat
cd /d "C:\Users\adity\OneDrive\Desktop\Sai_Vandhan_CRM\Sai_vandhan_CRM-main"
start_backend.bat
```

Backend health check:

```text
http://localhost:8001/api/
```

To run the complete project:

```bat
cd /d "C:\Users\adity\OneDrive\Desktop\Sai_Vandhan_CRM\Sai_vandhan_CRM-main"
start_crm.bat
```

Frontend login page:

```text
http://localhost:3001/login.html
```
