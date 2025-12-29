# jdk21 Image Start
FROM amazoncorretto:21-alpine

# jar 파일 복제 (버전 무관하게 자동으로 찾음)
COPY build/libs/*.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
