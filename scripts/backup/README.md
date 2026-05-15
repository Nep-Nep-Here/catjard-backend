# Backups y PITR — catjard

Estrategia de respaldo para el cluster PostgreSQL 18 nativo en Windows que aloja
las seis BDs del proyecto (`catjard_identity`, `catjard_catalog`, `catjard_crm`,
`catjard_sales`, `catjard_inventory`, `catjard_operations`).

## Visión general

Tres capas complementarias:

| Capa             | Herramienta       | Frecuencia | Sirve para                                  |
|------------------|-------------------|------------|---------------------------------------------|
| Lógico por BD    | `pg_dump`         | Diario     | Restaurar una BD puntual / migrar          |
| Físico cluster   | `pg_basebackup`   | Semanal    | Restaurar cluster completo + base para PITR|
| WAL archive      | `archive_command` | Continuo   | Point-In-Time Recovery (cualquier instante)|

Todos los artefactos viven en `C:\catjard-backups\`:

```
C:\catjard-backups\
├── dumps\          # pg_dump por BD (.dump, formato custom)
├── basebackup\     # snapshot físico semanal del cluster
├── wal_archive\    # WAL archivados (continuos)
└── logs\           # logs de ejecución de scripts
```

---

## 1. Configuración del cluster (una sola vez)

### 1.1 Aplicar configuración con script

Hay un script que parchea `postgresql.conf` automáticamente, hace backup del
original y reinicia el servicio:

```powershell
# Ejecutar PowerShell COMO ADMINISTRADOR
powershell -ExecutionPolicy Bypass -File scripts\backup\configure-wal-archiving.ps1
```

Para ver qué cambiaría sin escribir nada:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\backup\configure-wal-archiving.ps1 -DryRun
```

Las directivas que aplica son:

```conf
wal_level = replica
archive_mode = on
archive_command = 'copy "%p" "C:\\catjard-backups\\wal_archive\\%f"'
max_wal_senders = 10
```

> Notas:
> - `wal_level = replica` es suficiente para PITR y para una futura réplica streaming.
> - `archive_command` en Windows usa `copy` (cmd) con doble barra invertida.
> - `%p` es la ruta del WAL a archivar; `%f` es el nombre de archivo.
> - El script crea `postgresql.conf.bak_<timestamp>` antes de tocar nada.

#### Alternativa: edición manual

Si prefieres editarlo a mano, abrí `C:\Program Files\PostgreSQL\18\data\postgresql.conf`
con un editor (como administrador), aplicá las 4 líneas de arriba, guardá,
y reiniciá:

```powershell
Restart-Service postgresql-x64-18
```

### 1.3 Verificar que el archiving funciona

```sql
-- en psql, conectado a cualquier BD
SELECT name, setting FROM pg_settings
 WHERE name IN ('wal_level','archive_mode','archive_command');

-- forzar un cambio de WAL para ver que se archiva
SELECT pg_switch_wal();

-- estado del archiver
SELECT * FROM pg_stat_archiver;
```

Después de `pg_switch_wal()` deberías ver al menos un archivo nuevo en
`C:\catjard-backups\wal_archive\` y `archived_count` aumentando.

### 1.4 Crear un rol para backups (recomendado)

Evita usar `postgres` superusuario en los scripts:

```sql
CREATE ROLE backup_user WITH LOGIN REPLICATION PASSWORD 'backup_pass_cambiar';
GRANT CONNECT ON DATABASE catjard_identity   TO backup_user;
GRANT CONNECT ON DATABASE catjard_catalog    TO backup_user;
GRANT CONNECT ON DATABASE catjard_crm        TO backup_user;
GRANT CONNECT ON DATABASE catjard_sales      TO backup_user;
GRANT CONNECT ON DATABASE catjard_inventory  TO backup_user;
GRANT CONNECT ON DATABASE catjard_operations TO backup_user;
-- y, conectado a cada BD:
-- GRANT pg_read_all_data TO backup_user;
```

Y guarda la contraseña en `%APPDATA%\postgresql\pgpass.conf` (formato
`host:port:db:user:password`, una línea por entrada) para que los scripts no
pidan password.

---

## 2. Scripts disponibles

| Script                         | Qué hace                                        | Frecuencia |
|--------------------------------|-------------------------------------------------|------------|
| `pg-dump-all.ps1`              | `pg_dump` de las 6 BDs, formato custom         | Diario     |
| `pg-basebackup.ps1`            | Snapshot físico completo del cluster           | Semanal    |
| `cleanup-old-backups.ps1`      | Borra dumps y basebackups según retención      | Diario     |
| `restore-dump.ps1`             | Restaura una BD desde un `.dump`               | A demanda  |

Los archivos están todos en `scripts/backup/`.

---

## 3. Programar con Task Scheduler

Abrir **Programador de tareas** y crear tres tareas:

1. **catjard-pg-dump** — diaria 02:00
   - Acción: `powershell.exe -ExecutionPolicy Bypass -File "C:\Users\ADMIN\Documents\Universidad\datos2 - backend\catjard\scripts\backup\pg-dump-all.ps1"`

2. **catjard-pg-basebackup** — semanal domingo 03:00
   - Acción: `powershell.exe -ExecutionPolicy Bypass -File "...\pg-basebackup.ps1"`

3. **catjard-cleanup** — diaria 04:00
   - Acción: `powershell.exe -ExecutionPolicy Bypass -File "...\cleanup-old-backups.ps1"`

Recomendación: marcar "Ejecutar con privilegios más altos" y "Ejecutar
independientemente de si el usuario inició sesión".

---

## 4. Restore — ver `RESTORE.md`

Procedimientos para:
- Restaurar una BD desde un `pg_dump` (caso común).
- Restaurar el cluster completo desde `pg_basebackup`.
- Point-In-Time Recovery (PITR) a un instante específico usando WAL archive.
