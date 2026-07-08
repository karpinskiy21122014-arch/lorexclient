# Шаг 1: Сборка. Ограничиваем память Maven, чтобы Render не задыхался
FROM maven:3.8.5-openjdk-17 AS build
ENV MAVEN_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m"
COPY . .
RUN mvn clean package -DskipTests

# Шаг 2: Запуск легкого контейнера
FROM openjdk:17-jdk-slim
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT java -jar -Xmx256m -Dserver.port=${PORT:-8080} /app.jar