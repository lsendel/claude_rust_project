# Local Development Quickstart Guide
**Multi-Tenant SaaS Platform - Get Started in 15 Minutes**

## Overview

This guide will help you set up and run the Multi-Tenant SaaS Platform on your local machine for development and testing.

## Prerequisites

### Required Software

| Tool | Version | Installation |
|------|---------|--------------|
| **Java JDK** | 21 LTS | https://adoptium.net/ |
| **Node.js** | 18+ | https://nodejs.org/ |
| **Docker** | Latest | https://www.docker.com/get-started |
| **Maven** | 3.9+ | https://maven.apache.org/install.html |
| **PostgreSQL** | 15+ | Via Docker (see below) |

### Optional Tools

- **AWS CLI** (for cloud deployment): https://aws.amazon.com/cli/
- **Terraform** (for infrastructure): https://www.terraform.io/downloads
- **GitHub CLI** (for PR management): https://cli.github.com/

## Quick Setup (15 Minutes)

### Step 1: Clone the Repository (1 min)

```bash
git clone https://github.com/yourusername/saas-platform.git
cd saas-platform
```

### Step 2: Start PostgreSQL Database (2 min)

Using Docker Compose:

```bash
# Start PostgreSQL container
docker-compose up -d postgres

# Verify it's running
docker ps | grep postgres
```

**Connection Details**:
- Host: `localhost`
- Port: `5432`
- Database: `saasplatform`
- Username: `postgres`
- Password: `postgres`

Alternative - Use existing PostgreSQL:

```bash
# Create database
createdb saasplatform

# Update backend/src/main/resources/application-local.yml with your credentials
```

### Step 3: Set Up Backend (5 min)

```bash
cd backend

# Install dependencies and run tests
mvn clean install

# Run Flyway migrations (creates database schema)
mvn flyway:migrate

# Start Spring Boot application
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Or use your IDE:
# - IntelliJ IDEA: Right-click on SaasPlatformApplication.java → Run
# - VS Code: Use Spring Boot Dashboard extension
```

**Backend should now be running at**: http://localhost:8080

**Health Check**: http://localhost:8080/actuator/health
Expected: `{"status":"UP"}`

### Step 4: Set Up Frontend (5 min)

```bash
# Open a new terminal
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

**Frontend should now be running at**: http://localhost:5173

### Step 5: Verify Setup (2 min)

1. **Check Backend API**:
   ```bash
   curl http://localhost:8080/actuator/health
   # Expected: {"status":"UP"}
   ```

2. **Check Database Connection**:
   ```bash
   curl http://localhost:8080/actuator/health/db
   # Expected: {"status":"UP"}
   ```

3. **Open Frontend**:
   - Navigate to http://localhost:5173
   - You should see the sign-up page

**🎉 Success!** You now have a fully functional local development environment.

## Development Workflow

### Running Backend Tests

```bash
cd backend

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TenantServiceTest

# Run with coverage report
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Running Frontend Tests

```bash
cd frontend

# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage

# View coverage report
open coverage/lcov-report/index.html
```

### Database Management

#### View Current Schema

```bash
# Using psql
psql -h localhost -U postgres -d saasplatform -c "\dt"

# Using Docker
docker exec -it saasplatform-postgres psql -U postgres -d saasplatform -c "\dt"
```

#### Reset Database

```bash
cd backend

# Drop and recreate schema
mvn flyway:clean
mvn flyway:migrate

# Or drop the entire database
dropdb saasplatform
createdb saasplatform
mvn flyway:migrate
```

#### Add New Migration

```bash
cd backend/src/main/resources/db/migration

# Create new migration file (version number must be sequential)
# Format: V<version>__<description>.sql
# Example: V002__add_projects_table.sql

# Run migration
mvn flyway:migrate
```

### Hot Reload

#### Backend (Spring Boot DevTools)

Spring Boot DevTools is enabled in development:
- Changes to Java files will trigger automatic restart
- Changes to resources (application.yml) will reload configuration
- Restart time: ~5 seconds

#### Frontend (Vite HMR)

Vite Hot Module Replacement (HMR) is enabled:
- Changes to React components update instantly
- No page refresh required
- State is preserved when possible

## Configuration

### Backend Configuration

Edit `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/saasplatform
    username: postgres
    password: postgres

  security:
    oauth2:
      client:
        registration:
          cognito:
            client-id: ${COGNITO_CLIENT_ID:your-client-id}
            client-secret: ${COGNITO_CLIENT_SECRET:your-client-secret}

server:
  port: 8080

logging:
  level:
    com.platform.saas: DEBUG
```

### Frontend Configuration

Create `frontend/.env.local`:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=10000

# AWS Cognito Configuration (optional for local dev)
VITE_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
VITE_COGNITO_CLIENT_ID=your-client-id
VITE_COGNITO_DOMAIN=your-domain.auth.us-east-1.amazoncognito.com

# Feature Flags
VITE_ENABLE_OAUTH=false
VITE_ENABLE_ANALYTICS=false
```

## Testing Multi-Tenant Functionality Locally

### Option 1: Edit /etc/hosts (Recommended)

```bash
# Add test subdomains
sudo nano /etc/hosts

# Add these lines:
127.0.0.1 tenant1.localhost
127.0.0.1 tenant2.localhost
127.0.0.1 admin.localhost
```

Now access:
- http://tenant1.localhost:5173 (Tenant 1)
- http://tenant2.localhost:5173 (Tenant 2)
- http://admin.localhost:5173 (Admin tenant)

### Option 2: Use Query Parameter (Quick Test)

Access with query parameter: http://localhost:5173?tenant=tenant1

The frontend will extract the tenant from the query parameter.

### Option 3: Use ngrok (External Testing)

```bash
# Install ngrok
brew install ngrok  # macOS
# or download from https://ngrok.com/

# Expose frontend
ngrok http 5173

# Expose backend
ngrok http 8080

# Use the ngrok URLs for testing
```

## Common Development Tasks

### Create a New REST Endpoint

1. **Add Entity** (if needed):
   ```java
   @Entity
   @Table(name = "widgets")
   public class Widget {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @Column(name = "tenant_id", nullable = false)
       private Long tenantId;

       // ... other fields
   }
   ```

2. **Add Repository**:
   ```java
   public interface WidgetRepository extends JpaRepository<Widget, Long> {
       List<Widget> findByTenantId(Long tenantId);
   }
   ```

3. **Add Service**:
   ```java
   @Service
   public class WidgetService {
       @Autowired
       private WidgetRepository widgetRepository;

       public List<Widget> getAllWidgets() {
           Long tenantId = TenantContext.getCurrentTenantId();
           return widgetRepository.findByTenantId(tenantId);
       }
   }
   ```

4. **Add Controller**:
   ```java
   @RestController
   @RequestMapping("/api/widgets")
   public class WidgetController {
       @Autowired
       private WidgetService widgetService;

       @GetMapping
       public List<Widget> getWidgets() {
           return widgetService.getAllWidgets();
       }
   }
   ```

5. **Test the endpoint**:
   ```bash
   curl http://localhost:8080/api/widgets \
     -H "X-Tenant-Subdomain: tenant1"
   ```

### Add a New Frontend Page

1. **Create Page Component**:
   ```tsx
   // frontend/src/pages/WidgetsPage.tsx
   export const WidgetsPage: React.FC = () => {
       return (
           <div>
               <h1>Widgets</h1>
               {/* Your content */}
           </div>
       );
   };
   ```

2. **Add Route**:
   ```tsx
   // frontend/src/App.tsx
   import { WidgetsPage } from './pages/WidgetsPage';

   <Route path="/widgets" element={<WidgetsPage />} />
   ```

3. **Add Navigation Link**:
   ```tsx
   // frontend/src/components/Navigation.tsx
   <Link to="/widgets">Widgets</Link>
   ```

## Troubleshooting

### Backend Won't Start

**Problem**: `Port 8080 already in use`

**Solution**:
```bash
# Find and kill process using port 8080
lsof -ti:8080 | xargs kill -9

# Or change the port in application-local.yml
server.port: 8081
```

**Problem**: `Cannot connect to database`

**Solution**:
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Restart database
docker-compose restart postgres

# Check credentials in application-local.yml
```

### Frontend Won't Start

**Problem**: `EADDRINUSE: address already in use`

**Solution**:
```bash
# Kill process on port 5173
lsof -ti:5173 | xargs kill -9

# Or use a different port
npm run dev -- --port 3000
```

**Problem**: `Module not found` errors

**Solution**:
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

### Database Issues

**Problem**: Flyway migration failed

**Solution**:
```bash
# Check migration history
mvn flyway:info

# Repair checksum mismatches
mvn flyway:repair

# Clean and re-migrate (⚠️ DESTROYS DATA)
mvn flyway:clean
mvn flyway:migrate
```

**Problem**: Can't connect to local PostgreSQL

**Solution**:
```bash
# Check PostgreSQL is listening
psql -h localhost -U postgres -c "SELECT version();"

# Check connection parameters
cat backend/src/main/resources/application-local.yml | grep datasource -A 3
```

### Tests Failing

**Problem**: Integration tests fail due to database connection

**Solution**:
```bash
# Use Testcontainers (already configured)
# Tests will start a temporary PostgreSQL container

# Or skip tests temporarily
mvn clean install -DskipTests
```

## IDE Setup

### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `pom.xml` (backend) or `package.json` (frontend)
   - Enable "Auto-Import" for Maven

2. **Configure Java SDK**:
   - File → Project Structure → Project SDK → Add JDK → Select Java 21

3. **Run Configuration**:
   - Run → Edit Configurations → Add New → Spring Boot
   - Main class: `com.platform.saas.SaasPlatformApplication`
   - Active profiles: `local`

4. **Enable Annotation Processing**:
   - Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### VS Code

1. **Install Extensions**:
   - Java Extension Pack
   - Spring Boot Extension Pack
   - ESLint
   - Prettier
   - Volar (Vue.js/React)

2. **Configure Java**:
   - Settings → Java: Home → Set to Java 21 path

3. **Run Backend**:
   - Use Spring Boot Dashboard extension
   - Or use terminal: `mvn spring-boot:run`

4. **Run Frontend**:
   - Terminal → `npm run dev`

## Performance Tips

### Backend

1. **Enable JVM Options**:
   ```bash
   export MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"
   mvn spring-boot:run
   ```

2. **Use Spring Boot DevTools**:
   - Already included in pom.xml
   - Enables faster restarts

3. **Disable Unnecessary Features**:
   ```yaml
   # application-local.yml
   spring:
     jpa:
       show-sql: false  # Disable SQL logging for performance
   ```

### Frontend

1. **Use Vite's Dependency Pre-Bundling**:
   ```bash
   # Clear Vite cache if dependencies change
   rm -rf frontend/node_modules/.vite
   ```

2. **Optimize Build**:
   ```bash
   # Production build
   npm run build

   # Preview production build locally
   npm run preview
   ```

## Additional Resources

- **Backend API Documentation**: http://localhost:8080/swagger-ui.html (if Swagger is configured)
- **Database Schema**: `backend/src/main/resources/db/migration/`
- **Frontend Components**: `frontend/src/components/`
- **Deployment Guide**: `infrastructure/DEPLOYMENT.md`
- **Architecture Documentation**: `specs/001-saas-platform/plan.md`

## Getting Help

- **Issue Tracker**: https://github.com/yourusername/saas-platform/issues
- **Documentation**: See `docs/` directory
- **Stack Overflow**: Tag your questions with `saas-platform`
- **Team Chat**: #saas-platform channel

---

**Happy Coding!** 🚀

For production deployment, see `infrastructure/DEPLOYMENT.md`.
