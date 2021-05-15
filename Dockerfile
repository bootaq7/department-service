FROM openjdk:8-jdk-alpine
ADD target/department-service-1.1.jar department-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/department-service.jar"]