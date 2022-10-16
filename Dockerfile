FROM eclipse-temurin:18

MAINTAINER Ed Sweeney <ed@onextent.com>

EXPOSE 8443

RUN mkdir -p /app

COPY ./target/java-server-example-1.0-SNAPSHOT-jar-with-dependencies.jar /app/java-server-example.jar

WORKDIR /app

# override CMD from your run command, or k8s yaml, or marathon json, etc...
CMD java -jar ./java-server-example.jar

