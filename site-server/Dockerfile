# ============================================================
# Ptech Backend - 多阶段构建 Dockerfile
# 阶段一：Maven 构建；阶段二：JRE 运行
# ============================================================

# ---------- 阶段一：构建 ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. 先复制 pom 和依赖描述，利用 Docker 层缓存加速
COPY pom.xml ./
# 如果存在父 POM，按需取消注释：
# COPY ../pom.xml ../pom.xml

# 2. 下载依赖（不编译源码，仅缓存依赖层）
RUN mvn dependency:go-offline -B -q

# 3. 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B -q \
    && java -Djarmode=layertools -jar target/*.jar extract \
            --destination target/extracted

# ---------- 阶段二：运行 ----------
FROM eclipse-temurin:21-jre-alpine AS runtime

# 安全：创建非 root 用户
RUN addgroup -S jinynet && adduser -S jinynet -G jinynet

WORKDIR /app

# 分层复制，充分利用 Docker 缓存
COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

# 创建日志目录并授权
RUN mkdir -p /app/logs && chown -R jinynet:jinynet /app

USER jinynet

# JVM 默认参数（可通过环境变量 JAVA_OPTS 覆盖）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV SPRING_PROFILES_ACTIVE="prod"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]
