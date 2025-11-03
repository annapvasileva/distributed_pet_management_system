FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY target/*.jar app.jar
CMD ["java", "-jar", "app.jar"]