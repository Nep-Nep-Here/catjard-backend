# --- Build ---
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime ---
FROM eclipse-temurin:25-jre
WORKDIR /app
# Cliente de PostgreSQL (pg_dump) para el boton "Descargar copia local" del panel de
# Continuidad: la imagen JRE base no lo trae. postgresql-client en Ubuntu Noble = v16,
# igual que el server (postgres:16). El pg_dump queda en /usr/bin/pg_dump.
RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
