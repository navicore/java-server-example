FROM eclipse-temurin:18

MAINTAINER Ed Sweeney <ed@onextent.com>

EXPOSE 8443

RUN mkdir -p /app

COPY ./target/java-server-example.jar /app/

WORKDIR /app

# override CMD from your run command, or k8s yaml, or marathon json, etc...
CMD java -jar ./java-server-example.jar

