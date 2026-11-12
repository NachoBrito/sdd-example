FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties openapi.properties ./
COPY config config
COPY src src

RUN chmod +x gradlew && ./gradlew buildLayers --no-daemon -x test

FROM eclipse-temurin:25-jre
WORKDIR /home/app

COPY --from=builder /workspace/build/docker/main/layers/libs /home/app/libs
COPY --from=builder /workspace/build/docker/main/layers/resources /home/app/resources
COPY --from=builder /workspace/build/docker/main/layers/project_libs /home/app/project_libs
COPY --from=builder /workspace/build/docker/main/layers/app/application.jar /home/app/application.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/home/app/application.jar"]
