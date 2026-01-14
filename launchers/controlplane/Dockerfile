FROM eclipse-temurin:17-jdk

COPY build/libs/controlplane.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["sh", "-c", "exec java $JVM_ARGS -jar app.jar"]
