# 构建阶段
FROM maven:3.8-eclipse-temurin-11 AS build
WORKDIR /app

# 拷贝Maven配置文件（使用阿里云镜像加速）
COPY settings.xml /usr/share/maven/ref/settings.xml

# 先拷贝pom.xml，利用Docker缓存加速依赖下载
COPY pom.xml .
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline -B

# 拷贝源代码并构建
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:11-jre
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段复制jar包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动命令
ENTRYPOINT ["java", "-jar", "-Dserver.port=8000", "-Dfile.encoding=UTF-8", "app.jar"]
