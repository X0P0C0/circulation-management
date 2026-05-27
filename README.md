# CM - Circulation Management System

Accessory/parts circulation management system for internal use.

## Tech Stack

- Backend: Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL 8
- PC Frontend: Vue 3 + Element Plus + Vite
- H5 Frontend: Vue 3 + Vant 4 + html5-qrcode
- Auth: JWT (BCrypt password hashing)

## Quick Start

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 20+ (fnm recommended)
- Git

### 1. Database Setup

```bash
mysql -u root -p --default-character-set=utf8mb4 < cm-backend/sql/init.sql
```

### 2. Backend Configuration

Copy the example config and edit with your database credentials:

```bash
cp cm-backend/src/main/resources/application-dev.example.yml cm-backend/src/main/resources/application-dev.yml
```

Edit `application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cm_db?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: "your_password"
```

### 3. One-Click Start (Windows)

Double-click `setup.bat` (first time only), then `start.bat` to launch all services.

| Service | URL |
|---------|-----|
| Backend API | http://localhost:8080 |
| PC Frontend | http://localhost:3100 |
| H5 Frontend | http://localhost:3101 |

Default login: `admin` / `admin123`

### Manual Start

```bash
# Backend
cd cm-backend
mvn spring-boot:run

# PC Frontend (new terminal)
cd cm-pc
npm install && npm run dev

# H5 Frontend (new terminal)
cd cm-h5
npm install && npm run dev
```

## Project Structure

```
circulation-management/
├── cm-backend/              # Spring Boot backend
│   ├── src/main/java/com/cm/
│   │   ├── config/          # Configuration
│   │   ├── common/          # Result, Exception, Constants
│   │   ├── entity/          # Database entities
│   │   ├── dto/             # Request DTOs
│   │   ├── vo/              # Response VOs
│   │   ├── mapper/          # MyBatis-Plus mappers
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST controllers
│   │   ├── interceptor/     # JWT auth interceptor
│   │   └── util/            # JWT utility
│   └── sql/init.sql         # Database init script
├── cm-pc/                   # PC frontend (Vue3 + Element Plus)
├── cm-h5/                   # H5 mobile frontend (Vue3 + Vant)
├── docs/standards/          # Development standards
├── setup.bat                # First-time setup
├── start.bat                # One-click start
├── stop.bat                 # One-click stop
└── README.md
```

## API Overview

| Module | Endpoints |
|--------|-----------|
| Auth | POST /api/auth/login, /logout, GET /info |
| Accessory | POST /api/accessories/inbound, GET /{barcode} |
| Worker | CRUD /api/workers |
| Category | CRUD /api/categories |
| Inventory | GET /api/inventories, /stats |
| Flow | POST /api/flows/transfer-out, /transfer-in, /sell |
| Trace | GET /api/flows/trace/{barcode} |
| Logs | GET /api/logs |

## License

Internal use only.