FROM openjdk:11

WORKDIR /app

COPY target/rumpUp-0.0.1-SNAPSHOT.jar .

ENV DBHOST=db-rampup

EXPOSE 8080

CMD ["java", "-jar", "rumpUp-0.0.1-SNAPSHOT.jar"]
