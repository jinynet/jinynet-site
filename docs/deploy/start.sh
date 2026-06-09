#!/bin/bash
#
# Ptech Backend 服务管理脚本
# 支持 systemd 集成（Type=forking + PIDFile）
# 用法：./start.sh {start|stop|restart|status|logs|deploy|help}
#

set -o pipefail

# ======================== 配置区域（按需修改） ========================
JAR_NAME="site-server.jar"
JAR_PATH="/opt/apps/jar"
LOG_PATH="/opt/apps/jar/logs/sitejar.log"
GC_LOG_PATH="/opt/apps/jar/logs/gc.log"
LOG_RETENTION_DAYS=30
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${JAR_PATH}/heapdump.hprof"
APP_PORT=8080
SPRING_PROFILE="prod"
HEALTH_CHECK_URL="http://localhost:${APP_PORT}/api/health"
STARTUP_TIMEOUT=60
STOP_TIMEOUT=60
# systemd Type=forking 需要 PIDFile，留空则使用 ${JAR_PATH}/sitejar.pid
PID_FILE="${JAR_PATH}/sitejar.pid"
# 额外环境变量文件（可选），格式：KEY=VALUE，每行一个
ENV_FILE="${JAR_PATH}/.env"
# =====================================================================

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $(date '+%Y-%m-%d %H:%M:%S') $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') $*" >&2; }

# ---------- 工具函数 ----------

# 读取 PID 文件获取进程号
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid
        pid=$(cat "${PID_FILE}" 2>/dev/null)
        if [ -n "${pid}" ] && kill -0 "${pid}" 2>/dev/null; then
            echo "${pid}"
            return 0
        fi
    fi
    return 1
}

# 通过 jps 或 ps 查找进程（兜底）
find_pid_fallback() {
    # 优先使用 jps
    if command -v jps &>/dev/null; then
        jps -lv 2>/dev/null | grep "${JAR_NAME}" | grep -v grep | awk '{print $1}'
    else
        ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print $2}'
    fi
}

# 写入 PID 文件
write_pid() {
    local dir
    dir=$(dirname "${PID_FILE}")
    mkdir -p "${dir}"
    echo "$1" > "${PID_FILE}"
}

# 删除 PID 文件
remove_pid() {
    rm -f "${PID_FILE}"
}

# ---------- 前置检查 ----------

preflight_check() {
    # 1. Java 环境
    if ! command -v java &>/dev/null; then
        log_error "未找到 Java 运行环境，请安装 JDK 并设置 JAVA_HOME"
        return 1
    fi
    log_info "Java 版本：$(java -version 2>&1 | head -1)"

    # 2. Jar 文件
    if [ ! -f "${JAR_PATH}/${JAR_NAME}" ]; then
        log_error "Jar 文件不存在：${JAR_PATH}/${JAR_NAME}"
        return 1
    fi

    # 3. 日志目录
    mkdir -p "$(dirname "${LOG_PATH}")"
    mkdir -p "$(dirname "${GC_LOG_PATH}")"

    # 4. 端口检查工具（lsof / ss / netstat）
    if ! command -v lsof &>/dev/null && ! command -v ss &>/dev/null && ! command -v netstat &>/dev/null; then
        log_warn "未找到 lsof / ss / netstat，端口检查将跳过"
    fi

    return 0
}

# 检查端口是否被监听
check_port() {
    if command -v lsof &>/dev/null; then
        lsof -Pi ":${APP_PORT}" -sTCP:LISTEN -t &>/dev/null && return 0
    elif command -v ss &>/dev/null; then
        ss -tlnp 2>/dev/null | grep -q ":${APP_PORT} " && return 0
    elif command -v netstat &>/dev/null; then
        netstat -tlnp 2>/dev/null | grep -q ":${APP_PORT} " && return 0
    fi
    return 1
}

# 检查健康接口
check_health() {
    curl -sf --max-time 5 "${HEALTH_CHECK_URL}" &>/dev/null
}

# ---------- 日志清理 ----------

clean_logs() {
    local log_dir
    log_dir=$(dirname "${LOG_PATH}")
    if [ ! -d "${log_dir}" ]; then
        return 0
    fi

    log_info "清理 ${LOG_RETENTION_DAYS} 天前的日志..."
    local deleted
    deleted=$(find "${log_dir}" -name "*.log.*" -type f -mtime "+${LOG_RETENTION_DAYS}" -print -delete 2>/dev/null | wc -l)
    if [ "${deleted}" -gt 0 ]; then
        log_info "已清理 ${deleted} 个过期日志文件"
    fi

    # 清理过期 GC 日志
    deleted=$(find "${log_dir}" -name "gc.log.*" -type f -mtime "+${LOG_RETENTION_DAYS}" -print -delete 2>/dev/null | wc -l)
    if [ "${deleted}" -gt 0 ]; then
        log_info "已清理 ${deleted} 个过期 GC 日志文件"
    fi

    log_info "日志清理完成"
}

# ---------- 组装 JVM 参数 ----------

build_jvm_opts() {
    local opts="${JVM_OPTS}"

    # GC 日志（Java 9+ 语法）
    local java_ver
    java_ver=$(java -version 2>&1 | head -1 | grep -oP '"\K\d+')
    if [ "${java_ver}" -ge 9 ] 2>/dev/null; then
        opts="${opts} -Xlog:gc*,safepoint=info:file=${GC_LOG_PATH}:time,uptime,level,tags:filecount=10,filesize=50M"
    else
        opts="${opts} -Xloggc:${GC_LOG_PATH} -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=50M"
    fi

    echo "${opts}"
}

# ---------- 启动 ----------

start() {
    log_info "========== 启动 ${JAR_NAME} =========="

    # 前置检查
    if ! preflight_check; then
        return 1
    fi

    # 检查是否已在运行
    if pid=$(get_pid); then
        log_warn "服务已在运行中，PID：${pid}"
        return 0
    fi

    # 检查端口占用
    if check_port; then
        log_error "端口 ${APP_PORT} 已被占用，请检查"
        return 1
    fi

    # 清理旧日志
    clean_logs

    # 加载环境变量文件
    local env_opts=""
    if [ -f "${ENV_FILE}" ]; then
        log_info "加载环境变量文件：${ENV_FILE}"
        set -a
        # shellcheck source=/dev/null
        source "${ENV_FILE}"
        set +a
    fi

    # 进入工作目录
    cd "${JAR_PATH}" || {
        log_error "无法进入目录：${JAR_PATH}"
        return 1
    }

    local full_jvm_opts
    full_jvm_opts=$(build_jvm_opts)

    log_info "JVM 参数：${full_jvm_opts}"
    log_info "启动命令：java ${full_jvm_opts} -jar ${JAR_NAME} --spring.profiles.active=${SPRING_PROFILE}"

    # 后台启动
    nohup java ${full_jvm_opts} \
        -jar "${JAR_NAME}" \
        --spring.profiles.active="${SPRING_PROFILE}" \
        --server.port="${APP_PORT}" \
        >> "${LOG_PATH}" 2>&1 &

    local new_pid=$!
    write_pid "${new_pid}"

    log_info "进程已创建，PID：${new_pid}，等待服务就绪..."

    # 轮询等待就绪
    local elapsed=0
    while [ ${elapsed} -lt ${STARTUP_TIMEOUT} ]; do
        sleep 2
        elapsed=$((elapsed + 2))

        # 进程存活检查
        if ! kill -0 "${new_pid}" 2>/dev/null; then
            log_error "进程已退出，请查看日志：tail -100 ${LOG_PATH}"
            remove_pid
            return 1
        fi

        # 健康检查
        if check_health; then
            log_info "服务启动成功！PID：${new_pid}，端口：${APP_PORT}，耗时：${elapsed}s"
            return 0
        fi

        log_info "等待中...（${elapsed}s/${STARTUP_TIMEOUT}s）"
    done

    log_error "启动超时（${STARTUP_TIMEOUT}s）！进程可能仍在初始化，请查看日志：tail -100 ${LOG_PATH}"
    log_warn "当前进程 PID：${new_pid}，未自动终止"
    return 1
}

# ---------- 停止 ----------

stop() {
    log_info "========== 停止 ${JAR_NAME} =========="

    local pid
    if ! pid=$(get_pid); then
        # PID 文件不存在或无有效进程，尝试兜底查找
        pid=$(find_pid_fallback)
        if [ -z "${pid}" ]; then
            log_warn "服务未运行"
            remove_pid
            return 0
        fi
    fi

    log_info "正在停止进程 PID：${pid}..."

    # 第一步：SIGTERM 优雅停止
    kill -15 "${pid}" 2>/dev/null || true

    local elapsed=0
    while [ ${elapsed} -lt ${STOP_TIMEOUT} ]; do
        sleep 2
        elapsed=$((elapsed + 2))
        if ! kill -0 "${pid}" 2>/dev/null; then
            log_info "服务已优雅停止（耗时 ${elapsed}s）"
            remove_pid
            return 0
        fi
        log_info "等待进程退出...（${elapsed}s/${STOP_TIMEOUT}s）"
    done

    # 第二步：再次尝试 SIGTERM
    log_warn "超时未退出，再次发送 SIGTERM..."
    kill -15 "${pid}" 2>/dev/null || true
    sleep 5
    if ! kill -0 "${pid}" 2>/dev/null; then
        log_info "服务已停止"
        remove_pid
        return 0
    fi

    # 第三步：SIGKILL 强制终止
    log_error "优雅停止失败，执行强制终止（SIGKILL）"
    kill -9 "${pid}" 2>/dev/null || true
    sleep 1
    if ! kill -0 "${pid}" 2>/dev/null; then
        log_info "已强制停止"
    else
        log_error "无法停止进程 PID：${pid}，请手动处理"
        return 1
    fi

    remove_pid
    return 0
}

# ---------- 状态 ----------

status() {
    local pid
    pid=$(get_pid 2>/dev/null) || pid=$(find_pid_fallback)

    if [ -z "${pid}" ]; then
        echo -e "${RED}服务未运行${NC}"
        return 1
    fi

    # 进程信息
    local uptime_info=""
    if command -v ps &>/dev/null; then
        uptime_info=$(ps -o etime= -p "${pid}" 2>/dev/null | xargs)
    fi

    echo -e "进程 PID：${pid}"
    [ -n "${uptime_info}" ] && echo -e "运行时长：${uptime_info}"

    # 端口状态
    if check_port; then
        echo -e "${GREEN}端口 ${APP_PORT}：监听中${NC}"
    else
        echo -e "${RED}端口 ${APP_PORT}：未监听${NC}"
    fi

    # 健康检查
    if check_health; then
        echo -e "${GREEN}健康检查：通过 (${HEALTH_CHECK_URL})${NC}"
    else
        echo -e "${RED}健康检查：失败 (${HEALTH_CHECK_URL})${NC}"
    fi

    return 0
}

# ---------- 重启 ----------

restart() {
    stop
    # 确认进程已完全退出
    sleep 2
    if get_pid &>/dev/null; then
        log_error "停止失败，无法重启"
        return 1
    fi
    start
}

# ---------- 日志 ----------

logs() {
    local opt="${1:-tail}"
    if [ ! -f "${LOG_PATH}" ]; then
        log_error "日志文件不存在：${LOG_PATH}"
        return 1
    fi

    case "${opt}" in
        tail|follow)
            log_info "实时跟踪日志（Ctrl+C 退出）..."
            tail -f "${LOG_PATH}"
            ;;
        head)
            head -100 "${LOG_PATH}"
            ;;
        tail100)
            tail -100 "${LOG_PATH}"
            ;;
        *)
            tail -f "${LOG_PATH}"
            ;;
    esac
}

# ---------- 部署（停止→备份→替换→启动） ----------

deploy() {
    local new_jar="${1}"

    log_info "========== 开始部署 =========="

    if [ -z "${new_jar}" ]; then
        log_error "请指定新 Jar 包路径：$0 deploy /path/to/new.jar"
        return 1
    fi

    if [ ! -f "${new_jar}" ]; then
        log_error "Jar 包不存在：${new_jar}"
        return 1
    fi

    # 停止
    log_info "步骤 1/4：停止服务..."
    stop || {
        log_error "停止服务失败，中止部署"
        return 1
    }

    # 备份
    log_info "步骤 2/4：备份旧 Jar..."
    if [ -f "${JAR_PATH}/${JAR_NAME}" ]; then
        local bak_name="${JAR_NAME}.bak.$(date +%Y%m%d_%H%M%S)"
        cp "${JAR_PATH}/${JAR_NAME}" "${JAR_PATH}/${bak_name}"
        log_info "已备份为：${bak_name}"
    fi

    # 替换
    log_info "步骤 3/4：替换 Jar 包..."
    cp "${new_jar}" "${JAR_PATH}/${JAR_NAME}"
    log_info "Jar 包已替换"

    # 启动
    log_info "步骤 4/4：启动服务..."
    start

    log_info "========== 部署完成 =========="
}

# ---------- 帮助 ----------

usage() {
    cat << 'EOF'

  Ptech Backend 服务管理脚本

用法：
  ./start.sh {start|stop|restart|status|logs|deploy|help}

命令说明：
  start      - 启动服务（含前置检查、端口检测、启动等待）
  stop       - 优雅停止服务（SIGTERM → 超时 SIGKILL）
  restart    - 重启服务
  status     - 查看进程、端口、健康状态
  logs       - 实时跟踪日志（默认），支持：tail / head / tail100
  deploy     - 一键部署：停止 → 备份 → 替换 → 启动
  help       - 显示此帮助

配置项（脚本头部修改）：
  JAR_NAME          - Jar 包文件名（默认：site-server.jar）
  JAR_PATH          - Jar 包所在目录
  LOG_PATH          - 应用日志路径
  GC_LOG_PATH       - GC 日志路径
  LOG_RETENTION_DAYS - 日志保留天数（默认：30）
  JVM_OPTS          - JVM 运行时参数
  APP_PORT          - 应用端口
  SPRING_PROFILE    - Spring Profile
  HEALTH_CHECK_URL  - 健康检查 URL
  STARTUP_TIMEOUT   - 启动超时秒数（默认：60）
  STOP_TIMEOUT      - 停止超时秒数（默认：60）
  PID_FILE          - PID 文件路径
  ENV_FILE          - 环境变量文件路径（可选，格式 KEY=VALUE）

示例：
  ./start.sh start                     # 启动
  ./start.sh status                    # 查看状态
  ./start.sh logs tail100              # 查看最近 100 行日志
  ./start.sh deploy /tmp/new-app.jar   # 一键部署新版本

EOF
}

# ---------- 入口 ----------

case "${1}" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs "${2}"
        ;;
    deploy)
        deploy "${2}"
        ;;
    help|--help|-h)
        usage
        ;;
    *)
        usage
        exit 1
        ;;
esac
