-- Ejecuta esto UNA sola vez como superusuario postgres:
--   psql -U postgres -f init-databases.sql

CREATE DATABASE catjard_identity;
CREATE DATABASE catjard_catalog;
CREATE DATABASE catjard_crm;
CREATE DATABASE catjard_sales;
CREATE DATABASE catjard_inventory;
CREATE DATABASE catjard_operations;
