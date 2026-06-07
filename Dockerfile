# ---------- Build Stage ----------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
LABEL authors="shashanka"

WORKDIR /app

COPY pom.xml .

# To prevent downloading dependencies unless there is a change in pom.xml
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["java","-javaagent:/otel/opentelemetry-javaagent.jar","-Dotel.service.name=payment-fraud-detection-service","-Dotel.traces.sampler=always_on","-Dotel.instrumentation.logback-appender.enabled=true","-jar","app.jar"]