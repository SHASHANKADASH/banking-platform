# ---------- Build Stage ----------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
LABEL authors="shashanka"

WORKDIR /app

# Copy only POMs first
COPY pom.xml .
COPY account-service/pom.xml account-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml

# Download dependencies
RUN mvn dependency:go-offline -pl account-service -am

# Now copy source
COPY payment-service/src payment-service/src

RUN mvn package -pl payment-service -am -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/payment-service/target/*.jar app.jar

EXPOSE 8081

# ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["java","-javaagent:/otel/opentelemetry-javaagent.jar","-Dotel.service.name=payment-service","-Dotel.traces.sampler=always_on","-Dotel.instrumentation.logback-appender.enabled=true","-jar","app.jar"]