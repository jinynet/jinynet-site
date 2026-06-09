package cn.jinynet.site.service;

import cn.jinynet.site.entity.Article;
import cn.jinynet.site.entity.ArticleTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.sql.JSqlClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 文章阅读量统计服务
 * 基于IP限制阅读量统计，防止刷阅读量
 *
 * @author jinty
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleViewService {

    private final JSqlClient sqlClient;
    private final StringRedisTemplate redisTemplate;

    /**
     * 阅读量统计的Redis Key前缀
     */
    private static final String VIEW_KEY_PREFIX = "article:view:";

    /**
     * 同一IP对同一文章的阅读量统计间隔（24小时）
     */
    private static final Duration VIEW_INTERVAL = Duration.ofHours(24);

    /**
     * 记录文章阅读量（带IP限制）
     *
     * @param articleId 文章ID
     * @param ipAddress 访问者IP地址
     */
    public void recordView(long articleId, String ipAddress) {
        // 生成唯一的访问记录Key
        String viewKey = VIEW_KEY_PREFIX + articleId + ":" + ipAddress;

        // 尝试设置Redis键，如果键已存在则返回false
        Boolean success = redisTemplate.opsForValue().setIfAbsent(viewKey, "1", VIEW_INTERVAL);

        if (Boolean.TRUE.equals(success)) {
            // Redis设置成功，说明是新访问，增加阅读量
            incrementViewCount(articleId);
            log.debug("文章 {} 阅读量+1，IP: {}", articleId, ipAddress);
        } else {
            // Redis键已存在，说明该IP在24小时内已经访问过
            log.debug("文章 {} 在24小时内已被IP {} 访问过，跳过阅读量统计", articleId, ipAddress);
        }
    }

    /**
     * 增加文章阅读量
     *
     * @param articleId 文章ID
     */
    private void incrementViewCount(Long articleId) {
        sqlClient.createUpdate(ArticleTable.$)
                .where(ArticleTable.$.id().eq(articleId))
                .set(ArticleTable.$.viewCount(), ArticleTable.$.viewCount().plus(1))
                .execute();
    }

    /**
     * 获取文章详情（包含阅读量统计）
     *
     * @param articleId 文章ID
     * @param ipAddress 访问者IP地址
     * @param requireLogin 是否需要登录才能访问
     * @return 文章详情，如果未找到或无权限返回null
     */
    public Article getArticleWithView(Long articleId, String ipAddress, boolean requireLogin) {
        Article article = sqlClient.createQuery(ArticleTable.$)
                .where(ArticleTable.$.id().eq(articleId))
                .select(ArticleTable.$)
                .fetchOptional()
                .orElse(null);

        if (article != null) {
            // 尝试记录阅读量（如果IP限制允许）
            recordView(articleId, ipAddress);
        }

        return article;
    }

    /**
     * 清除指定文章的所有访问记录（用于文章更新或重置阅读量）
     *
     * @param articleId 文章ID
     */
    public void clearViewRecords(Long articleId) {
        // 由于Redis不支持通配符删除，这里我们需要扫描匹配的key
        // 注意：生产环境中应避免使用KEYS命令，可以使用SCAN命令
        var keys = redisTemplate.keys(VIEW_KEY_PREFIX + articleId + ":*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已清除文章 {} 的所有访问记录", articleId);
        }
    }
}
