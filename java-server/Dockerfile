# 1. Сборка
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Запуск
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Копируем JAR
COPY --from=build /app/target/*.jar app.jar
# Создаем папку uploads
RUN mkdir -p uploads

ENTRYPOINT ["java", "-jar", "app.jar"]