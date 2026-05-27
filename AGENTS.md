# CM - Circulation Management System

## Architecture

- Backend: `cm-backend/` - Spring Boot 3.2 + MyBatis-Plus + JWT
- PC Frontend: `cm-pc/` - Vue 3 + Element Plus + Vite (port 3100)
- H5 Frontend: `cm-h5/` - Vue 3 + Vant 4 + html5-qrcode (port 3101)
- Database: MySQL 8 - schema in `cm-backend/sql/init.sql`

## Conventions

- Backend: Java 17, package `com.cm`, RESTful APIs under `/api/`
- Frontend: JavaScript (not TypeScript), Composition API with `<script setup>`
- All text files must be UTF-8 without BOM
- Chinese UI text, English code identifiers
- Git commits: `<type>(<scope>): <message>`

## Key Files

- `docs/standards/` - 7 development standards documents
- `deploy/nginx.conf` - Production Nginx config
- `build.bat` - Production build script
- `start.bat` / `stop.bat` - Development start/stop scripts

## Common Tasks

- Start dev: `start.bat` (Windows)
- Build production: `build.bat`
- Database init: `mysql -u root -p --default-character-set=utf8mb4 < cm-backend/sql/init.sql`
- Backend compile: `cd cm-backend && mvn compile`
- Frontend build: `cd cm-pc && npm run build`

## Ports

- Backend API: 8080
- PC Frontend: 3100
- H5 Frontend: 3101
- Default login: admin / admin123