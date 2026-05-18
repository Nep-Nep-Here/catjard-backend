# Despliegue en DigitalOcean (Droplet, demo por IP/HTTP)

Stack: 10 contenedores (Postgres, Eureka, API Gateway, 6 microservicios Spring
Boot, frontend nginx). El navegador solo usa el **puerto 80**; nginx hace de
reverse-proxy de `/api` al gateway (mismo origen → sin CORS).

---

## 0. Antes de empezar (en tu PC)

Subir a GitHub el código de despliegue. **Backend** (este repo, rama `master`):

```bash
git add .gitignore .env.example docker-compose.yml docker-compose.prod.yml DEPLOY.md \
        */Dockerfile */.dockerignore
git commit -m "Infra: Docker + override de producción para despliegue"
git push origin master
```

**Frontend** (repo `catjard-frontend`, rama `main`) — se modificó `nginx.conf`:

```bash
git add nginx.conf
git commit -m "nginx: reverse-proxy de /api al api-gateway"
git push origin main
```

---

## 1. Crear el Droplet (web de DigitalOcean)

> Estudiante: activa el **GitHub Student Developer Pack** → DigitalOcean da
> **$200 de crédito** (60 días). Cubre este Droplet de sobra.

1. **Create → Droplets**
2. Región: la más cercana (ej. *New York* o *San Francisco*).
3. Imagen: **Ubuntu 24.04 LTS**.
4. Tipo: **Basic** → CPU **Regular** → **4 GB RAM / 2 vCPU** (~$24/mes).
5. Autenticación: **SSH Key** (recomendado) o Password. Guarda la credencial.
6. Hostname: `catjard`.
7. **Create Droplet**. Anota la **IP pública** (ej. `203.0.113.10`).

### Firewall (solo 22 y 80)

**Networking → Firewalls → Create Firewall**:

- Inbound: `SSH 22` (TCP) y `HTTP 80` (TCP). Nada más.
- Outbound: dejar todo (default).
- Aplicar al Droplet `catjard`.

---

## 2. Desplegar (dentro del Droplet por SSH)

Conéctate: `ssh root@TU_IP`

```bash
# --- Docker + Compose ---
apt-get update && apt-get install -y docker.io docker-compose-v2 git
systemctl enable --now docker

# --- Swap 2 GB (colchón de seguridad para las 8 JVMs en 4 GB) ---
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# --- Clonar repos en layout: ~/catjard/{backend,frontend} ---
mkdir -p ~/catjard && cd ~/catjard
git clone https://github.com/Nep-Nep-Here/catjard-backend.git backend
git clone https://github.com/Nep-Nep-Here/catjard-frontend.git frontend
cd backend

# --- Crear .env con secretos NUEVOS (no se versiona) ---
cp .env.example .env
JWT=$(openssl rand -base64 48)
PGPASS=$(openssl rand -base64 24 | tr -d '/+=')
IP=$(curl -s ifconfig.me)
sed -i "s|^JWT_SECRET=.*|JWT_SECRET=$JWT|"            .env
sed -i "s|^POSTGRES_PASSWORD=.*|POSTGRES_PASSWORD=$PGPASS|" .env
sed -i "s|^FRONT_ORIGIN=.*|FRONT_ORIGIN=http://$IP|"  .env
sed -i "s|^SERVER_IP=.*|SERVER_IP=$IP|"               .env
cat .env   # verifica que IP y secretos quedaron bien

# --- Levantar (build incluido; primera vez ~15-25 min) ---
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build

# --- Verificar ---
docker compose -f docker-compose.yml -f docker-compose.prod.yml ps
curl -s -o /dev/null -w "%{http_code}\n" http://localhost/        # 200 = frontend OK
```

Abre en el navegador: **http://TU_IP**

---

## 3. Operación

Desde `~/catjard/backend` (siempre con los dos `-f`):

```bash
# Definir alias cómodo
alias dcp='docker compose -f docker-compose.yml -f docker-compose.prod.yml'

dcp ps                 # estado
dcp logs -f api-gateway   # logs
dcp restart frontend   # reiniciar un servicio
dcp down               # apagar (los datos persisten en el volumen pgdata)
dcp up -d              # encender de nuevo (sin --build si no cambió código)

# Actualizar tras un push:
cd ~/catjard/backend && git pull
cd ~/catjard/frontend && git pull
cd ~/catjard/backend && dcp up -d --build
```

## Notas

- Si algo no responde tras reiniciar el Droplet: `dcp down && dcp up -d`
  (reengancha el proxy de puertos; `pgdata` no se pierde).
- 4 GB es ajustado para 8 JVMs: el swap evita caídas. Si va muy lento, sube el
  Droplet a 8 GB desde el panel (Resize, sin perder datos).
- HTTPS/dominio: no incluido (demo por IP). Si luego quieres candado verde,
  se añade Caddy/Traefik + Let's Encrypt.
