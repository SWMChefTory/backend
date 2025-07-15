# Dockerfile

# jdk21 Image Start
FROM openjdk:21

# 인자 설정 - JAR_File
ARG JAR_FILE=build/libs/*.jar

# jar 파일 복제
COPY build/libs/api-0.0.1-SNAPSHOT.jar app.jar

# 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]
