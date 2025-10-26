# 构建阶段：编译Jar包
FROM maven:3.8.5-openjdk-11-slim AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline  # 缓存Maven依赖
COPY src/ /build/src/
RUN mvn package -DskipTests    # 构建可执行Jar

# 运行阶段：仅保留运行时环境
FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]