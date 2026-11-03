FROM eclipse-temurin:21-jre-jammy
RUN mkdir -p /app /run/minimmorts && chown 10001:10001 /run/minimmorts
COPY server/minimmorts-server.jar /app/server.jar
COPY config/server.xml /app/config.xml
WORKDIR /run/minimmorts
USER 10001:10001
EXPOSE 8015
ENTRYPOINT ["java", "-jar", "/app/server.jar", "/app/config.xml"]
