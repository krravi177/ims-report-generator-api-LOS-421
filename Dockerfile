FROM openjdk:17-jdk-alpine
COPY target/ims-report-generator-api-0.0.1.jar ims-report-generator-api.jar

# Set default environment variables
ENV APP_PORT=8080
ENV AWS_REGION="us-east-1"

EXPOSE 8080
ENTRYPOINT ["java","-jar","/ims-report-generator-api.jar"]