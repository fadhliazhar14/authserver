# Database Setup and Troubleshooting Guide

## 🗄️ MySQL Database Setup

### Prerequisites
- MySQL 8.0+ installed and running
- Database user with appropriate privileges

### Quick Setup Commands

```sql
-- Connect to MySQL as root
mysql -u root -p

-- Create database for development
CREATE DATABASE authserver_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create database for production (optional)
CREATE DATABASE authserver CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user and grant privileges (optional - can use root for dev)
CREATE USER 'authserver_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON authserver_dev.* TO 'authserver_user'@'localhost';
GRANT ALL PRIVILEGES ON authserver.* TO 'authserver_user'@'localhost';
FLUSH PRIVILEGES;
```

---

## 🔧 Common Connection Issues & Solutions

### Issue 1: "Public Key Retrieval is not allowed"
**Error:** `JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Public Key Retrieval is not allowed]`

**Solution:** ✅ Already fixed in configuration files
- Added `allowPublicKeyRetrieval=true` to connection URLs
- This allows the client to automatically request public keys from the server

### Issue 2: SSL Connection Warnings
**Error:** SSL warnings or connection refused

**Solution:** ✅ Already configured
- Added `useSSL=false` for local development
- For production, configure SSL properly

### Issue 3: Character Encoding Issues
**Error:** Character encoding problems with special characters

**Solution:** ✅ Already configured
- Added `useUnicode=true&characterEncoding=utf8`
- Database created with `utf8mb4` charset

### Issue 4: Timezone Issues
**Error:** Server timezone errors

**Solution:** ✅ Already configured  
- Added `serverTimezone=UTC` to handle timezone properly

---

## 🚀 Running the Application

### Development Mode (Recommended for Testing)

```bash
# Method 1: Using Maven with profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Method 2: Set environment variable
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Method 3: Using IDE
# Set active profile to "dev" in your IDE run configuration
```

### Custom Database Configuration

If you need to use different database settings, set these environment variables:

```bash
# For development profile
export MYSQL_DEV_URL="jdbc:mysql://localhost:3306/your_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8"
export MYSQL_DEV_USERNAME="your_username"
export MYSQL_DEV_PASSWORD="your_password"

# Then run the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🔍 Debugging Connection Issues

### Check MySQL Status
```bash
# Check if MySQL is running
sudo systemctl status mysql
# or on macOS with Homebrew
brew services list | grep mysql

# Check MySQL version
mysql --version
```

### Test Manual Connection
```bash
# Test connection manually
mysql -h localhost -P 3306 -u root -p

# If successful, try creating the database
CREATE DATABASE authserver_dev;
USE authserver_dev;
```

### Check Application Logs
Look for these log messages when starting the application:

```
✅ Success indicators:
- "HikariPool-1 - Starting..."
- "HikariPool-1 - Start completed"
- "Started AuthServer Application"

❌ Error indicators:
- "JDBCConnectionException"
- "Communications link failure"
- "Access denied for user"
```

---

## 📋 Verification Steps

Once the application starts successfully, verify:

### 1. Health Check
```bash
curl http://localhost:8080/actuator/health
```
Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

### 2. Database Tables Created
```sql
-- Check if tables were created automatically
USE authserver_dev;
SHOW TABLES;

-- Expected tables:
-- - jwk_keys
-- - oauth2_authorization
-- - oauth2_authorization_consent
-- - oauth2_registered_client
```

### 3. API Endpoints Working
```bash
# Test public endpoint
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{"clientName": "Test Client"}'

# Test API documentation
curl http://localhost:8080/swagger-ui.html
```

---

## ⚡ Quick Fix Commands

If you encounter issues, try these quick fixes:

```bash
# 1. Clean and rebuild
mvn clean compile

# 2. Check for port conflicts
lsof -i :8080
# Kill process if needed: kill -9 <PID>

# 3. Reset MySQL connection
sudo systemctl restart mysql

# 4. Check database exists
mysql -u root -p -e "SHOW DATABASES LIKE 'authserver%';"

# 5. Create database if missing
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS authserver_dev;"
```

---

## 🔒 Production Database Configuration

For production deployment:

```bash
# Set production environment variables
export SPRING_PROFILES_ACTIVE=prod
export MYSQL_PROD_URL="jdbc:mysql://your-prod-server:3306/authserver?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true"
export MYSQL_PROD_USERNAME="prod_user"
export MYSQL_PROD_PASSWORD="secure_prod_password"
export ADMIN_API_KEY="your-very-secure-api-key"

# Run application
mvn spring-boot:run
```

---

## 📞 Support

If you continue to experience database connection issues:

1. **Check MySQL Error Logs**: Usually located at `/var/log/mysql/error.log`
2. **Verify Network Connectivity**: Ensure MySQL is accepting connections on port 3306
3. **Review User Privileges**: Make sure the database user has sufficient privileges
4. **Test with Different User**: Try connecting with a different MySQL user
5. **Check Firewall Settings**: Ensure port 3306 is not blocked

The MySQL connection configuration has been updated to handle common authentication and connection issues. Try running the application again with the dev profile.