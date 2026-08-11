package cn.jinynet.site.service;

import cn.jinynet.sdk.llm.client.LlmClient;
import cn.jinynet.sdk.llm.client.LlmHttpExecutor;
import cn.jinynet.sdk.llm.client.StreamHandler;
import cn.jinynet.sdk.llm.config.LlmProperties;
import cn.jinynet.site.types.AiConfig;
import cn.jinynet.starter.common.types.exception.BaseBizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AI 帮写服务
 * <p>
 * 从系统设置数据库读取 AI 配置，动态创建 LlmClient，
 * 提供文章生成、续写、润色、摘要、标题等 AI 帮写功能，
 * 通过 SseEmitter 实现流式输出。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final SettingsService settingsService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 缓存的 LlmClient（配置变更时自动重建）
     */
    private volatile LlmClient cachedClient;
    private volatile String cachedApiKey;

    /**
     * 获取或创建 LlmClient
     * <p>
     * AI 配置存储在数据库中（可通过管理后台修改），
     * 当 API Key 发生变化时自动重建 Client。
     * </p>
     *
     * @return LlmClient 实例
     * @throws RuntimeException AI 功能未启用或未配置 API Key 时抛出
     */
    private LlmClient getLlmClient() {
        AiConfig config = settingsService.getAiConfig();
        if (!config.isEnabled() || config.getApiKey() == null || config.getApiKey().isEmpty()) {
            throw new BaseBizException("AI 功能未启用或未配置 API Key");
        }
        // 如果 API Key 变了，重建 client
        if (cachedClient == null || !config.getApiKey().equals(cachedApiKey)) {
            synchronized (this) {
                if (cachedClient == null || !config.getApiKey().equals(cachedApiKey)) {
                    LlmProperties props = getLlmProperties(config);

                    OkHttpClient httpClient = new OkHttpClient.Builder()
                            .connectTimeout(props.getConnectTimeout(), TimeUnit.MILLISECONDS)
                            .readTimeout(props.getReadTimeout(), TimeUnit.MILLISECONDS)
                            .build();

                    // 创建 LlmHttpExecutor 和 LlmClient
                    LlmHttpExecutor httpExecutor = new LlmHttpExecutor(props, httpClient);
                    cachedClient = new LlmClient(httpExecutor, props);
                    cachedApiKey = config.getApiKey();
                    log.info("LlmClient 已重建，模型：{}", props.getModel());
                }
            }
        }
        return cachedClient;
    }

    private static @NonNull LlmProperties getLlmProperties(AiConfig config) {
        LlmProperties props = new LlmProperties();
        props.setApiKey(config.getApiKey());
        if (config.getBaseUrl() != null && !config.getBaseUrl().isEmpty()) {
            props.setBaseUrl(config.getBaseUrl());
        }
        if (config.getModel() != null && !config.getModel().isEmpty()) {
            props.setModel(config.getModel());
        }
        if (config.getTemperature() != null) {
            props.setTemperature(config.getTemperature());
        }
        if (config.getMaxTokens() != null) {
            props.setMaxTokens(config.getMaxTokens());
        }
        return props;
    }

    /**
     * 通用流式 AI 请求
     * <p>
     * 使用线程池异步执行流式请求，通过 SseEmitter 将内容实时推送到前端。
     * SSE 数据格式：data: {"content":"..."} 或 data: [DONE] 或 data: {"error":"..."}
     * </p>
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @param emitter      SSE 发射器
     */
    private void streamRequest(String systemPrompt, String userMessage, SseEmitter emitter) {
        LlmClient client = getLlmClient();
        executor.execute(() -> {
            try {
                client.streamChat(systemPrompt, userMessage, new StreamHandler() {
                    @Override
                    public void onContent(String content) {
                        try {
                            // 前端解析格式为 "data: {...}"，需在数据前加空格使 Spring 输出 "data: {...}"
                            emitter.send(SseEmitter.event()
                                    .data(" {\"content\":\"" + escapeJson(content) + "\"}"));
                        } catch (IOException e) {
                            log.warn("SSE 发送失败: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete(String fullContent) {
                        try {
                            emitter.send(SseEmitter.event().data(" [DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("SSE 完成发送失败: {}", e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("AI 流式请求出错", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(" {\"error\":\"" + escapeJson(error.getMessage()) + "\"}"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.warn("SSE 错误发送失败: {}", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                log.error("AI 流式请求异常", e);
                try {
                    emitter.send(SseEmitter.event()
                            .data(" {\"error\":\"" + escapeJson(e.getMessage()) + "\"}"));
                    emitter.complete();
                } catch (IOException ex) {
                    log.warn("SSE 异常发送失败: {}", ex.getMessage());
                }
            }
        });
    }

    /**
     * 生成文章
     *
     * @param topic    文章主题
     * @param keywords 关键词（可选）
     * @param style    写作风格（可选）
     * @param emitter  SSE 发射器
     */
    public void generateArticle(String topic, String keywords, String style, SseEmitter emitter) {
        String systemPrompt = "你是一位专业的技术博客作者。请根据用户提供的主题撰写一篇高质量的 Markdown 格式文章。" +
                "文章应包含清晰的标题层级、代码示例（如适用）、适当的段落分隔。" +
                (style != null && !style.isEmpty() ? "写作风格：" + style + "。" : "") +
                "直接输出文章内容，不要添加额外的说明或解释。";
        StringBuilder userMsg = new StringBuilder();
        userMsg.append("主题：").append(topic).append("\n");
        if (keywords != null && !keywords.isEmpty()) {
            userMsg.append("关键词：").append(keywords).append("\n");
        }
        userMsg.append("请撰写一篇完整的技术文章。");
        streamRequest(systemPrompt, userMsg.toString(), emitter);
    }

    /**
     * 续写内容
     *
     * @param content   已有内容
     * @param direction 续写方向（可选）
     * @param emitter   SSE 发射器
     */
    public void continueArticle(String content, String direction, SseEmitter emitter) {
        String systemPrompt = "你是一位专业的技术博客作者。请根据已有的文章内容进行续写，保持风格和语调一致。" +
                "只输出续写的内容，不要重复已有内容。" +
                (direction != null && !direction.isEmpty() ? "续写方向：" + direction + "。" : "");
        String userMessage = "已有内容：\n\n" + content + "\n\n请继续撰写后续内容。";
        streamRequest(systemPrompt, userMessage, emitter);
    }

    /**
     * 润色优化
     *
     * @param content 文章内容
     * @param type    优化方向（可选）
     * @param emitter SSE 发射器
     */
    public void optimizeArticle(String content, String type, SseEmitter emitter) {
        String systemPrompt = "你是一位专业的文字编辑。请对提供的文章内容进行润色优化，保持原有信息不变。" +
                (type != null && !type.isEmpty() ? "优化方向：" + type + "。" : "优化方向：语法修正和文笔润色。") +
                "直接输出优化后的完整内容，使用 Markdown 格式。";
        String userMessage = "请优化以下内容：\n\n" + content;
        streamRequest(systemPrompt, userMessage, emitter);
    }

    /**
     * 生成摘要
     *
     * @param content 文章内容
     * @param emitter SSE 发射器
     */
    public void summarizeArticle(String content, SseEmitter emitter) {
        String systemPrompt = "请为以下文章生成一段简洁的摘要，不超过200字。直接输出摘要内容，不要添加额外说明。";
        String userMessage = content;
        streamRequest(systemPrompt, userMessage, emitter);
    }

    /**
     * 生成标题
     *
     * @param content 文章内容
     * @param emitter SSE 发射器
     */
    public void generateTitle(String content, SseEmitter emitter) {
        String systemPrompt = "请为以下文章内容生成3个合适的标题建议，每行一个，不要编号。直接输出标题，不要额外说明。";
        String userMessage = content;
        streamRequest(systemPrompt, userMessage, emitter);
    }

    /**
     * JSON 字符串转义
     * <p>
     * 处理反斜杠、引号、换行符等特殊字符，确保 JSON 格式正确。
     * </p>
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
