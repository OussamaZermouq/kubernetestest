FROM openjdk:17-jdk-alpine
WORKDIR /opt/app
COPY ./ /opt/app
RUN mvn clean install -DskipTests
RUN addgroup -S inwi && adduser -S inwi -G inwi
USER inwi:inwi
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8085
ENTRYPOINT ["java","-jar","/app.jar"]