FROM eclipse-temurin:17-jre-jammy

RUN which curl || (apt-get update && apt-get install -y curl --no-install-recommends && apt-get clean)

RUN mkdir /app

COPY build/libs/cp-mini.jar /app/cp-mini.jar

EXPOSE 8081/tcp
EXPOSE 9092/tcp

ENTRYPOINT ["java", "-jar", "/app/cp-mini.jar"]

HEALTHCHECK --start-period=30s --interval=2s --timeout=3s \
    CMD curl --fail http://localhost:8081
