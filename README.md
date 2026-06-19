# Catjard — Backend

![CI](https://github.com/Nep-Nep-Here/catjard-backend/actions/workflows/ci.yml/badge.svg)

Backend de microservicios (Spring Boot + Spring Cloud) para Cat Jard.

## Arquitectura

- **eureka-server** — service discovery
- **api-gateway** — único punto de entrada (proxy a `/api`)
- **identity-service** — autenticación / JWT
- **catalog-service** — catálogo de productos
- **crm-service** — clientes / leads
- **sales-service** — ventas
- **inventory-service** — inventario y movimientos
- **operations-service** — operaciones / artes
- **solicitudes-service** — solicitudes de ayuda y control de cambios (integración Jira)

Base de datos: PostgreSQL (una instancia, varias BD vía `init-databases.sql`).

## CI/CD

- **CI** (`.github/workflows/ci.yml`, on push/PR): detecta cambios → tests unitarios → integración → cobertura, por microservicio.
- **CD** (`.github/workflows/deploy.yml`, manual): plantilla de despliegue de referencia.

## Despliegue

Ver [DEPLOY.md](DEPLOY.md) — despliegue en Droplet de DigitalOcean por Docker Compose.

## Desarrollo local

```bash
docker compose up -d --build
```

Frontend en `http://localhost:5173`, API vía gateway en `http://localhost:8080/api`.
