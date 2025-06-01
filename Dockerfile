FROM eclipse-temurin:21-jdk

COPY build/libs/test-grafana-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java","-Dspring.datasource.jdbc-url=jdbc:postgresql://test-postgres:5432/test","-Dspring.datasource.username=postgres","-Dspring.datasource.password=postgres","-Dspring.profiles.active=local","-jar", "/app.jar"]
