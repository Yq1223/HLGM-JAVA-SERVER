# ============================
# 构建阶段 (Maven + JDK 8)
# ============================
FROM maven:3.8-openjdk-8-slim AS builder

WORKDIR /build

# 先复制 pom.xml，利用 Docker 缓存层加速构建
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================
# 运行阶段 (JRE 8)
# ============================
FROM openjdk:8-jre-slim

LABEL maintainer="wool-team"
LABEL description="薅羊毛信息社区后台服务"

WORKDIR /app

# 创建非 root 用户
RUN groupadd -r wool && useradd -r -g wool wool

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/wool-backend-1.0.0.jar app.jar

# 时区设置为上海
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 暴露端口
EXPOSE 8080

# 切换到非 root 用户
USER wool

# JVM 优化参数 + 启动
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Duser.timezone=Asia/Shanghai", \
    "-jar", "app.jar"]
