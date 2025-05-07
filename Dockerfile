FROM openjdk:17-jdk-slim

EXPOSE 8081

COPY target/cloud-service-diploma-0.0.1-SNAPSHOT.jar cloudservice.jar

ADD src/main/resources/application.properties

CMD ["java","-jar","/cloudservice.jar"]