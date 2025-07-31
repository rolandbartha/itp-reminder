# ---------- Stage 1: Build ----------
FROM gradle:8.4.0-jdk17 AS builder
COPY --chown=gradle:gradle . /app
WORKDIR /app
RUN gradle build --no-daemon

# ---------- Stage 2: Run ----------
FROM azul/zulu-openjdk:17-latest
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]