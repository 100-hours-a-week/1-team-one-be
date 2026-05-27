# ========== 1단계: 빌드 스테이지 ==========
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app

# 빌드 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 소스 코드 복사
COPY src src

# Gradle 빌드 실행 (테스트는 스킵하여 빌드 속도 단축)
RUN chmod +x gradlew && ./gradlew build -x test

# ========== 2단계: 런타임 스테이지 ==========
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# OpenTelemetry Java Agent 다운로드
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
RUN chmod 644 /app/opentelemetry-javaagent.jar

# 1단계 빌더에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/backend-app.jar app.jar

# 비루트 사용자로 안전하게 실행
USER nobody

EXPOSE 8080

CMD ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
