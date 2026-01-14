FROM eclipse-temurin:17-jdk

COPY build/libs/minimal.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["sh", "-c", "exec java $JVM_ARGS -jar app.jar"]
