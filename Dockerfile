# 1. 基础镜像：使用 JDK 17 (根据你的项目版本调整，如果是 Java 8 就换 openjdk:8)
FROM eclipse-temurin:17-jdk-jammy

# 2. 作者/维护者信息（可选）
LABEL maintainer="HuanFeng"

# 3. 将本地 maven 打包好的 jar 包拷贝进容器，并重命名为 app.jar
# 注意：这里假设你运行过 mvn package，jar 包在 target 目录下
COPY target/*.jar app.jar

# 4. 暴露端口（只是声明，实际端口映射在 docker-compose 里做）
EXPOSE 8080

# 5. 启动命令
# 这里的配置是为了加快随机数生成，避免启动卡顿
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.redis.host=redis", "--spring.datasource.url=jdbc:mysql://mysqldb:3306/todo_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8"]