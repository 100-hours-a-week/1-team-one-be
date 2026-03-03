# Backend Dockerfile
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
RUN chmod 644 /app/opentelemetry-javaagent.jar

# JAR 파일 복사
COPY backend-app.jar app.jar

# 비루트 사용자로 실행
USER nobody

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]