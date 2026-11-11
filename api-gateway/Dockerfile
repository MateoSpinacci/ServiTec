# Etapa 1: Compilación (Builder)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar el wrapper y los POMs necesarios para el reactor de Maven
COPY api-gateway/.mvn/ .mvn/
COPY api-gateway/mvnw ./mvnw
COPY pom.xml ./
COPY servitec-common/pom.xml servitec-common/
COPY ms-tecnicos/pom.xml ms-tecnicos/
COPY ms-solicitudes/pom.xml ms-solicitudes/
COPY api-gateway/pom.xml api-gateway/

RUN chmod +x mvnw
# Descargar dependencias del módulo
RUN ./mvnw dependency:go-offline -B -pl api-gateway -am

COPY api-gateway/src api-gateway/src
RUN ./mvnw clean package -pl api-gateway -am -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
