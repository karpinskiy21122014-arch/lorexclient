FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
ENV MAVEN_OPTS="-Xmx256m -XX:MaxMetaspaceSize=128m"
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT java -jar -Xmx256m -Dserver.port=${PORT:-8080} /app.jar
