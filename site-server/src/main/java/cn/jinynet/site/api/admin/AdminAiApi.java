package cn.jinynet.site.api.admin;

import cn.jinynet.site.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * AI 帮写接口
 * <p>
 * 提供文章生成、续写、润色、摘要、标题等 AI 辅助写作功能。
 * 所有接口返回 SSE 流式响应，前端通过 fetch + ReadableStream 实时读取。
 * 路径 /admin/** 已被 Sa-Token 拦截器统一保护，需登录后访问。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/admin/articles/ai")
@RequiredArgsConstructor
public class AdminAiApi {

    private final AiService aiService;

    /**
     * 生成文章
     * <p>
     * 根据主题、关键词、写作风格生成一篇完整的 Markdown 格式技术文章。
     * </p>
     *
     * @param body 请求体，包含 topic（主题）、keywords（关键词，可选）、style（风格，可选）
     * @return SSE 流式响应
     */
    @PostMapping(value = "/generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generate(@RequestBody Map<String, String> body) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiService.generateArticle(
                body.get("topic"),
                body.get("keywords"),
                body.get("style"),
                emitter
        );
        return emitter;
    }

    /**
     * 续写内容
     * <p>
     * 根据已有文章内容进行续写，保持风格和语调一致。
     * </p>
     *
     * @param body 请求体，包含 content（已有内容）、direction（续写方向，可选）
     * @return SSE 流式响应
     */
    @PostMapping(value = "/continue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter continueWriting(@RequestBody Map<String, String> body) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiService.continueArticle(
                body.get("content"),
                body.get("direction"),
                emitter
        );
        return emitter;
    }

    /**
     * 润色优化
     * <p>
     * 对文章内容进行润色优化，支持指定优化方向（如语法修正、文笔润色等）。
     * </p>
     *
     * @param body 请求体，包含 content（文章内容）、type（优化方向，可选）
     * @return SSE 流式响应
     */
    @PostMapping(value = "/optimize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter optimize(@RequestBody Map<String, String> body) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiService.optimizeArticle(
                body.get("content"),
                body.get("type"),
                emitter
        );
        return emitter;
    }

    /**
     * 生成摘要
     * <p>
     * 为文章内容生成一段不超过200字的简洁摘要。
     * </p>
     *
     * @param body 请求体，包含 content（文章内容）
     * @return SSE 流式响应
     */
    @PostMapping(value = "/summarize", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter summarize(@RequestBody Map<String, String> body) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiService.summarizeArticle(body.get("content"), emitter);
        return emitter;
    }

    /**
     * 生成标题
     * <p>
     * 为文章内容生成3个合适的标题建议。
     * </p>
     *
     * @param body 请求体，包含 content（文章内容）
     * @return SSE 流式响应
     */
    @PostMapping(value = "/title", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter title(@RequestBody Map<String, String> body) {
        SseEmitter emitter = new SseEmitter(120000L);
        aiService.generateTitle(body.get("content"), emitter);
        return emitter;
    }
}
