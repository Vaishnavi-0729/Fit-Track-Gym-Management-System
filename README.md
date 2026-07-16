# javaproject – Gym Membership Management System

Built on top of the exact project you generated from Spring Initializr (group `com.example`,
artifact `javaproject`, Spring Boot **4.1.0**).

- **Backend:** Java 21, Spring Boot 4 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`,
  `spring-boot-starter-security`, `spring-boot-starter-thymeleaf`, DevTools)
- **Frontend:** HTML, CSS, vanilla JavaScript, served as static resources and talking to the backend
  purely via REST + `fetch`
- **Database:** MySQL

## ⚠️ Important — things changed from your original zip
1. **Java version bumped 17 → 21.** Spring Boot 4 requires Java 21+ as a minimum; the project won't
   build on 17. Make sure `java -version` shows 21 (or newer) before running.
2. **Added two dependencies your Initializr config didn't include, because the code needs them:**
   - `spring-boot-starter-validation` — for the `@NotBlank`/`@Email`/`@Valid` annotations used on
     the entities and controllers
   - `lombok` — for the `@Data`/`@RequiredArgsConstructor` boilerplate reduction on entities/services
3. Kept everything else exactly as your Initializr project had it: `spring-boot-starter-webmvc`
   (Boot 4's renamed `-web` starter), the per-module test starters, `mysql-connector-j`, DevTools, and
   `thymeleaf-extras-springsecurity6`.

## Features
- Secure login (Spring Security 7, session-based, BCrypt-hashed passwords)
- Register / edit / delete gym members (name, email, phone, address, gender, DOB)
- Assign a membership plan to each member; joining date and end date are tracked automatically
- Create / edit / delete membership plans (name, duration in months, price, description)
- Search members by name/email/phone, filter by status (Active / Expired / Cancelled)
- Renew or cancel a membership with one click
- Dashboard with live counts (total, active, expired members, number of plans)

## 1. Prerequisites
- **JDK 21+** (required by Spring Boot 4 / Spring Framework 7)
- Maven (or just use the included `./mvnw` wrapper — no local Maven install needed)
- MySQL 8.x running locally (or reachable)

## 2. Database setup
`spring.jpa.hibernate.ddl-auto=update` creates the tables for you, and the datasource URL includes
`createDatabaseIfNotExist=true`. You only need MySQL itself running and reachable. If you'd rather
create the database yourself first:
```sql
CREATE DATABASE gym_management_db;
```

## 3. Configure credentials
Edit `src/main/resources/application.properties` and set your MySQL username/password:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_management_db?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

## 4. Run the application
Using the wrapper (recommended, matches how Initializr set the project up):
```bash
./mvnw spring-boot:run
```
Or build a jar and run it:
```bash
./mvnw clean package -DskipTests
java -jar target/javaproject-0.0.1-SNAPSHOT.jar
```

The app starts on **http://localhost:8080**.

## 5. Log in
On first startup a default admin account and four sample membership plans are seeded automatically:

- **Username:** `admin`
- **Password:** `admin123`

You'll see this printed in the console log too. Change this password (or add new users directly in
the `app_users` table, hashing the password with BCrypt) before using this in anything resembling
production.

## 6. Project structure
```
src/main/java/com/example/javaproject
 ├── config/          Security config + default data seeding
 ├── controller/       REST controllers (members, plans) + view/login controller + error handling
 ├── dto/              Shared error/response types
 ├── model/            JPA entities: Member, MembershipPlan, AppUser, MembershipStatus
 ├── repository/       Spring Data JPA repositories
 └── service/          Business logic

src/main/resources
 ├── templates/        Thymeleaf: login.html, index.html (app shell)
 ├── static/css/        style.css
 ├── static/js/         script.js (all frontend logic — fetch calls to /api/**)
 └── application.properties
```

## 7. REST API reference

| Method | Endpoint                    | Description                          |
|--------|------------------------------|---------------------------------------|
| GET    | `/api/members`                | List all members (supports `?search=` and `?status=`) |
| GET    | `/api/members/{id}`           | Get one member |
| POST   | `/api/members`                | Register a new member |
| PUT    | `/api/members/{id}`           | Update a member |
| DELETE | `/api/members/{id}`           | Delete a member |
| POST   | `/api/members/{id}/renew`     | Renew membership (resets joining date to today) |
| POST   | `/api/members/{id}/cancel`    | Cancel a membership |
| GET    | `/api/members/stats`          | Dashboard counts |
| GET    | `/api/plans`                   | List all membership plans |
| POST   | `/api/plans`                   | Create a plan |
| PUT    | `/api/plans/{id}`              | Update a plan |
| DELETE | `/api/plans/{id}`              | Delete a plan (blocked if members are assigned) |

All `/api/**` endpoints require an authenticated session — log in via `/login` first; the browser
handles the session cookie automatically once you're logged into the dashboard.

## Notes on Spring Boot 4 / Spring Security 7 specifics
- CSRF protection is on by default in Spring Security 7 for everything, including what used to be
  more lenient defaults in Boot 3. This app is session/cookie-based (not a stateless token API), so
  CSRF is left **enabled** — `index.html` exposes the token via a meta tag and `script.js` sends it
  as a header on every `fetch` call. If you ever convert this to a stateless JWT-based API, you'd
  disable CSRF and add a different auth filter instead.
- `authorizeHttpRequests()`/`formLogin()` lambda DSL is used throughout — the older
  `authorizeRequests()`/chained `.and()` style is removed entirely in Spring Security 7.

## Notes / next steps you may want
- Passwords are BCrypt-hashed and DB schema auto-updates in dev; switch `ddl-auto` to `validate` and
  add Flyway/Liquibase migrations before using this in production.
- Add pagination if your member list grows large.
- Add role-based views (e.g., STAFF vs ADMIN) using the `role` field already on `AppUser`.
- Add email/SMS reminders before a membership expires.
