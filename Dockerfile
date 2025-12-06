# 1. JDK 기반 이미지
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# 2. 프로젝트 전체 복사
COPY . .

# 3. Spring Boot 빌드
RUN ./gradlew clean build -x test

# 4. 실제 실행용 이미지
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 5. 빌드 결과 복사
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENV PORT=8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
