package cn.jinynet.site.types;

import lombok.Data;

/**
 * AI 大模型配置
 * <p>
 * 配置信息从系统设置数据库读取，支持运行时动态修改。
 * </p>
 *
 * @author jinty
 * @since 1.0
 */
@Data
public class AiConfig {

    /**
     * 是否启用 AI 功能
     */
    private boolean enabled;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 接口基础地址
     */
    private String baseUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 温度参数（控制生成随机性，0-2）
     */
    private Double temperature;

    /**
     * 最大生成 Token 数
     */
    private Integer maxTokens;
}
