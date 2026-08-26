FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

COPY src src

RUN ./gradlew clean bootJar -x test --no-daemon


FROM eclipse-temurin:21-jre

ARG APP_UID=10001
ARG APP_GID=10001

RUN groupadd --gid ${APP_GID} appgroup \
    && useradd \
        --uid ${APP_UID} \
        --gid appgroup \
        --no-log-init \
        --no-create-home \
        --shell /usr/sbin/nologin \
        appuser \
    && mkdir -p /app \
    && chown appuser:appgroup /app

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

USER appuser:appgroup

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]