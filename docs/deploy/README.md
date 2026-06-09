# 部署文档

## 文档信息

| 版本   | 日期 | 作者 | 说明 |
|:-----| :--- | :--- | :--- |
| v1.0 | 2026-06-01 | - | 初始版本 |

## 目录

1. [环境要求](#1-环境要求)
2. [前端部署](#2-前端部署)
3. [后端部署](#3-后端部署)
4. [Docker 部署](#4-docker-部署)
5. [Systemd 自动启动](#5-systemd-自动启动)
6. [SSL 证书配置](#6-ssl-证书配置)
7. [配置说明](#7-配置说明)

---

## 1. 环境要求

| 组件 | 版本要求 | 说明 |
| :--- | :--- | :--- |
| Node.js | ≥ 18.x | 前端开发/构建环境 |
| Java | ≥ 21 | 后端运行环境 |
| PostgreSQL | ≥ 16.x | 数据库 |
| Redis | ≥ 7.x | 缓存/会话存储 |
| Docker | ≥ 20.x | 容器化部署 |
| Docker Compose | ≥ 2.x | 容器编排 |

---

## 2. 前端部署

### 2.1 开发环境

1. 安装依赖：
   ```bash
   pnpm install
   ```

2. 启动开发服务器：
   ```bash
   pnpm dev
   ```

### 2.2 生产部署

1. 构建生产版本：
   ```bash
   pnpm build
   ```

2. 部署到 Nginx：

   配置文件参考 `deploy/nginx.conf`，已包含 HTTPS 支持和安全配置。

---

## 3. 后端部署

### 3.1 开发环境

1. 编译项目：
   ```bash
   mvn clean install
   ```

2. 启动服务：
   ```bash
   mvn spring-boot:run
   ```

### 3.2 生产部署

1. 构建 JAR：
   ```bash
   mvn clean package -DskipTests
   ```

2. 使用启动脚本：

   ```bash
   # 启动（含 Java/Jar 前置检查、端口检测、健康检查等待）
   sh start.sh start

   # 停止（优雅停止：SIGTERM → 二次 SIGTERM → SIGKILL 三阶梯）
   sh start.sh stop

   # 重启
   sh start.sh restart

   # 查看状态（进程、端口、健康状态）
   sh start.sh status

   # 查看日志（支持 tail / head / tail100 三种模式）
   sh start.sh logs          # 实时跟踪（默认）
   sh start.sh logs head     # 查看前 100 行
   sh start.sh logs tail100  # 查看最近 100 行

   # 一键部署（停止 → 备份旧 Jar → 替换 → 启动）
   sh start.sh deploy /path/to/new-site-server.jar
   ```

---

## 4. Docker 部署

### 4.1 目录结构

```
jinynet-site/
├── site-webui/            # 前端项目
├── site-server/           # 后端项目
├── docs/
│   ├── design/
│   │   └── 05-schema.sql  # 数据库初始化脚本
│   └── deploy/
│       ├── docker-compose.yml
│       ├── nginx.conf
│       ├── start.sh
│       ├── sitejar.service
│       └── ssl/           # SSL 证书目录（需自行创建）
│           ├── cert.pem
│           └── key.pem
├── site-server/
│   ├── Dockerfile         # 后端镜像构建文件
│   └── .dockerignore      # 构建忽略规则
```

### 4.2 启动服务

```bash
# 进入部署目录
cd docs/deploy

# 构建镜像并后台启动所有服务
docker compose up -d --build

# 仅启动（使用已有镜像，不重新构建）
docker compose up -d

# 查看所有服务状态（含健康检查结果）
docker compose ps

# 实时查看所有日志
docker compose logs -f

# 查看指定服务日志（如 backend）
docker compose logs -f backend

# 查看最近 100 行日志
docker compose logs --tail 100 backend

# 重启单个服务
docker compose restart backend

# 停止所有服务
docker compose down

# 停止并删除数据卷（⚠️ 会清除数据库和 Redis 数据）
docker compose down -v
```

### 4.3 服务说明

| 服务 | 端口 | 资源限制 | 说明 |
| :--- | :--- | :--- | :--- |
| frontend | 80, 443 | CPU 0.5 / Mem 256M | Nginx 前端服务 |
| backend | 8080 | CPU 2.0 / Mem 1G | Spring Boot 后端服务 |
| postgres | 5432 | CPU 2.0 / Mem 1G | PostgreSQL 数据库 |
| redis | 6379 | CPU 1.0 / Mem 512M | Redis 缓存 |

### 4.4 健康检查

所有服务均已配置健康检查，依赖链确保启动顺序：
- **frontend**: 检查 `http://localhost:80/`
- **backend**: 检查 `http://localhost:8080/api/health`（等待 Postgres 和 Redis 就绪后启动）
- **postgres**: 使用 `pg_isready -U ptech -d ptech`
- **redis**: 使用 `redis-cli -a <password> ping`

### 4.5 后端 Dockerfile

采用多阶段构建，位于 `site-server/Dockerfile`：

- **构建阶段**：Maven + Temurin JDK 21 Alpine，分离依赖下载与源码编译以充分利用缓存
- **运行阶段**：JRE 21 Alpine，使用 `jarmode=layertools` 分层提取，非 root 用户运行，内置健康检查

```dockerfile
# 阶段一：Maven 构建
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
COPY pom.xml ./
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn clean package -DskipTests -B -q \
    && java -Djarmode=layertools -jar target/*.jar extract

# 阶段二：JRE 运行
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S ptech && adduser -S ptech -G ptech
COPY --from=builder /build/target/extracted/*/ ./
USER ptech
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} org.springframework.boot.loader.launch.JarLauncher"]
```

### 4.6 数据安全

| 配置 | 说明 |
| :--- | :--- |
| Redis 密码 | `redis123456`（通过 `--requirepass` 和 `SPRING_REDIS_PASSWORD` 同步） |
| Redis 持久化 | AOF + RDB 双写，`allkeys-lru` 淘汰策略 |
| PostgreSQL 初始化 | 首次启动自动执行 `05-schema.sql` |
| 日志轮转 | 所有服务配置 `json-file` 驱动，50~100MB 切割，保留 5 个文件 |
| 网络隔离 | 各服务通过 `ptech-br0` 网桥通信，无公网暴露（除 80/443） |

---

## 5. Systemd 自动启动

### 5.1 部署 service 文件

```bash
# 1. 复制 service 文件到 systemd 目录
sudo cp docs/deploy/sitejar.service /etc/systemd/system/

# 2. 将启动脚本复制到目标目录并赋予执行权限
sudo mkdir -p /opt/apps/jar
sudo cp docs/deploy/start.sh /opt/apps/jar/start.sh
sudo chmod +x /opt/apps/jar/start.sh

# 3. 创建专用运行用户（推荐，替换 root）
sudo useradd -r -s /bin/false ptech
sudo chown -R ptech:ptech /opt/apps/jar
```

> **注意**：如使用专用用户，需同步修改 `sitejar.service` 中的 `User=` 字段。

### 5.2 启用并启动

```bash
# 重新加载 systemd 配置
sudo systemctl daemon-reload

# 开启开机自启
sudo systemctl enable sitejar

# 启动服务
sudo systemctl start sitejar

# 查看状态
sudo systemctl status sitejar
```

### 5.3 日常管理

```bash
# 查看实时日志
journalctl -u sitejar -f

# 查看最近 100 行日志
journalctl -u sitejar -n 100

# 重启服务
sudo systemctl restart sitejar

# 停止服务
sudo systemctl stop sitejar

# 禁用开机自启
sudo systemctl disable sitejar
```

### 5.4 安全保障

Service 文件已内置安全加固措施，兼顾安全性与 Java 运行时兼容性：

| 配置项 | 级别 | 作用 |
| :--- | :--- | :--- |
| `NoNewPrivileges=yes` | 强制 | 禁止进程获取新权限 |
| `ProtectSystem=full` | 推荐 | `/usr`、`/etc` 只读（留 `/dev`、`/proc`、`/sys` 可访问） |
| `PrivateTmp=yes` | 推荐 | 使用独立的 `/tmp` 与 `/var/tmp` |
| `PrivateDevices=yes` | 推荐 | 隔离 `/dev`，仅保留基础设备节点 |
| `ReadWritePaths=/opt/apps/jar` | 必要条件 | 显式授权 JVM 写入日志、PID、GC dump 等 |
| `ProtectHome=yes` | 推荐 | 禁止访问 `/home`、`/root` |
| `ProtectKernelTunables=yes` | 推荐 | 禁止修改内核参数 |
| `ProtectKernelModules=yes` | 推荐 | 禁止加载内核模块 |
| `ProtectControlGroups=yes` | 推荐 | 禁止修改 cgroup |
| `ProtectClock=yes` | 推荐 | 禁止修改系统时钟 |
| `ProtectKernelLogs=yes` | 推荐 | 禁止读取内核日志 |
| `ProtectProc=invisible` | 推荐 | 隐藏其他进程的 `/proc` 信息 |

> **故障排查**：若依旧 `exit-code`，逐步执行：
> ```bash
> # 1. 先手工跑脚本确认能否启动
> sudo /bin/bash /opt/apps/jar/start.sh start
> 
> # 2. 查看详细日志
> journalctl -u sitejar --no-pager -l
> 
> # 3. 如果还不行，临时注释掉所有安全配置后重试以隔离问题
> ```

### 5.5 重启策略

| 配置项 | 值 | 说明 |
| :--- | :--- | :--- |
| `Restart` | `on-failure` | 仅异常退出时重启 |
| `RestartSec` | `10s` | 重启前等待 10 秒 |
| `StartLimitBurst` | `5` | 5 分钟内最多重启 5 次 |
| `StartLimitAction` | `reboot-force` | 超过限制后强制重启整机 |

---

## 6. SSL 证书配置

### 6.1 生成自签名证书（测试环境）

```bash
# 创建 SSL 目录
mkdir -p docs/deploy/ssl

# 生成自签名证书
openssl req -x509 -newkey rsa:4096 -keyout docs/deploy/ssl/key.pem -out docs/deploy/ssl/cert.pem -days 365 -nodes
```

### 6.2 使用 Let's Encrypt 证书（生产环境）

```bash
# 安装 Certbot
sudo apt-get install certbot

# 获取证书
sudo certbot certonly --standalone -d your-domain.com

# 复制证书到部署目录
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem docs/deploy/ssl/cert.pem
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem docs/deploy/ssl/key.pem
```

---

## 7. 配置说明

### 7.1 docker-compose.yml 主要配置

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| POSTGRES_DB | ptech | 数据库名 |
| POSTGRES_USER | ptech | 数据库用户名 |
| POSTGRES_PASSWORD | ptech123456 | 数据库密码 |
| SPRING_REDIS_HOST | redis | Redis 主机 |
| SPRING_REDIS_PORT | 6379 | Redis 端口 |
| SPRING_REDIS_PASSWORD | redis123456 | Redis 密码（与 Redis 容器同步） |
| JAVA_OPTS | -Xms256m -Xmx512m -XX:+UseG1GC ... | JVM 参数 |
| PG_SHARED_BUFFERS | 256MB | PG 共享缓冲区 |
| REDIS_MAXMEMORY | 256mb | Redis 最大内存 |
| LOG_MAX_SIZE | 50m~100m | 单文件日志上限 |
| LOG_MAX_FILE | 5 | 日志保留文件数 |

### 7.2 start.sh 配置项

| 配置项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| JAR_NAME | site-server.jar | Jar 包文件名 |
| JAR_PATH | /opt/apps/jar | Jar 所在目录 |
| LOG_PATH | /opt/apps/jar/logs/ptech.log | 应用日志路径 |
| GC_LOG_PATH | /opt/apps/jar/logs/gc.log | GC 日志路径 |
| LOG_RETENTION_DAYS | 30 | 日志保留天数 |
| JVM_OPTS | -Xms512m -Xmx1024m ... | JVM 运行时参数 |
| APP_PORT | 8080 | 应用端口 |
| SPRING_PROFILE | prod | Spring Profile |
| HEALTH_CHECK_URL | http://localhost:8080/api/health | 健康检查 URL |
| STARTUP_TIMEOUT | 60 | 启动超时秒数 |
| STOP_TIMEOUT | 60 | 停止超时秒数 |
| PID_FILE | /opt/apps/jar/ptech.pid | PID 文件路径 |
| ENV_FILE | /opt/apps/jar/.env | 环境变量文件（可选） |

---

## 更新日志

| 版本 | 日期 | 说明 |
| :--- | :--- | :--- |
| v1.2 | 2026-06-01 | 优化启动脚本（PID管理、健康检查、GC日志、一键部署），新增 systemd 自动启动，完善 Docker 部署（多阶段 Dockerfile、资源限制、Redis 密码、日志轮转） |
| v1.1 | 2026-05-07 | 添加 HTTPS 支持、健康检查、优雅启停、日志管理 |
| v1.0 | 2024-XX-XX | 初始版本 |
