# Etapa 1: Compilación (Builder)
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar el wrapper y los POMs necesarios para el reactor de Maven
COPY ms-solicitudes/.mvn/ .mvn/
COPY ms-solicitudes/mvnw ./mvnw
COPY pom.xml ./
COPY servitec-common/pom.xml servitec-common/
COPY ms-tecnicos/pom.xml ms-tecnicos/
COPY ms-solicitudes/pom.xml ms-solicitudes/
COPY api-gateway/pom.xml api-gateway/

RUN chmod +x mvnw
# Descargar dependencias del módulo y del common
RUN ./mvnw dependency:go-offline -B -pl ms-solicitudes -am

# Copiar el código fuente de ambos
COPY servitec-common/src servitec-common/src
COPY ms-solicitudes/src ms-solicitudes/src

# Compilar específicamente ms-solicitudes (y compilará common automáticamente)
RUN ./mvnw clean package -pl ms-solicitudes -am -DskipTests

# Etapa 2: Imagen final
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
# Extraer el JAR de la carpeta del módulo
COPY --from=builder /app/ms-solicitudes/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
