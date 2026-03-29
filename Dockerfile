# syntax=docker/dockerfile:1
# ============================================
# Dockerfile
# 多阶段构建，支持本地和 CI/CD 环境
# 使用 BuildKit 缓存挂载加速依赖下载
# ============================================

# -------------------- 构建阶段 --------------------
FROM maven:3.9-eclipse-temurin-17 AS builder

# GitHub Packages 认证
ARG GITHUB_ACTOR
ARG GITHUB_TOKEN

# 代理设置（本地构建时传入，CI/CD 不需要）
ARG HTTP_PROXY
ARG HTTPS_PROXY
ENV http_proxy=$HTTP_PROXY
ENV https_proxy=$HTTPS_PROXY

WORKDIR /build

# 创建 Maven settings.xml（包含代理配置）
RUN mkdir -p /root/.m2 && \
    if [ -n "$HTTP_PROXY" ]; then \
        PROXY_HOST=$(echo $HTTP_PROXY | sed -e 's|http://||' -e 's|https://||' | cut -d: -f1); \
        PROXY_PORT=$(echo $HTTP_PROXY | sed -e 's|http://||' -e 's|https://||' | cut -d: -f2); \
        printf '%s\n' \
        '<?xml version="1.0" encoding="UTF-8"?>' \
        '<settings>' \
        '  <proxies>' \
        '    <proxy>' \
        '      <id>docker-proxy</id>' \
        '      <active>true</active>' \
        "      <protocol>http</protocol>" \
        "      <host>${PROXY_HOST}</host>" \
        "      <port>${PROXY_PORT}</port>" \
        '    </proxy>' \
        '  </proxies>' \
        '  <servers>' \
        '    <server>' \
        '      <id>github</id>' \
        "      <username>${GITHUB_ACTOR}</username>" \
        "      <password>${GITHUB_TOKEN}</password>" \
        '    </server>' \
        '  </servers>' \
        '  <profiles>' \
        '    <profile>' \
        '      <id>github</id>' \
        '      <repositories>' \
        '        <repository>' \
        '          <id>github</id>' \
        '          <url>https://maven.pkg.github.com/Yggdrasil-Labs/mimir-boot</url>' \
        '        </repository>' \
        '      </repositories>' \
        '    </profile>' \
        '  </profiles>' \
        '  <activeProfiles>' \
        '    <activeProfile>github</activeProfile>' \
        '  </activeProfiles>' \
        '</settings>' > /root/.m2/settings.xml; \
    else \
        printf '%s\n' \
        '<?xml version="1.0" encoding="UTF-8"?>' \
        '<settings>' \
        '  <servers>' \
        '    <server>' \
        '      <id>github</id>' \
        "      <username>${GITHUB_ACTOR}</username>" \
        "      <password>${GITHUB_TOKEN}</password>" \
        '    </server>' \
        '  </servers>' \
        '  <profiles>' \
        '    <profile>' \
        '      <id>github</id>' \
        '      <repositories>' \
        '        <repository>' \
        '          <id>github</id>' \
        '          <url>https://maven.pkg.github.com/Yggdrasil-Labs/mimir-boot</url>' \
        '        </repository>' \
        '      </repositories>' \
        '    </profile>' \
        '  </profiles>' \
        '  <activeProfiles>' \
        '    <activeProfile>github</activeProfile>' \
        '  </activeProfiles>' \
        '</settings>' > /root/.m2/settings.xml; \
    fi

# 复制 pom 文件（利用 Docker 缓存层）
COPY pom.xml .
COPY mealmate-start/pom.xml mealmate-start/
COPY mealmate-client/pom.xml mealmate-client/
COPY mealmate-adapter/pom.xml mealmate-adapter/
COPY mealmate-app/pom.xml mealmate-app/
COPY mealmate-domain/pom.xml mealmate-domain/
COPY mealmate-infrastructure/pom.xml mealmate-infrastructure/

# 下载依赖（使用 BuildKit 缓存挂载，加速后续构建）
RUN --mount=type=cache,target=/root/.m2/repository \
    echo "HTTP_PROXY=$HTTP_PROXY" && \
    echo "HTTPS_PROXY=$HTTPS_PROXY" && \
    cat /root/.m2/settings.xml | grep -A 5 "proxies" || echo "No proxy in settings.xml" && \
    mvn dependency:go-offline -B -DskipTests

# 复制源代码
COPY . .

# 先修复代码格式（解决 Windows CRLF 换行符问题）
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn spotless:apply -B || true

# 构建项目（使用缓存挂载）
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -Pci

# -------------------- 运行阶段 --------------------
FROM eclipse-temurin:17-jre

LABEL maintainer="YoungerYang-Y"
LABEL description="MealMate mealmate-service"

# 安装运行时依赖，并创建非 root 用户
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system app && \
    useradd --system --gid app --create-home --home-dir /app app

WORKDIR /app

# 从构建阶段复制 JAR 文件（排除 original-*.jar，只复制 Spring Boot 打包的 fat JAR）
COPY --from=builder /build/mealmate-start/target/mealmate-start-*.jar app.jar

# 设置文件权限
RUN chown -R app:app /app
USER app

# 暴露端口
EXPOSE 8081
EXPOSE 20880

# JVM 优化参数
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseSerialGC -XX:MaxRAM=512m"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8081/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
