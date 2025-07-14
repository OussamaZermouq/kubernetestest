FROM openjdk:17-jdk-alpine
RUN addgroup -S inwi && adduser -S inwi -G inwi
USER inwi:inwi
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]