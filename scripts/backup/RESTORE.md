# Procedimientos de restore

Tres escenarios cubiertos:

1. **Restore de una BD desde un `pg_dump`** — el caso más común (alguien borró
   datos, o se corrompió una sola BD).
2. **Restore del cluster completo desde `pg_basebackup`** — el cluster entero
   está perdido, queremos volver al último snapshot.
3. **Point-In-Time Recovery (PITR)** — combinar basebackup + WAL para volver a
   un instante exacto, p.ej. justo antes de un `DROP TABLE` accidental.

---

## 1. Restore de una BD desde dump

Lo hace el script `restore-dump.ps1`. Restaura a un nombre distinto por
defecto para no pisar la BD productiva.

```powershell
# 1. Listar los dumps disponibles
Get-ChildItem C:\catjard-backups\dumps\catjard_sales_*.dump | Sort-Object LastWriteTime -Descending

# 2. Restaurar a una BD nueva (segura, no toca la original)
.\restore-dump.ps1 `
    -DumpFile "C:\catjard-backups\dumps\catjard_sales_20260508_020000.dump" `
    -TargetDb catjard_sales_restore

# 3. Verificar
psql -U postgres -d catjard_sales_restore -c "SELECT count(*) FROM venta;"

# 4. Si se ve bien, swap (manual y consciente):
#    a) Apagar el microservicio que usa esa BD
#    b) Renombrar:
psql -U postgres -d postgres -c "ALTER DATABASE catjard_sales RENAME TO catjard_sales_old;"
psql -U postgres -d postgres -c "ALTER DATABASE catjard_sales_restore RENAME TO catjard_sales;"
#    c) Levantar el microservicio
#    d) Cuando confirmes, dropear la _old
```

> Si el problema afectó solo a una tabla, puedes restaurar selectivamente:
> ```powershell
> pg_restore -U postgres -d catjard_sales_restore --table=venta dump.dump
> ```

---

## 2. Restore completo del cluster desde basebackup

Para cuando el `PGDATA` se corrompió o se perdió.

> ⚠️ Detiene la BD entera. Coordínalo con el equipo.

```powershell
# 1. Detener Postgres
Stop-Service postgresql-x64-18

# 2. Mover (no borrar) el PGDATA actual por si acaso
$pgdata = "C:\Program Files\PostgreSQL\18\data"
Rename-Item $pgdata "$pgdata.broken_$(Get-Date -Format yyyyMMdd_HHmmss)"

# 3. Copiar el basebackup mas reciente al PGDATA
$ultimo = Get-ChildItem C:\catjard-backups\basebackup -Directory |
          Sort-Object LastWriteTime -Descending | Select-Object -First 1
Copy-Item -Recurse $ultimo.FullName $pgdata

# 4. Como se hizo con --write-recovery-conf, el directorio ya tiene
#    standby.signal -> Postgres arrancaria como replica. Para restore
#    "puntual" lo eliminamos:
Remove-Item (Join-Path $pgdata "standby.signal") -ErrorAction SilentlyContinue

# 5. Asegurar permisos correctos al usuario del servicio
icacls $pgdata /grant "NETWORK SERVICE:(OI)(CI)F" /T

# 6. Arrancar
Start-Service postgresql-x64-18
```

Postgres replay-eará automáticamente los WAL incluidos en el basebackup
(porque se usó `-X stream`) y abrirá listo para conexiones. Llegas al estado
del cluster al momento en que terminó `pg_basebackup`.

---

## 3. Point-In-Time Recovery (PITR)

Para volver a un instante exacto entre el basebackup y "ahora", usando los
WAL archivados.

**Caso de uso típico**: alguien hizo `DELETE FROM cliente;` a las 14:32 y
quieres volver al estado de 14:31.

```powershell
# 1. Detener Postgres
Stop-Service postgresql-x64-18

# 2. Mover PGDATA actual
$pgdata = "C:\Program Files\PostgreSQL\18\data"
Rename-Item $pgdata "$pgdata.broken_$(Get-Date -Format yyyyMMdd_HHmmss)"

# 3. Copiar basebackup ANTERIOR al incidente
$base = "C:\catjard-backups\basebackup\base_20260507_030000"
Copy-Item -Recurse $base $pgdata

# 4. Quitar standby.signal y ajustar postgresql.auto.conf
Remove-Item (Join-Path $pgdata "standby.signal") -ErrorAction SilentlyContinue
```

Editar `postgresql.auto.conf` (creado por `--write-recovery-conf`) y dejarlo
así:

```conf
restore_command = 'copy "C:\\catjard-backups\\wal_archive\\%f" "%p"'
recovery_target_time = '2026-05-08 14:31:00'
recovery_target_action = 'promote'
```

Luego crear un archivo vacío `recovery.signal` en `PGDATA`:

```powershell
New-Item -ItemType File -Path (Join-Path $pgdata "recovery.signal")
icacls $pgdata /grant "NETWORK SERVICE:(OI)(CI)F" /T
Start-Service postgresql-x64-18
```

Postgres reproducirá los WAL hasta `recovery_target_time`, se promoverá y
quedará abierto para escritura. Verifica antes de seguir trabajando:

```sql
SELECT now(), pg_is_in_recovery();
SELECT count(*) FROM cliente;  -- la tabla ya deberia estar
```

> **Tras un PITR** se inicia una nueva *timeline*. Los WAL siguientes
> tendrán nombres con prefijo `00000002...`. Mantén el `wal_archive`
> intacto: las viejas timelines se conservan ahí.

---

## 4. Probar el plan (importante)

Un backup que nunca se restauró no es un backup. Recomendado al menos una vez
al mes:

```powershell
# Test rapido: restore del ultimo dump de identity a una BD throwaway
$last = Get-ChildItem C:\catjard-backups\dumps\catjard_identity_*.dump |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1
.\restore-dump.ps1 -DumpFile $last.FullName -TargetDb catjard_identity_test -Force
psql -U postgres -d catjard_identity_test -c "SELECT count(*) FROM usuario;"
psql -U postgres -d postgres -c "DROP DATABASE catjard_identity_test;"
```
